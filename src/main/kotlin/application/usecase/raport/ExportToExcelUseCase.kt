package pl.rafapp.marko.appendixCreator.application.usecase.raport

import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.time.format.DateTimeFormatter

/**
 * Use Case: Eksport raportu do pliku Excel
 * Generuje arkusz zgodny ze wzorem "Załącznik do faktury"
 * Single Responsibility: tylko generowanie Excel
 */
class ExportToExcelUseCase {

    private val formatData = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    operator fun invoke(dane: DaneRaportu, plik: File): Result<File> {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet(dane.budynek.ulica)
            val styles = createStyles(workbook)

            dodajNaglowek(sheet, styles, dane)
            dodajNaglowekTabeli(sheet, styles)
            val lastDataRow = dodajWiersze(sheet, styles, dane)
            dodajSumowanie(sheet, styles, dane, lastDataRow)
            ustawSzerokosci(sheet)

            plik.outputStream().use { workbook.write(it) }
            workbook.close()
            Result.success(plik)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
        // Row 9: grupy kolumn
        val row9 = sheet.createRow(8)
        fun nagl(row: Row, col: Int, text: String) =
            row.createCell(col).apply { setCellValue(text); cellStyle = styles.naglowekGrupa }

        // Puste A-C (LP, Data, Usługa) - połączone w dół z row10 osobno
        nagl(row9, 3, "Robocizna")
        sheet.addMergedRegion(CellRangeAddress(8, 8, 3, 4))
        nagl(row9, 5, "Materiał")
        sheet.addMergedRegion(CellRangeAddress(8, 8, 5, 8))
        nagl(row9, 9, "Koszt zakupu 8%")
        sheet.addMergedRegion(CellRangeAddress(8, 9, 9, 9)) // J9:J10
        nagl(row9, 10, "Transport/sprzęt")
        sheet.addMergedRegion(CellRangeAddress(8, 8, 10, 12))
        nagl(row9, 13, "Suma")
        sheet.addMergedRegion(CellRangeAddress(8, 9, 13, 13)) // N9:N10

        // Row 10: podkolumny
        val row10 = sheet.createRow(9)
        fun subnagl(col: Int, text: String) =
            row10.createCell(col).apply { setCellValue(text); cellStyle = styles.naglowekKolumna }

        subnagl(0, "LP")
        subnagl(1, "Data")
        subnagl(2, "Usługa")
        subnagl(3, "Ilość godzin")
        subnagl(4, "Wartość")
        subnagl(5, "Nazwa")
        subnagl(6, "Ilość")
        subnagl(7, "Jednostka")
        subnagl(8, "Wartość")
        // col 9 (J) - merged z row9
        subnagl(10, "Nazwa")
        subnagl(11, "Ilość")
        subnagl(12, "Wartość")
        // col 13 (N) - merged z row9

        // LP/Data/Usługa w row9 mergujemy z row10
        sheet.addMergedRegion(CellRangeAddress(8, 9, 0, 0))
        sheet.addMergedRegion(CellRangeAddress(8, 9, 1, 1))
        sheet.addMergedRegion(CellRangeAddress(8, 9, 2, 2))
        row9.createCell(0).apply { setCellValue("LP"); cellStyle = styles.naglowekKolumna }
        row9.createCell(1).apply { setCellValue("Data"); cellStyle = styles.naglowekKolumna }
        row9.createCell(2).apply { setCellValue("Usługa"); cellStyle = styles.naglowekKolumna }
    }

