import io.github.cdimascio.dotenv.dotenv
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.sql.DriverManager

fun main(args: Array<String>) {
    println("=== Import materiałów z Excel ===")
    println()

    // Pobierz ścieżkę do pliku
    val sciezkaPliku = when {
        args.isNotEmpty() -> args[0]
        else -> {
            print("Podaj ścieżkę do pliku Excel: ")
            readLine()?.trim() ?: ""
        }
    }

    if (sciezkaPliku.isBlank()) {
        println("❌ Nie podano ścieżki do pliku!")
        return
    }

    val plik = File(sciezkaPliku)
    if (!plik.exists()) {
        println("❌ Plik nie istnieje: $sciezkaPliku")
        return
    }

    // Wczytaj konfigurację bazy z .env (szukaj w folderze nadrzędnym - główny projekt)
    val dotenv = try {
        dotenv {
            // Sprawdzamy najpierw folder, w którym jesteśmy
            val currentDir = System.getProperty("user.dir")
            directory = if (File(currentDir, ".env").exists()) {
                currentDir
            } else {
                // Jeśli nie ma go tutaj, sprawdzamy poziom wyżej (dla folderu tools)
                ".."
            }
            ignoreIfMissing = false
        }
    } catch (e: Exception) {
        println("❌ Krytyczny błąd: Nie znaleziono pliku .env")
        println("Szukałem w: ${System.getProperty("user.dir")} oraz w folderze nadrzędnym.")
        return
    }

    val host = dotenv["DB_HOST"] ?: error("Brak DB_HOST w .env")
    val port = dotenv["DB_PORT"] ?: "5432"
    val dbName = dotenv["DB_NAME"] ?: "postgres"
    val user = dotenv["DB_USER"] ?: error("Brak DB_USER w .env")
    val password = dotenv["DB_PASSWORD"] ?: error("Brak DB_PASSWORD w .env")

    val jdbcUrl = "jdbc:postgresql://$host:$port/$dbName"

    println("🔌 Łączę z bazą: $jdbcUrl")

    // Wczytaj Excel
    println("📂 Wczytuję plik: ${plik.name}")
    val materialy = wczytajExcel(plik)
    println("📊 Znaleziono ${materialy.size} materiałów do importu")
    println()

    if (materialy.isEmpty()) {
        println("⚠️ Brak danych do importu!")
        return
    }

    // Pokaż podgląd
    println("=== Podgląd (pierwsze 5) ===")
    materialy.take(5).forEach { (nazwa, jednostka, cena) ->
        println("  $nazwa | $jednostka | $cena zł")
    }
    if (materialy.size > 5) println("  ... i ${materialy.size - 5} więcej")
    println()

    // Potwierdź import
    print("Czy chcesz importować? (T/N): ")
    val potwierdzenie = readLine()?.trim()?.uppercase()
    if (potwierdzenie != "T") {
        println("❌ Import anulowany")
        return
    }

    // Importuj do bazy
    println()
    println("⏳ Importuję...")

    val result = importujDoBazy(jdbcUrl, user, password, materialy)

    println()
    println("=== Wynik importu ===")
    println("✅ Dodano:    ${result.dodane}")
    println("⏭️  Pominięto: ${result.pominiete} (duplikaty)")
    println("❌ Błędy:     ${result.bledy.size}")

    if (result.bledy.isNotEmpty()) {
        println()
        println("=== Szczegóły błędów ===")
        result.bledy.forEach { println("  • $it") }
    }

    println()
    println("🎉 Import zakończony!")
}

// Data class dla wiersza z Excela
data class WierszExcel(
    val nazwa: String,
    val jednostka: String,
    val cena: Double
)

data class ImportResult(
    val dodane: Int,
    val pominiete: Int,
    val bledy: List<String>
)

