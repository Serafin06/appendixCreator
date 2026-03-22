import io.github.cdimascio.dotenv.dotenv
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.sql.Date
import java.sql.DriverManager
import java.time.LocalDate

fun main(args: Array<String>) {
    println("=== Import załączników z Excel ===")
    println()

    val sciezkaPliku = when {
        args.isNotEmpty() -> args[0]
        else -> {
            val chooser = javax.swing.JFileChooser().apply {
                dialogTitle = "Wybierz plik Excel z załącznikami"
                fileFilter = javax.swing.filechooser.FileNameExtensionFilter("Pliki Excel (*.xlsx)", "xlsx")
            }
            if (chooser.showOpenDialog(null) == javax.swing.JFileChooser.APPROVE_OPTION) {
                chooser.selectedFile.absolutePath
            } else {
                println("❌ Nie wybrano pliku"); return
            }
        }
    }

    val plik = File(sciezkaPliku)
    if (!plik.exists()) { println("❌ Plik nie istnieje: $sciezkaPliku"); return }

    val dotenv = try {
        dotenv {
            val currentDir = System.getProperty("user.dir")
            directory = if (File(currentDir, ".env").exists()) currentDir else ".."
            ignoreIfMissing = false
        }
    } catch (e: Exception) {
        println("❌ Nie znaleziono pliku .env"); return
    }

    val jdbcUrl = "jdbc:postgresql://${dotenv["DB_HOST"]}:${dotenv["DB_PORT"] ?: "5432"}/${dotenv["DB_NAME"] ?: "postgres"}"
    val user = dotenv["DB_USER"] ?: error("Brak DB_USER")
    val password = dotenv["DB_PASSWORD"] ?: error("Brak DB_PASSWORD")

    println("🔌 Łączę z bazą: $jdbcUrl")
    println("📂 Wczytuję plik: ${plik.name}")

    val budynki = wczytajZalaczniki(plik)
    val lacznie = budynki.sumOf { it.prace.size }
    val lacznieMat = budynki.sumOf { b -> b.prace.sumOf { it.materialy.size } }
    println("📊 Znaleziono: ${budynki.size} budynków, $lacznie prac, $lacznieMat pozycji materiałów")
    println()

    if (budynki.isEmpty()) { println("⚠️ Brak danych!"); return }

    println("=== Podgląd ===")
    budynki.take(3).forEach { b ->
        println("  📍 ${b.ulica}")
        b.prace.take(2).forEach { p ->
            println("     ${p.data} | ${p.roboczogodziny}h | VAT ${p.vat}% | ${p.opis.take(50)}...")
            p.materialy.forEach { m -> println("       🔧 ${m.nazwa} x${m.ilosc}") }
        }
    }
    println()

    // Wykryj miesiąc/rok z danych
    val daty = budynki.flatMap { b -> b.prace.map { it.data } }
    val rok = daty.minOf { it.year }
    val miesiac = daty.minOf { it.monthValue }
    val nazwyMiesiecy = listOf("", "styczeń", "luty", "marzec", "kwiecień", "maj", "czerwiec",
        "lipiec", "sierpień", "wrzesień", "październik", "listopad", "grudzień")
    println("📅 Wykryto okres: ${nazwyMiesiecy[miesiac]} $rok")
    println()

    // Dialog wyboru trybu importu
    val opcje = arrayOf("Wyczyść ${ nazwyMiesiecy[miesiac] } $rok i importuj", "Doimportuj (ryzyko duplikatów)", "Anuluj")
    val wybor = javax.swing.JOptionPane.showOptionDialog(
        null,
        "Wykryto prace z okresu: ${nazwyMiesiecy[miesiac]} $rok\n\n" +
                "Co chcesz zrobić z istniejącymi pracami z tego okresu w bazie?",
        "Zarządzanie duplikatami — ${ nazwyMiesiecy[miesiac] } $rok",
        javax.swing.JOptionPane.DEFAULT_OPTION,
        javax.swing.JOptionPane.QUESTION_MESSAGE,
        null,
        opcje,
        opcje[0]
    )

    val czyscPrzedImportem = when (wybor) {
        0 -> { println("🗑️  Tryb: wyczyść i importuj od nowa"); true }
        1 -> { println("➕ Tryb: doimportuj bez czyszczenia"); false }
        else -> { println("❌ Anulowano"); return }
    }

    println()
    println("⏳ Importuję...")

    val result = importujZalacznikDoBazy(jdbcUrl, user, password, budynki, rok, miesiac, czyscPrzedImportem)

    println()
    println("=== Wynik ===")
    if (czyscPrzedImportem) println("🗑️  Usunięto prace: ${result.praceUsuniete}")
    println("✅ Budynki dodane:     ${result.budynkiDodane}")
    println("⏭️  Budynki istniały:   ${result.budynkiIstniejace}")
    println("✅ Prace dodane:       ${result.praceDodane}")
    println("✅ Materiały dodane:   ${result.materialyDodane}")
    println("⚠️  Materiały nieznane: ${result.materialyNieznane.size}")
    println("❌ Błędy:              ${result.bledy.size}")

    if (result.materialyNieznane.isNotEmpty()) {
        println()
        println("=== Nieznane materiały (brak w tabeli materialy) ===")
        result.materialyNieznane.forEach { println("  • $it") }
    }
    if (result.bledy.isNotEmpty()) {
        println()
        println("=== Błędy ===")
        result.bledy.forEach { println("  • $it") }
    }

    println()
    println("🎉 Import zakończony!")
}

