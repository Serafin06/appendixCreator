package pl.rafapp.marko.appendixCreator.application.usecase.raport

import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Use Case: Eksport raportu do pliku Excel
 * Single Responsibility: tylko generowanie Excel
 */
class ExportToExcelUseCase {

    private val formatData = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val formatKwota = "###,##0.00"

    operator fun invoke(dane: DaneRaportu, plik: File): Result<File> {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Raport")

            // Style
            val styles = createStyles(workbook)

            var rowNum = 0

            // === NAGŁÓWEK ===
            rowNum = dodajNaglowek(sheet, styles, dane, rowNum)

            // === TABELA ===
            rowNum = dodajNaglowekTabeli(sheet, styles, rowNum)
            rowNum = dodajWierszeTabeli(sheet, styles, dane, rowNum)

            // === PODSUMOWANIE ===
            rowNum = dodajPodsumowanie(sheet, styles, dane, rowNum)

            // === STOPKA ===
            dodajStopke(sheet, styles, dane, rowNum)

            // Szerokości kolumn
            sheet.setColumnWidth(0, 3500)   // Data
            sheet.setColumnWidth(1, 10000)  // Opis
            sheet.setColumnWidth(2, 3000)   // Roboczogodziny
            sheet.setColumnWidth(3, 3500)   // Stawka
            sheet.setColumnWidth(4, 4000)   // Koszt robocizny
            sheet.setColumnWidth(5, 3500)   // Dojazd
            sheet.setColumnWidth(6, 6000)   // Materiały
            sheet.setColumnWidth(7, 4000)   // Koszt materiałów
            sheet.setColumnWidth(8, 4000)   // Netto
            sheet.setColumnWidth(9, 2500)   // VAT
            sheet.setColumnWidth(10, 4000)  // Brutto

            // Zapisz plik
            plik.outputStream().use { workbook.write(it) }
            workbook.close()

            Result.success(plik)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun dodajNaglowek(
        sheet: Sheet,
        styles: ExcelStyles,
        dane: DaneRaportu,
        startRow: Int
    ): Int {
        var rowNum = startRow
        val nazwaeMiesiaca = java.time.Month.of(dane.miesiac)
            .getDisplayName(TextStyle.FULL_STANDALONE, Locale("pl"))
            .replaceFirstChar { it.uppercase() }

        // Puste miejsce na logo/nagłówek firmowy
        repeat(3) {
            sheet.createRow(rowNum++)
        }

        // Tytuł
        val tytulRow = sheet.createRow(rowNum++)
        val tytulCell = tytulRow.createCell(0)
        tytulCell.setCellValue("ZESTAWIENIE PRAC BUDOWLANYCH")
        tytulCell.cellStyle = styles.tytul
        sheet.addMergedRegion(CellRangeAddress(tytulRow.rowNum, tytulRow.rowNum, 0, 10))

        // Okres
        val okresRow = sheet.createRow(rowNum++)
        val okresCell = okresRow.createCell(0)
        okresCell.setCellValue("Okres: $nazwaeMiesiaca ${dane.rok}")
        okresCell.cellStyle = styles.podtytul
        sheet.addMergedRegion(CellRangeAddress(okresRow.rowNum, okresRow.rowNum, 0, 10))

        // Budynek
        val budynekRow = sheet.createRow(rowNum++)
        val budynekCell = budynekRow.createCell(0)
        budynekCell.setCellValue("Adres: ${dane.budynek.pelnyAdres}")
        budynekCell.cellStyle = styles.podtytul
        sheet.addMergedRegion(CellRangeAddress(budynekRow.rowNum, budynekRow.rowNum, 0, 10))

        // Stawka
        val stawkaRow = sheet.createRow(rowNum++)
        val stawkaCell = stawkaRow.createCell(0)
        stawkaCell.setCellValue("Stawka za roboczogodzinę: ${formatujKwote(dane.stawkaRoboczogodziny)} zł")
        stawkaCell.cellStyle = styles.info
        sheet.addMergedRegion(CellRangeAddress(stawkaRow.rowNum, stawkaRow.rowNum, 0, 10))

        // Pusta linia
        sheet.createRow(rowNum++)

        return rowNum
    }

    private fun dodajNaglowekTabeli(
        sheet: Sheet,
        styles: ExcelStyles,
        startRow: Int
    ): Int {
        val row = sheet.createRow(startRow)
        val headers = listOf(
            "Data",
            "Opis pracy",
            "Roboczogodziny",
            "Stawka (zł/h)",
            "Koszt robocizny (zł)",
            "Koszt dojazdu (zł)",
            "Materiały",
            "Koszt materiałów (zł)",
            "Netto (zł)",
            "VAT (%)",
            "Brutto (zł)"
        )

        headers.forEachIndexed { i, header ->
            val cell = row.createCell(i)
            cell.setCellValue(header)
            cell.cellStyle = styles.naglowekTabeli
        }

        return startRow + 1
    }

    private fun dodajWierszeTabeli(
        sheet: Sheet,
        styles: ExcelStyles,
        dane: DaneRaportu,
        startRow: Int
    ): Int {
        var rowNum = startRow

        dane.wiersze.forEach { wiersz ->
            val row = sheet.createRow(rowNum++)
            val isOdd = (rowNum % 2 == 0)
            val stylDanych = if (isOdd) styles.daneParzyste else styles.daneNieparzyste
            val stylKwoty = if (isOdd) styles.kwotaParzysta else styles.kwotaNieparzysta

            // Data
            row.createCell(0).apply {
                setCellValue(wiersz.data.format(formatData))
                cellStyle = stylDanych
            }

            // Opis
            row.createCell(1).apply {
                setCellValue(wiersz.opis)
                cellStyle = stylDanych
            }

            // Roboczogodziny
            row.createCell(2).apply {
                setCellValue(wiersz.roboczogodziny.toDouble())
                cellStyle = stylDanych
            }

            // Stawka
            row.createCell(3).apply {
                setCellValue(wiersz.stawkaRoboczogodziny)
                cellStyle = stylKwoty
            }

            // Koszt robocizny
            row.createCell(4).apply {
                setCellValue(wiersz.kosztRobocizny)
                cellStyle = stylKwoty
            }

            // Koszt dojazdu
            row.createCell(5).apply {
                setCellValue(wiersz.kosztDojazdu)
                cellStyle = stylKwoty
            }

            // Materiały - lista w jednej komórce
            row.createCell(6).apply {
                val materialyText = if (wiersz.materialy.isEmpty()) {
                    "-"
                } else {
                    wiersz.materialy.joinToString("\n") { m ->
                        "${m.nazwa}: ${m.ilosc} ${m.jednostka} × ${formatujKwote(m.cenaZaJednostke)} zł"
                    }
                }
                setCellValue(materialyText)
                cellStyle = stylDanych
            }

            // Koszt materiałów
            row.createCell(7).apply {
                setCellValue(wiersz.kosztMaterialow)
                cellStyle = stylKwoty
            }

            // Netto
            row.createCell(8).apply {
                setCellValue(wiersz.kosztNetto)
                cellStyle = stylKwoty
            }

            // VAT
            row.createCell(9).apply {
                setCellValue("${wiersz.vat}%")
                cellStyle = stylDanych
            }

            // Brutto
            row.createCell(10).apply {
                setCellValue(wiersz.kosztBrutto)
                cellStyle = stylKwoty
            }

            // Ustaw wysokość wiersza jeśli są materiały
            if (wiersz.materialy.size > 1) {
                row.heightInPoints = (wiersz.materialy.size * 15).toFloat()
            }
        }

        return rowNum
    }

    private fun dodajPodsumowanie(
        sheet: Sheet,
        styles: ExcelStyles,
        dane: DaneRaportu,
        startRow: Int
    ): Int {
        var rowNum = startRow

        // Pusta linia
        sheet.createRow(rowNum++)

        // Wiersz podsumowania
        val row = sheet.createRow(rowNum++)

        row.createCell(0).apply {
            setCellValue("PODSUMOWANIE")
            cellStyle = styles.podsumowanieLabel
        }
        sheet.addMergedRegion(CellRangeAddress(row.rowNum, row.rowNum, 0, 1))

        // Suma godzin
        row.createCell(2).apply {
            setCellValue(dane.sumaRoboczogodzin.toDouble())
            cellStyle = styles.podsumowanieWartosc
        }

        // Suma kosztów robocizny
        row.createCell(4).apply {
            setCellValue(dane.sumaKosztowRobocizny)
            cellStyle = styles.podsumowanieKwota
        }

        // Suma kosztów dojazdu
        row.createCell(5).apply {
            setCellValue(dane.sumaKosztowDojazdu)
            cellStyle = styles.podsumowanieKwota
        }

        // Suma kosztów materiałów
        row.createCell(7).apply {
            setCellValue(dane.sumaKosztowMaterialow)
            cellStyle = styles.podsumowanieKwota
        }

        // Suma netto
        row.createCell(8).apply {
            setCellValue(dane.sumaNetto)
            cellStyle = styles.podsumowanieKwota
        }

        // Suma brutto
        row.createCell(10).apply {
            setCellValue(dane.sumaBrutto)
            cellStyle = styles.podsumowanieKwota
        }

        // Pusta linia
        sheet.createRow(rowNum++)

        // Blok VAT
        val vatRow = sheet.createRow(rowNum++)
        vatRow.createCell(0).apply {
            setCellValue("Wartość netto:")
            cellStyle = styles.vatLabel
        }
        vatRow.createCell(2).apply {
            setCellValue(dane.sumaNetto)
            cellStyle = styles.vatKwota
        }
        sheet.addMergedRegion(CellRangeAddress(vatRow.rowNum, vatRow.rowNum, 0, 1))

        val vatRow2 = sheet.createRow(rowNum++)
        vatRow2.createCell(0).apply {
            setCellValue("Kwota VAT:")
            cellStyle = styles.vatLabel
        }
        vatRow2.createCell(2).apply {
            setCellValue(dane.sumaVat)
            cellStyle = styles.vatKwota
        }
        sheet.addMergedRegion(CellRangeAddress(vatRow2.rowNum, vatRow2.rowNum, 0, 1))

        val bruttoRow = sheet.createRow(rowNum++)
        bruttoRow.createCell(0).apply {
            setCellValue("Wartość brutto:")
            cellStyle = styles.bruttoLabel
        }
        bruttoRow.createCell(2).apply {
            setCellValue(dane.sumaBrutto)
            cellStyle = styles.bruttoKwota
        }
        sheet.addMergedRegion(CellRangeAddress(bruttoRow.rowNum, bruttoRow.rowNum, 0, 1))

        return rowNum
    }

    private fun dodajStopke(
        sheet: Sheet,
        styles: ExcelStyles,
        dane: DaneRaportu,
        startRow: Int
    ) {
        var rowNum = startRow + 2

        // Pusta linia na podpis
        repeat(3) { sheet.createRow(rowNum++) }

        val podpisRow = sheet.createRow(rowNum++)
        podpisRow.createCell(8).apply {
            setCellValue("Podpis i pieczątka")
            cellStyle = styles.info
        }
        sheet.addMergedRegion(CellRangeAddress(podpisRow.rowNum, podpisRow.rowNum, 8, 10))
    }

    private fun createStyles(workbook: Workbook): ExcelStyles {
        val fmt = workbook.createDataFormat()

        fun font(bold: Boolean = false, size: Short = 11, color: Short = IndexedColors.BLACK.index) =
            workbook.createFont().apply {
                this.bold = bold
                this.fontHeightInPoints = size
                this.color = color
            }

        fun style(
            font: Font? = null,
            bg: Short? = null,
            alignment: HorizontalAlignment = HorizontalAlignment.LEFT,
            border: Boolean = false,
            format: Short? = null,
            wrap: Boolean = false
        ) = workbook.createCellStyle().apply {
            font?.let { setFont(it) }
            bg?.let {
                fillForegroundColor = it
                fillPattern = FillPatternType.SOLID_FOREGROUND
            }
            this.alignment = alignment
            if (border) {
                borderBottom = BorderStyle.THIN
                borderTop = BorderStyle.THIN
                borderLeft = BorderStyle.THIN
                borderRight = BorderStyle.THIN
            }
            format?.let { dataFormat = it }
            this.wrapText = wrap
        }

        val kwotaFmt = fmt.getFormat(formatKwota)
        val ciemnyNiebieski = IndexedColors.DARK_BLUE.index
        val jasnyNiebieski = IndexedColors.PALE_BLUE.index
        val szary = IndexedColors.GREY_25_PERCENT.index
        val bialy = IndexedColors.WHITE.index
        val zolty = IndexedColors.LIGHT_YELLOW.index

        return ExcelStyles(
            tytul = style(font(true, 16), alignment = HorizontalAlignment.CENTER),
            podtytul = style(font(true, 12), alignment = HorizontalAlignment.CENTER),
            info = style(font(false, 10), alignment = HorizontalAlignment.LEFT),
            naglowekTabeli = style(
                font(true, 10, IndexedColors.WHITE.index),
                ciemnyNiebieski,
                HorizontalAlignment.CENTER,
                border = true
            ),
            daneParzyste = style(null, bialy, border = true, wrap = true),
            daneNieparzyste = style(null, szary, border = true, wrap = true),
            kwotaParzysta = style(null, bialy, HorizontalAlignment.RIGHT, true, kwotaFmt),
            kwotaNieparzysta = style(null, szary, HorizontalAlignment.RIGHT, true, kwotaFmt),
            podsumowanieLabel = style(font(true, 11), jasnyNiebieski, border = true),
            podsumowanieWartosc = style(font(true, 11), jasnyNiebieski, HorizontalAlignment.CENTER, true),
            podsumowanieKwota = style(font(true, 11), jasnyNiebieski, HorizontalAlignment.RIGHT, true, kwotaFmt),
            vatLabel = style(font(false, 10), alignment = HorizontalAlignment.RIGHT),
            vatKwota = style(font(false, 10), alignment = HorizontalAlignment.RIGHT, format = kwotaFmt),
            bruttoLabel = style(font(true, 11), zolty, HorizontalAlignment.RIGHT, border = true),
            bruttoKwota = style(font(true, 11), zolty, HorizontalAlignment.RIGHT, true, kwotaFmt)
        )
    }

    private fun formatujKwote(kwota: Double): String {
        return String.format("%.2f", kwota).replace(".", ",")
    }
}

data class ExcelStyles(
    val tytul: CellStyle,
    val podtytul: CellStyle,
    val info: CellStyle,
    val naglowekTabeli: CellStyle,
    val daneParzyste: CellStyle,
    val daneNieparzyste: CellStyle,
    val kwotaParzysta: CellStyle,
    val kwotaNieparzysta: CellStyle,
    val podsumowanieLabel: CellStyle,
    val podsumowanieWartosc: CellStyle,
    val podsumowanieKwota: CellStyle,
    val vatLabel: CellStyle,
    val vatKwota: CellStyle,
    val bruttoLabel: CellStyle,
    val bruttoKwota: CellStyle
)