fun wczytajExcel(plik: File): List<WierszExcel> {
    val wynik = mutableListOf<WierszExcel>()

    val workbook: Workbook = plik.inputStream().use { stream ->
        if (plik.extension.lowercase() == "xlsx") XSSFWorkbook(stream)
        else HSSFWorkbook(stream)
    }

    val sheet = workbook.getSheetAt(0)

    // Pomijamy wiersz 0 (nagłówek: LP | Nazwa | Jednostka | Cena)
    for (i in 1..sheet.lastRowNum) {
        val row = sheet.getRow(i) ?: continue

        // Kolumna 1 = Nazwa (pomijamy 0 = LP)
        val nazwa = pobierzTekst(row.getCell(1))
        if (nazwa.isNullOrBlank()) continue

        // Kolumna 2 = Jednostka
        val jednostkaRaw = pobierzTekst(row.getCell(2)) ?: "szt"
        val jednostka = normalizujJednostke(jednostkaRaw)

        // Kolumna 3 = Cena
        val cena = pobierzCene(row.getCell(3))
        if (cena == null || cena <= 0) {
            println("⚠️  Pominięto '$nazwa' - brak ceny")
            continue
        }

        wynik.add(WierszExcel(nazwa.trim(), jednostka, cena))
    }

    workbook.close()
    return wynik
}

fun importujDoBazy(
    jdbcUrl: String,
    user: String,
    password: String,
    materialy: List<WierszExcel>
): ImportResult {
    var dodane = 0
    var pominiete = 0
    val bledy = mutableListOf<String>()

    DriverManager.getConnection(jdbcUrl, user, password).use { conn ->
        // Pobierz istniejące nazwy (lowercase) żeby wykryć duplikaty
        val istniejace = mutableSetOf<String>()
        conn.createStatement().use { stmt ->
            val rs = stmt.executeQuery("SELECT LOWER(nazwa) FROM materialy")
            while (rs.next()) istniejace.add(rs.getString(1))
        }

        println("ℹ️  W bazie już jest ${istniejace.size} materiałów")

        val sql = "INSERT INTO materialy (nazwa, jednostka, cena_za_jednostke) VALUES (?, ?, ?)"

        conn.prepareStatement(sql).use { stmt ->
            materialy.forEach { (nazwa, jednostka, cena) ->
                try {
                    if (nazwa.lowercase() in istniejace) {
                        pominiete++
                        return@forEach
                    }

                    stmt.setString(1, nazwa)
                    stmt.setString(2, jednostka)
                    stmt.setDouble(3, cena)
                    stmt.executeUpdate()

                    dodane++
                    istniejace.add(nazwa.lowercase())

                } catch (e: Exception) {
                    bledy.add("'$nazwa': ${e.message}")
                }
            }
        }
    }

    return ImportResult(dodane, pominiete, bledy)
}

fun pobierzTekst(cell: Cell?): String? {
    if (cell == null) return null
    return when (cell.cellType) {
        CellType.STRING -> cell.stringCellValue.trim()
        CellType.NUMERIC -> {
            val num = cell.numericCellValue
            // Sprawdź czy to liczba całkowita
            if (num == num.toLong().toDouble()) num.toLong().toString()
            else num.toString()
        }
        CellType.BLANK -> null
        else -> null
    }
}

fun pobierzCene(cell: Cell?): Double? {
    if (cell == null) return null
    return when (cell.cellType) {
        CellType.NUMERIC -> cell.numericCellValue
        CellType.STRING -> cell.stringCellValue
            .replace(",", ".")
            .replace(" ", "")
            .trim()
            .toDoubleOrNull()
        CellType.BLANK -> null
        else -> null
    }
}

fun normalizujJednostke(jednostka: String): String {
    return when (jednostka.lowercase().trim()) {
        "sztuka", "sztuki", "szt.", "szt" -> "szt"
        "kilogram", "kilogramy", "kg" -> "kg"
        "litr", "litry", "l" -> "litr"
        "metr", "metry", "m" -> "mb"
        "metr bieżący", "mb", "mb." -> "mb"
        "metr kwadratowy", "m2", "m²" -> "m²"
        "metr sześcienny", "m3", "m³" -> "m³"
        "tona", "tony", "t" -> "tona"
        "komplet", "kpl", "kpl." -> "komplet"
        "para", "pary" -> "para"
        "rolka", "rolki" -> "rolka"
        "worek", "worki" -> "worek"
        "opakowanie", "op.", "op" -> "opakowanie"
        else -> jednostka.trim()
    }
}

fun findEnvFile(): String {
    // Szukaj .env w bieżącym i nadrzędnym katalogu
    val dirs = listOf(
        System.getProperty("user.dir"),
        File(System.getProperty("user.dir")).parent
    )

    return dirs.firstOrNull { File(it, ".env").exists() }
        ?: System.getProperty("user.dir")
}