// === Modele ===

data class MaterialExcel(val nazwa: String, val ilosc: Double)

data class PracaExcel(
    val data: LocalDate,
    val opis: String,
    val roboczogodziny: Int,
    val kosztDojazdu: Double,
    val vat: Int,
    val materialy: List<MaterialExcel>
)

data class BudynekExcel(val ulica: String, val prace: List<PracaExcel>)

data class ZalacznikImportResult(
    val praceUsuniete: Int,
    val budynkiDodane: Int,
    val budynkiIstniejace: Int,
    val praceDodane: Int,
    val materialyDodane: Int,
    val materialyNieznane: List<String>,
    val bledy: List<String>
)

// === Parsowanie Excel ===

fun wczytajZalaczniki(plik: File): List<BudynekExcel> {
    val workbook: Workbook = plik.inputStream().use { XSSFWorkbook(it) }
    val wyniki = mutableListOf<BudynekExcel>()

    for (i in 0 until workbook.numberOfSheets) {
        val sheet = workbook.getSheetAt(i)
        val name = sheet.sheetName.trim()
        if (name == "Cennik" || name.startsWith("Arkusz")) continue

        val wierszeDanych = (10..sheet.lastRowNum)
            .mapNotNull { sheet.getRow(it) }
            .filter { row -> (0..15).any { row.getCell(it)?.cellType != CellType.BLANK } }

        if (wierszeDanych.isEmpty()) continue

        val prace = parsujPraceZMaterialami(wierszeDanych, name)
        if (prace.isNotEmpty()) wyniki.add(BudynekExcel(ulica = name, prace = prace))
    }

    workbook.close()
    return wyniki
}

fun parsujPraceZMaterialami(wiersze: List<Row>, sheetName: String): List<PracaExcel> {
    val grupy = mutableListOf<List<Row>>()
    var aktualna = mutableListOf<Row>()

    for (wiersz in wiersze) {
        val lp = wiersz.getCell(0)
        if (lp != null && lp.cellType == CellType.NUMERIC) {
            if (aktualna.isNotEmpty()) grupy.add(aktualna.toList())
            aktualna = mutableListOf(wiersz)
        } else if (aktualna.isNotEmpty()) {
            aktualna.add(wiersz)
        }
    }
    if (aktualna.isNotEmpty()) grupy.add(aktualna)

    val prace = mutableListOf<PracaExcel>()

    for (grupa in grupy) {
        val pierwsza = grupa[0]

        val data = pobierzDateKom(pierwsza.getCell(1)) ?: run {
            println("  [WARN] $sheetName LP=${pierwsza.getCell(0)?.numericCellValue?.toInt()}: brak daty, pomijam")
            continue
        }
        val opis = pobierzKomorke(pierwsza.getCell(2))?.trim() ?: run {
            println("  [WARN] $sheetName LP=${pierwsza.getCell(0)?.numericCellValue?.toInt()}: brak opisu, pomijam")
            continue
        }

        var roboczogodziny = 0
        var kosztDojazdu = 0.0
        var vat = 23
        val materialy = mutableListOf<MaterialExcel>()

        for (wiersz in grupa) {
            wiersz.getCell(3)?.takeIf { it.cellType == CellType.NUMERIC }?.let {
                roboczogodziny = it.numericCellValue.toInt()
            }
            val nazwaM = pobierzKomorke(wiersz.getCell(5))
            val ilosc = wiersz.getCell(6)?.takeIf { it.cellType == CellType.NUMERIC }?.numericCellValue
            if (!nazwaM.isNullOrBlank() && ilosc != null && ilosc > 0) {
                materialy.add(MaterialExcel(nazwa = nazwaM.trim(), ilosc = ilosc))
            }
            if (pobierzKomorke(wiersz.getCell(10)) == "dojazd") {
                wiersz.getCell(12)?.takeIf { it.cellType == CellType.NUMERIC }?.let {
                    kosztDojazdu = it.numericCellValue
                }
            }
            for (vatCol in listOf(14, 15)) {
                wiersz.getCell(vatCol)?.takeIf { it.cellType == CellType.NUMERIC }?.let {
                    val vInt = Math.round(it.numericCellValue * 100).toInt()
                    if (vInt in listOf(8, 23)) vat = vInt
                }
            }
        }

        prace.add(PracaExcel(data, opis, roboczogodziny, kosztDojazdu, vat, materialy))
    }

    return prace
}

// === Import do bazy ===

