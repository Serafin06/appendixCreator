package pl.rafapp.marko.appendixCreator.application.usecase.raport

import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.util.RegionUtil
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.time.format.DateTimeFormatter

/**
 * Use Case: Eksport raportu do pliku Excel
 * Generuje arkusz zgodny ze wzorem "Załącznik do faktury"
 * Single Responsibility: tylko generowanie Excel
 */
class ExportToExcelUseCase : RaportExporter {
    override val rozszerzenie = "xlsx"

    private val formatData = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    operator fun invoke(dane: DaneRaportu, plik: File): Result<File> {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet(unikalnaNazwa(dane.budynek.ulica, mutableSetOf()))
            val styles = createStyles(workbook)

            dodajNaglowek(sheet, styles, dane)
            dodajNaglowekTabeli(sheet, styles)
            val lastDataRow = dodajWiersze(sheet, styles, dane)
            dodajSumowanie(sheet, styles, dane, lastDataRow)
            ustawSzerokosci(sheet)
            ustawStrone(sheet)

            plik.outputStream().use { workbook.write(it) }
            workbook.close()
            Result.success(plik)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun invokeMulti(raporty: List<DaneRaportu>, plik: File): Result<File> {
        return try {
            val workbook = XSSFWorkbook()
            val styles = createStyles(workbook)
            val uzyte = mutableSetOf<String>()
            raporty.forEach { dane ->
                val sheet = workbook.createSheet(unikalnaNazwa(dane.budynek.ulica, uzyte))
                dodajNaglowek(sheet, styles, dane)
                dodajNaglowekTabeli(sheet, styles)
                val lastDataRow = dodajWiersze(sheet, styles, dane)
                dodajSumowanie(sheet, styles, dane, lastDataRow)
                ustawSzerokosci(sheet)
                ustawStrone(sheet)
            }
            plik.outputStream().use { workbook.write(it) }
            workbook.close()
            Result.success(plik)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun eksportuj(raporty: List<DaneRaportu>, plik: File) = invokeMulti(raporty, plik)

    /** Zestawienie zbiorcze: jeden arkusz, wiersz na budynek + wiersz "Razem". */
    override fun eksportujZbiorczy(dane: DaneRaportuZbiorczego, plik: File): Result<File> = runCatching {
        val wb: Workbook = XSSFWorkbook()
        wb.use {
            val styles = createStyles(wb)
            val sheet = wb.createSheet("Zbiorcze")

            sheet.createRow(1).createCell(0).apply {
                setCellValue("Zestawienie zbiorcze za %02d.%d".format(dane.miesiac, dane.rok))
                cellStyle = styles.naglowekTytul
            }

            val naglowek = sheet.createRow(3)
            NAGLOWKI_ZBIORCZE.forEachIndexed { i, t ->
                naglowek.createCell(i).apply { setCellValue(t); cellStyle = styles.naglowekKolumna }
            }

            var r = 4
            dane.wierszeBudynkow.forEach { w ->
                val row = sheet.createRow(r++)
                row.createCell(0).apply { setCellValue(w.budynek.ulica); cellStyle = styles.dane }
                w.kwoty().forEachIndexed { i, v ->
                    row.createCell(i + 1).apply { setCellValue(v); cellStyle = styles.kwota }
                }
            }

            val razem = sheet.createRow(r)
            razem.createCell(0).apply { setCellValue("Razem"); cellStyle = styles.dane }
            dane.kwotyRazem().forEachIndexed { i, v ->
                razem.createCell(i + 1).apply { setCellValue(v); cellStyle = styles.kwotaSuma }
            }

            sheet.setColumnWidth(0, 8000)
            (1..9).forEach { sheet.setColumnWidth(it, 3200) }

            plik.outputStream().use { wb.write(it) }
            plik
        }
    }

    /** Nazwa zakładki: bez znaków zabronionych, max 31 znaków, unikalna (to wywalało eksport). */
    private fun unikalnaNazwa(ulica: String, uzyte: MutableSet<String>): String {
        val baza = ulica.replace(Regex("[\\\\/?*\\[\\]:]"), "-").take(28).ifBlank { "Arkusz" }
        var nazwa = baza
        var n = 2
        while (!uzyte.add(nazwa.lowercase())) nazwa = "$baza (${n++})"
        return nazwa
    }

    // === Wiersz 1 pusty, wiersze 2-5: nagłówek tekstowy ===
    private fun dodajNaglowek(sheet: Sheet, styles: ExcelStyles, dane: DaneRaportu) {
        sheet.createRow(0) // row 1 - pusta

        // Row 2: "Załącznik do faktury nr X/YYYY" → D2:G2
        sheet.createRow(1).also { row ->
            row.createCell(3).apply {
                setCellValue("Załącznik do faktury nr ${dane.numerFaktury}")
                cellStyle = styles.naglowekTytul
            }
            sheet.addMergedRegion(CellRangeAddress(1, 1, 3, 6))
        }

        // Row 3: firma → D3:K3
        sheet.createRow(2).also { row ->
            row.createCell(3).apply {
                setCellValue("Kalkulacja kosztów czynności wykonanych przez F.H.U. Marko Marek Grabowski")
                cellStyle = styles.naglowekInfo
            }
            sheet.addMergedRegion(CellRangeAddress(2, 2, 3, 10))
        }

        // Row 4: budynek → D4:J4
        sheet.createRow(3).also { row ->
            row.createCell(3).apply {
                setCellValue("Dotyczy ul. ${dane.budynek.ulica}")
                cellStyle = styles.naglowekInfo
            }
            sheet.addMergedRegion(CellRangeAddress(3, 3, 3, 9))
        }

        // Row 5: stawka → D5:I5
        sheet.createRow(4).also { row ->
            row.createCell(3).apply {
                setCellValue("Stawka roboczogodziny ${dane.stawkaRoboczogodziny.toInt()} zł")
                cellStyle = styles.naglowekInfo
            }
            sheet.addMergedRegion(CellRangeAddress(4, 4, 3, 8))
        }

        // Rows 6-8: puste
        (5..7).forEach { sheet.createRow(it) }
    }

    // === Wiersze 9-10: nagłówek tabeli (dwupoziomowy) ===
    private fun dodajNaglowekTabeli(sheet: Sheet, styles: ExcelStyles) {
        val row9 = sheet.createRow(8)
        val row10 = sheet.createRow(9)

        fun nagl(col: Int, text: String, style: CellStyle = styles.naglowekGrupa) =
            row9.createCell(col).apply { setCellValue(text); cellStyle = style }
        fun subnagl(col: Int, text: String) =
            row10.createCell(col).apply { setCellValue(text); cellStyle = styles.naglowekKolumna }

        // wiersz 9: nagłówki grup i nagłówki scalane w dół
        nagl(0, "LP", styles.naglowekKolumna)
        nagl(1, "Data", styles.naglowekKolumna)
        nagl(2, "Usługa", styles.naglowekKolumna)
        nagl(3, "Robocizna")
        nagl(5, "Materiał")
        nagl(9, "Koszt zakupu 8%", styles.naglowekKolumna)
        nagl(10, "Transport/sprzęt")
        nagl(13, "Suma", styles.naglowekKolumna)

        // wiersz 10: podkolumny
        subnagl(3, "Ilość godzin"); subnagl(4, "Wartość")
        subnagl(5, "Nazwa"); subnagl(6, "Ilość"); subnagl(7, "Jednostka"); subnagl(8, "Wartość")
        subnagl(10, "Nazwa"); subnagl(11, "Ilość"); subnagl(12, "Wartość")

        // puste komórki w obu wierszach, żeby ramka objęła cały scalony obszar
        (0..13).forEach { c ->
            if (row9.getCell(c) == null) row9.createCell(c).cellStyle = styles.naglowekGrupa
            if (row10.getCell(c) == null) row10.createCell(c).cellStyle = styles.naglowekKolumna
        }

        // scalanie dopiero na końcu, gdy wszystkie komórki już istnieją
        scal(sheet, 8, 8, 3, 4)    // Robocizna
        scal(sheet, 8, 8, 5, 8)    // Materiał
        scal(sheet, 8, 8, 10, 12)  // Transport/sprzęt
        listOf(0, 1, 2, 9, 13).forEach { scal(sheet, 8, 9, it) }
    }

    // === Dane od wiersza 11 (index 10) ===
    // Każda praca zajmuje tyle wierszy ile ma materiałów (min. 1)
    // Zwraca indeks ostatniego wiersza danych (do SUM)
    private fun dodajWiersze(sheet: Sheet, styles: ExcelStyles, dane: DaneRaportu): Int {
        var r = 10
        dane.wiersze.forEachIndexed { idx, w ->
            val n = maxOf(w.materialy.size, 1)
            val dojazd = w.kosztDojazdu > 0
            val rows = (0 until n).map { sheet.createRow(r + it) }

            fun tekst(row: Row, col: Int, v: String, st: CellStyle) =
                row.createCell(col).apply { setCellValue(v); cellStyle = st }
            fun liczba(row: Row, col: Int, v: Double, st: CellStyle) =
                row.createCell(col).apply { setCellValue(v); cellStyle = st }

            // kolumny scalone pionowo (wartość w pierwszym wierszu pracy)
            val first = rows[0]
            tekst(first, 0, (idx + 1).toString(), styles.srodekGora)
            tekst(first, 1, w.data.format(formatData), styles.srodekGora)
            tekst(first, 2, w.opis, styles.lewaDol)
            tekst(first, 3, w.roboczogodziny.toString(), styles.srodekDol)
            liczba(first, 4, w.kosztRobocizny, styles.prawaDol)
            tekst(first, 10, if (dojazd) "dojazd" else "", styles.srodekDol)
            tekst(first, 11, if (dojazd) "1" else "", styles.srodekDol)
            if (dojazd) liczba(first, 12, w.kosztDojazdu, styles.prawaDol) else tekst(first, 12, "", styles.prawaDol)
            liczba(first, 13, sumaPracy(w), styles.prawaDol)
            listOf(0, 1, 2, 3, 4, 10, 11, 12, 13).forEach { scal(sheet, r, r + n - 1, it) }

            // materiały: po jednym wierszu
            rows.forEachIndexed { i, row ->
                val m = w.materialy.getOrNull(i)
                tekst(row, 5, m?.nazwa ?: "", styles.lewaDol)
                if (m != null) liczba(row, 6, m.ilosc, styles.srodekDol) else tekst(row, 6, "", styles.srodekDol)
                tekst(row, 7, m?.jednostka ?: "", styles.srodekDol)
                if (m != null) {
                    liczba(row, 8, m.kosztCalkowity, styles.prawaDol)
                    liczba(row, 9, m.kosztCalkowity * NARZUT_MATERIALOW, styles.prawaDol)
                } else {
                    tekst(row, 8, "", styles.prawaDol); tekst(row, 9, "", styles.prawaDol)
                }
            }
            r += n
        }
        return r - 1
    }

    private fun sumaPracy(w: WierszRaportu) =
        w.kosztRobocizny + w.kosztDojazdu + w.materialy.sumOf { it.kosztCalkowity } * (1 + NARZUT_MATERIALOW)

    private companion object { const val NARZUT_MATERIALOW = 0.08 }

    // === Wiersz sumy pod tabelą ===
    private fun dodajSumowanie(sheet: Sheet, styles: ExcelStyles, dane: DaneRaportu, lastDataRowIdx: Int) {
        val sumRow = sheet.createRow(lastDataRowIdx + 1)
        // Suma kolumny N (index 13)
        val firstDataRow = 11 // Excel 1-indexed
        val lastDataRow = lastDataRowIdx + 1 // Excel 1-indexed

        sumRow.createCell(13).apply {
            cellFormula = "SUM(N${firstDataRow}:N${lastDataRow})"
            cellStyle = styles.kwotaSuma
        }
    }

    private fun ustawSzerokosci(sheet: Sheet) {
        // Szerokości w jednostkach POI (1/256 znaku)
        val widths = mapOf(
            0 to 850,    // A - LP
            1 to 3400,   // B - Data
            2 to 10160,  // C - Usługa
            3 to 1650,   // D - Ilość godz
            4 to 2020,   // E - Wartość rob.
            5 to 5230,   // F - Nazwa mat.
            6 to 1250,   // G - Ilość mat.
            7 to 2445,   // H - Jednostka
            8 to 2020,   // I - Wartość mat.
            9 to 1905,   // J - Koszt zakupu 8%
            10 to 2390,  // K - Nazwa transp.
            11 to 1250,  // L - Ilość transp.
            12 to 2020,  // M - Wartość transp.
            13 to 2275   // N - Suma
        )
        widths.forEach { (col, width) -> sheet.setColumnWidth(col, width) }
    }

    private fun createStyles(workbook: Workbook): ExcelStyles {
        val fmt = workbook.createDataFormat()
        val kwotaFmt = fmt.getFormat("#,##0.00")


        fun font(bold: Boolean = false, size: Short = 8) =
            workbook.createFont().apply { fontHeightInPoints = 8 } // bez pogrubień, jak w PDF

        fun baseStyle(
            font: Font? = null,
            bg: Short? = null,
            hAlign: HorizontalAlignment = HorizontalAlignment.LEFT,
            vAlign: VerticalAlignment = VerticalAlignment.CENTER,
            borders: Boolean = true,
            format: Short? = null,
            wrap: Boolean = false
        ) = workbook.createCellStyle().apply {
            font?.let { setFont(it) }
            bg?.let {
                fillForegroundColor = it
                fillPattern = FillPatternType.SOLID_FOREGROUND
            }
            alignment = hAlign
            verticalAlignment = vAlign
            if (borders) {
                borderTop = BorderStyle.THIN
                borderBottom = BorderStyle.THIN
                borderLeft = BorderStyle.THIN
                borderRight = BorderStyle.THIN
            }
            format?.let { dataFormat = it }
            wrapText = wrap
        }


        return ExcelStyles(
            naglowekTytul = baseStyle(font(bold = true, size = 11), borders = false),
            naglowekInfo = baseStyle(font(bold = false, size = 10), borders = false),
            naglowekGrupa = baseStyle(
                font(bold = true, size = 10),

                hAlign = HorizontalAlignment.CENTER
            ),
            naglowekKolumna = baseStyle(
                font(bold = true, size = 9),

                hAlign = HorizontalAlignment.CENTER,
                wrap = true
            ),
            dane = baseStyle(font(size = 9)),
            daneWrap = baseStyle(font(size = 9), wrap = true),
            liczba = baseStyle(font(size = 9), hAlign = HorizontalAlignment.CENTER),
            kwota = baseStyle(font(size = 9), hAlign = HorizontalAlignment.RIGHT, format = kwotaFmt),
            kwotaSuma = baseStyle(
                font(bold = true, size = 9),
                hAlign = HorizontalAlignment.RIGHT,
                vAlign = VerticalAlignment.BOTTOM,
                format = kwotaFmt
            ),
            srodekGora = baseStyle(font(), hAlign = HorizontalAlignment.CENTER, vAlign = VerticalAlignment.TOP),
            lewaDol = baseStyle(font(), vAlign = VerticalAlignment.BOTTOM, wrap = true),
            srodekDol = baseStyle(font(), hAlign = HorizontalAlignment.CENTER, vAlign = VerticalAlignment.BOTTOM, wrap = true),
            prawaDol = baseStyle(font(), hAlign = HorizontalAlignment.RIGHT, vAlign = VerticalAlignment.BOTTOM, format = kwotaFmt)
        )
    }

    /** Scala zakres i rysuje ramkę wokół całego obszaru (POI sam tego nie robi). */
    private fun scal(sheet: Sheet, r1: Int, r2: Int, c1: Int, c2: Int = c1) {
        if (r1 == r2 && c1 == c2) return
        val reg = CellRangeAddress(r1, r2, c1, c2)
        sheet.addMergedRegion(reg)
        RegionUtil.setBorderTop(BorderStyle.THIN, reg, sheet)
        RegionUtil.setBorderBottom(BorderStyle.THIN, reg, sheet)
        RegionUtil.setBorderLeft(BorderStyle.THIN, reg, sheet)
        RegionUtil.setBorderRight(BorderStyle.THIN, reg, sheet)
    }

    private fun ustawStrone(sheet: Sheet) {
        sheet.printSetup.apply { landscape = true; paperSize = PrintSetup.A4_PAPERSIZE; fitWidth = 1; fitHeight = 0 }
        sheet.fitToPage = true
        sheet.setMargin(Sheet.LeftMargin, 0.28); sheet.setMargin(Sheet.RightMargin, 0.28)
        sheet.setMargin(Sheet.TopMargin, 0.28); sheet.setMargin(Sheet.BottomMargin, 0.28)
        sheet.setRepeatingRows(CellRangeAddress(8, 9, -1, -1))
    }
}

data class ExcelStyles(
    val naglowekTytul: CellStyle,
    val naglowekInfo: CellStyle,
    val naglowekGrupa: CellStyle,
    val naglowekKolumna: CellStyle,
    val dane: CellStyle,
    val daneWrap: CellStyle,
    val liczba: CellStyle,
    val kwota: CellStyle,
    val kwotaSuma: CellStyle,
    val srodekGora: CellStyle,
    val lewaDol: CellStyle,
    val srodekDol: CellStyle,
    val prawaDol: CellStyle
)