    // === Dane od wiersza 11 (index 10) ===
    // Każda praca zajmuje tyle wierszy ile ma materiałów (min. 1)
    // Zwraca indeks ostatniego wiersza danych (do SUM)
    private fun dodajWiersze(sheet: Sheet, styles: ExcelStyles, dane: DaneRaportu): Int {
        var rowIdx = 10
        var lp = 1

        dane.wiersze.forEach { wiersz ->
            val materialyCount = maxOf(wiersz.materialy.size, 1)

            // Jeśli praca ma wiele materiałów - merguj komórki LP/Data/Usługa/Robocizna/Transport/Suma
            val endRowIdx = rowIdx + materialyCount - 1

            val row = sheet.createRow(rowIdx)

            fun cell(col: Int, value: Any?, style: CellStyle) =
                row.createCell(col).apply {
                    when (value) {
                        is Double -> setCellValue(value)
                        is String -> setCellValue(value)
                        is Int -> setCellValue(value.toDouble())
                        null -> setCellValue("")
                    }
                    cellStyle = style
                }

            cell(0, lp, styles.dane)
            cell(1, wiersz.data.format(formatData), styles.dane)
            cell(2, wiersz.opis, styles.daneWrap)
            cell(3, wiersz.roboczogodziny, styles.liczba)
            cell(4, wiersz.kosztRobocizny, styles.kwota)

            // Transport/sprzęt (dojazd) w kolumnach K-M
            if (wiersz.kosztDojazdu > 0) {
                cell(10, "dojazd", styles.dane)
                cell(11, 1, styles.liczba)
                cell(12, wiersz.kosztDojazdu, styles.kwota)
            }

            // Suma wiersza
            val kosztMat = wiersz.materialy.firstOrNull()?.kosztCalkowity ?: 0.0
            val vatMat = kosztMat * 0.08
            val suma = wiersz.kosztRobocizny + wiersz.kosztDojazdu + kosztMat + vatMat
            cell(13, suma, styles.kwotaSuma)

            // VAT % w kolumnie P (index 15)
            cell(15, wiersz.vat, styles.dane)

            // Materiały
            if (wiersz.materialy.isEmpty()) {
                // Puste kolumny materiałów
                listOf(5, 6, 7, 8, 9).forEach { col ->
                    row.createCell(col).cellStyle = styles.dane
                }
            } else {
                val m = wiersz.materialy[0]
                cell(5, m.nazwa, styles.dane)
                cell(6, m.ilosc, styles.liczba)
                cell(7, m.jednostka, styles.dane)
                cell(8, m.kosztCalkowity, styles.kwota)
                cell(9, m.kosztCalkowity * 0.08, styles.kwota)
            }

            // Dodatkowe wiersze dla kolejnych materiałów
            wiersz.materialy.drop(1).forEachIndexed { i, m ->
                val extraRow = sheet.createRow(rowIdx + i + 1)
                fun extraCell(col: Int, value: Any?, style: CellStyle) =
                    extraRow.createCell(col).apply {
                        when (value) {
                            is Double -> setCellValue(value)
                            is String -> setCellValue(value)
                            null -> setCellValue("")
                        }
                        cellStyle = style
                    }
                extraCell(5, m.nazwa, styles.dane)
                extraCell(6, m.ilosc, styles.liczba)
                extraCell(7, m.jednostka, styles.dane)
                extraCell(8, m.kosztCalkowity, styles.kwota)
                extraCell(9, m.kosztCalkowity * 0.08, styles.kwota)
            }

            // Merguj komórki LP/Data/Usługa/robocizna/transport/suma jeśli >1 materiał
            if (materialyCount > 1) {
                listOf(0, 1, 2, 3, 4, 10, 11, 12, 13, 15).forEach { col ->
                    if (sheet.getRow(rowIdx)?.getCell(col) != null || col in listOf(0,1,2,3,4,13,15)) {
                        sheet.addMergedRegion(CellRangeAddress(rowIdx, endRowIdx, col, col))
                    }
                }
            }

            rowIdx = endRowIdx + 1
            lp++
        }

        return rowIdx - 1 // ostatni wiersz danych (1-indexed: rowIdx)
    }

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

        fun font(bold: Boolean = false, size: Short = 10) =
            workbook.createFont().apply {
                this.bold = bold
                fontHeightInPoints = size
            }

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

        val headerBg = IndexedColors.GREY_25_PERCENT.index

        return ExcelStyles(
            naglowekTytul = baseStyle(font(bold = true, size = 11), borders = false),
            naglowekInfo = baseStyle(font(bold = false, size = 10), borders = false),
            naglowekGrupa = baseStyle(
                font(bold = true, size = 10),
                bg = headerBg,
                hAlign = HorizontalAlignment.CENTER
            ),
            naglowekKolumna = baseStyle(
                font(bold = true, size = 9),
                bg = headerBg,
                hAlign = HorizontalAlignment.CENTER,
                wrap = true
            ),
            dane = baseStyle(font(size = 9)),
            daneWrap = baseStyle(font(size = 9), wrap = true),
            liczba = baseStyle(font(size = 9), hAlign = HorizontalAlignment.CENTER),
            kwota = baseStyle(font(size = 9), hAlign = HorizontalAlignment.RIGHT, format = kwotaFmt),
            kwotaSuma = baseStyle(
                font(bold = true, size = 9),
                bg = IndexedColors.LIGHT_YELLOW.index,
                hAlign = HorizontalAlignment.RIGHT,
                format = kwotaFmt
            )
        )
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
    val kwotaSuma: CellStyle
)