fun importujZalacznikDoBazy(
    jdbcUrl: String,
    user: String,
    password: String,
    budynki: List<BudynekExcel>,
    rok: Int,
    miesiac: Int,
    czyscPrzedImportem: Boolean
): ZalacznikImportResult {
    var praceUsuniete = 0
    var budynkiDodane = 0
    var budynkiIstniejace = 0
    var praceDodane = 0
    var materialyDodane = 0
    val materialyNieznane = mutableListOf<String>()
    val bledy = mutableListOf<String>()
    val miasto = "Katowice"

    DriverManager.getConnection(jdbcUrl, user, password).use { conn ->
        // Opcjonalne czyszczenie - usuwa prace (cascade usuwa też praca_material)
        if (czyscPrzedImportem) {
            conn.prepareStatement(
                "DELETE FROM praca WHERE EXTRACT(YEAR FROM data) = ? AND EXTRACT(MONTH FROM data) = ?"
            ).use { stmt ->
                stmt.setInt(1, rok)
                stmt.setInt(2, miesiac)
                praceUsuniete = stmt.executeUpdate()
                println("🗑️  Usunięto $praceUsuniete prac z $miesiac/$rok")
            }
        }

        // Słownik materiałów: nazwa lowercase -> id
        val slownikMaterialow = mutableMapOf<String, Long>()
        conn.createStatement().use { stmt ->
            val rs = stmt.executeQuery("SELECT id, LOWER(nazwa) FROM materialy")
            while (rs.next()) slownikMaterialow[rs.getString(2)] = rs.getLong(1)
        }
        println("ℹ️  Załadowano ${slownikMaterialow.size} materiałów ze słownika")

        val sqlSelectBudynek = "SELECT id FROM budynki WHERE ulica = ? AND miasto = ?"
        val sqlInsertBudynek = "INSERT INTO budynki (ulica, miasto) VALUES (?, ?) RETURNING id"
        val sqlInsertPraca = """
            INSERT INTO praca (data, opis, roboczogodziny, koszt_dojazdu, vat, budynek_id)
            VALUES (?, ?, ?, ?, ?, ?) RETURNING id
        """.trimIndent()
        val sqlInsertPracaMaterial = "INSERT INTO praca_material (ilosc, material_id, praca_id) VALUES (?, ?, ?)"

        for (budynek in budynki) {
            try {
                val budynekId: Long = conn.prepareStatement(sqlSelectBudynek).use { stmt ->
                    stmt.setString(1, budynek.ulica)
                    stmt.setString(2, miasto)
                    val rs = stmt.executeQuery()
                    if (rs.next()) {
                        budynkiIstniejace++; rs.getLong(1)
                    } else {
                        budynkiDodane++
                        conn.prepareStatement(sqlInsertBudynek).use { ins ->
                            ins.setString(1, budynek.ulica)
                            ins.setString(2, miasto)
                            val rs2 = ins.executeQuery()
                            rs2.next(); rs2.getLong(1)
                        }
                    }
                }

                for (praca in budynek.prace) {
                    try {
                        val pracaId: Long = conn.prepareStatement(sqlInsertPraca).use { stmt ->
                            stmt.setDate(1, Date.valueOf(praca.data))
                            stmt.setString(2, praca.opis)
                            stmt.setInt(3, praca.roboczogodziny)
                            stmt.setDouble(4, praca.kosztDojazdu)
                            stmt.setInt(5, praca.vat)
                            stmt.setLong(6, budynekId)
                            val rs = stmt.executeQuery()
                            rs.next(); rs.getLong(1)
                        }
                        praceDodane++

                        conn.prepareStatement(sqlInsertPracaMaterial).use { stmt ->
                            for (mat in praca.materialy) {
                                val materialId = slownikMaterialow[mat.nazwa.lowercase()]
                                if (materialId == null) {
                                    val klucz = "${budynek.ulica} | ${praca.data} | ${mat.nazwa}"
                                    if (klucz !in materialyNieznane) materialyNieznane.add(klucz)
                                    continue
                                }
                                stmt.setDouble(1, mat.ilosc)
                                stmt.setLong(2, materialId)
                                stmt.setLong(3, pracaId)
                                stmt.executeUpdate()
                                materialyDodane++
                            }
                        }
                    } catch (e: Exception) {
                        bledy.add("${budynek.ulica} | ${praca.data}: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                bledy.add("Budynek '${budynek.ulica}': ${e.message}")
            }
        }
    }

    return ZalacznikImportResult(praceUsuniete, budynkiDodane, budynkiIstniejace, praceDodane, materialyDodane, materialyNieznane, bledy)
}

// === Helpers ===

fun pobierzKomorke(cell: Cell?): String? {
    if (cell == null) return null
    return when (cell.cellType) {
        CellType.STRING -> cell.stringCellValue.trim().takeIf { it.isNotBlank() }
        CellType.NUMERIC -> {
            val n = cell.numericCellValue
            if (n == n.toLong().toDouble()) n.toLong().toString() else n.toString()
        }
        else -> null
    }
}

fun pobierzDateKom(cell: Cell?): LocalDate? {
    if (cell == null) return null
    return try {
        when (cell.cellType) {
            CellType.NUMERIC -> cell.dateCellValue.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
            else -> null
        }
    } catch (e: Exception) { null }
}