package pl.rafapp.marko.appendixCreator.application.usecase.raport

import com.lowagie.text.Document
import com.lowagie.text.Element
import com.lowagie.text.Font
import com.lowagie.text.PageSize
import com.lowagie.text.Paragraph
import com.lowagie.text.Phrase
import com.lowagie.text.Rectangle
import com.lowagie.text.pdf.BaseFont
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import java.awt.Color
import java.io.File
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Use Case: eksport raportów do jednego PDF (A4 poziomo).
 * Wygląd: czarno-biały, bez wypełnień i pogrubień (oszczędność tuszu), układ jak ręczny wzór.
 * Każdy budynek zaczyna się od nowej strony.
 * Single Responsibility: tylko renderowanie PDF, dane dostaje gotowe z GenerujRaportUseCase.
 */
class ExportToPdfUseCase : RaportExporter {
    override val rozszerzenie = "pdf"

    private val formatData = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val symbole = DecimalFormatSymbols(Locale("pl", "PL"))
    private val formatKwoty = DecimalFormat("#,##0.00", symbole)
    private val formatIlosci = DecimalFormat("0.##", symbole)

    // CP1250 = polskie znaki w standardowej czcionce Helvetica
    private val font: Font by lazy {
        Font(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1250, BaseFont.NOT_EMBEDDED), 8f, Font.NORMAL, Color.BLACK)
    }

    // proporcje kolumn A-N jak we wzorze
    private val szerokosci = floatArrayOf(3.3f, 13.3f, 39.7f, 6.4f, 7.9f, 20.4f, 4.9f, 9.6f, 7.9f, 7.4f, 9.3f, 4.9f, 7.9f, 8.9f, 5.0f)

    override fun eksportuj(raporty: List<DaneRaportu>, plik: File): Result<File> = runCatching {
        plik.outputStream().use { out ->
            val doc = Document(PageSize.A4.rotate(), 20f, 20f, 20f, 20f)
            PdfWriter.getInstance(doc, out)
            doc.open()
            raporty.forEachIndexed { i, dane ->
                if (i > 0) doc.newPage()
                dodajNaglowek(doc, dane)
                doc.add(zbudujTabele(dane))
            }
            doc.close()
        }
        plik
    }

    override fun eksportujZbiorczy(dane: DaneRaportuZbiorczego, plik: File): Result<File> = runCatching {
        plik.outputStream().use { out ->
            val doc = Document(PageSize.A4.rotate(), 20f, 20f, 20f, 20f)
            PdfWriter.getInstance(doc, out)
            doc.open()
            doc.add(Paragraph("Zestawienie zbiorcze za %02d.%d".format(dane.miesiac, dane.rok), font))
            doc.add(Paragraph(" ", font))

            val t = PdfPTable(10).apply {
                widthPercentage = 100f
                setWidths(floatArrayOf(22f, 8f, 8f, 8f, 8f, 8f, 8f, 9f, 9f, 9f))
                headerRows = 1
            }
            NAGLOWKI_ZBIORCZE.forEach { t.addCell(c(it)) }
            dane.wierszeBudynkow.forEach { w ->
                t.addCell(c(w.budynek.ulica, Element.ALIGN_LEFT))
                w.kwoty().forEach { t.addCell(c(kwota(it), Element.ALIGN_RIGHT)) }
            }
            t.addCell(c("Razem", Element.ALIGN_LEFT))
            dane.kwotyRazem().forEach { t.addCell(c(kwota(it), Element.ALIGN_RIGHT)) }

            doc.add(t)
            doc.close()
        }
        plik
    }

    private fun dodajNaglowek(doc: Document, dane: DaneRaportu) {
        listOf(
            "Załącznik do faktury nr ${dane.numerFaktury}",
            "Kalkulacja kosztów czynności wykonanych przez F.H.U. Marko Marek Grabowski",
            "Dotyczy ul. ${dane.budynek.ulica}",
            "Stawka roboczogodziny ${dane.stawkaRoboczogodziny.toInt()} zł"
        ).forEach {
            doc.add(Paragraph(it, font).apply {
                alignment = Element.ALIGN_LEFT // Wyrównanie tekstu do lewej
                indentationLeft = 275f // Przesunięcie całego bloku w prawo (wartość w punktach, dostosuj do potrzeb)
            })
        }
        doc.add(Paragraph(" ", font))
    }

    private fun zbudujTabele(dane: DaneRaportu): PdfPTable {
        val t = PdfPTable(szerokosci.size).apply {
            widthPercentage = 100f
            setWidths(szerokosci)
            headerRows = 2 // nagłówek powtarza się na kolejnych stronach
        }
        dodajNaglowekTabeli(t)
        dane.wiersze.forEachIndexed { idx, w -> dodajPrace(t, idx + 1, w) }
        dodajSume(t, dane)
        return t
    }

    private fun dodajNaglowekTabeli(t: PdfPTable) {
        t.addCell(c("LP", rowspan = 2)); t.addCell(c("Data", rowspan = 2)); t.addCell(c("Usługa", rowspan = 2))
        t.addCell(c("Robocizna", colspan = 2)); t.addCell(c("Materiał", colspan = 4))
        t.addCell(c("Koszt zakupu 8%", rowspan = 2)); t.addCell(c("Transport/sprzęt", colspan = 3))
        t.addCell(c("Suma", rowspan = 2))
        t.addCell(c("", rowspan = 2, ramka = false))
        listOf("Ilość godzin", "Wartość", "Nazwa", "Ilość", "Jednostka", "Wartość", "Nazwa", "Ilość", "Wartość")
            .forEach { t.addCell(c(it)) }
    }

    /** Praca zajmuje tyle wierszy, ile ma materiałów (min. 1); wszystko poza materiałami jest scalone w pionie. */
    private fun dodajPrace(t: PdfPTable, lp: Int, w: WierszRaportu) {
        val n = maxOf(w.materialy.size, 1)
        val dojazd = w.kosztDojazdu > 0

        for (i in 0 until n) {
            if (i == 0) {
                t.addCell(c(lp.toString(), rowspan = n, valign = Element.ALIGN_TOP))
                t.addCell(c(w.data.format(formatData), rowspan = n, valign = Element.ALIGN_TOP))
                t.addCell(c(w.opis, Element.ALIGN_LEFT, rowspan = n, valign = Element.ALIGN_BOTTOM))
                t.addCell(c(w.roboczogodziny.toString(), rowspan = n, valign = Element.ALIGN_BOTTOM))
                t.addCell(c(kwota(w.kosztRobocizny), valign = Element.ALIGN_BOTTOM, rowspan = n))
            }

            val m = w.materialy.getOrNull(i)
            t.addCell(c(m?.nazwa ?: "", align = Element.ALIGN_LEFT, valign = Element.ALIGN_BOTTOM))
            t.addCell(c(m?.let { formatIlosci.format(it.ilosc) } ?: "", valign = Element.ALIGN_BOTTOM))
            t.addCell(c(m?.jednostka ?: "", valign = Element.ALIGN_BOTTOM))
            t.addCell(c(m?.let { kwota(it.kosztCalkowity) } ?: "", align = Element.ALIGN_RIGHT, valign = Element.ALIGN_BOTTOM))
            t.addCell(c(m?.let { kwota(it.kosztCalkowity * NARZUT_MATERIALOW) } ?: "", align = Element.ALIGN_RIGHT, valign = Element.ALIGN_BOTTOM))

            if (i == 0) {
                t.addCell(c(if (dojazd) "dojazd" else "", rowspan = n, valign = Element.ALIGN_BOTTOM))
                t.addCell(c(if (dojazd) "1" else "", rowspan = n,valign = Element.ALIGN_BOTTOM))
                t.addCell(c(if (dojazd) kwota(w.kosztDojazdu) else "", valign = Element.ALIGN_BOTTOM, rowspan = n))
                t.addCell(c(kwota(sumaPracy(w)), valign = Element.ALIGN_BOTTOM, rowspan = n))
                t.addCell(c(if (w.vat == 23) "23%" else "", align = Element.ALIGN_RIGHT, rowspan = n, valign = Element.ALIGN_BOTTOM, ramka = false))
            }
        }
    }

    private fun dodajSume(t: PdfPTable, dane: DaneRaportu) {
        t.addCell(c("", colspan = 13, ramka = false))
        t.addCell(c(kwota(dane.wiersze.sumOf { sumaPracy(it) }), valign = Element.ALIGN_BOTTOM))
        t.addCell(c("", ramka = false))
    }

    private fun sumaPracy(w: WierszRaportu) =
        w.kosztRobocizny + w.kosztDojazdu + w.materialy.sumOf { it.kosztCalkowity } * (1 + NARZUT_MATERIALOW)

    private fun kwota(v: Double) = formatKwoty.format(v)

    private fun c(
        tekst: String,
        align: Int = Element.ALIGN_CENTER,
        rowspan: Int = 1,
        colspan: Int = 1,
        ramka: Boolean = true,
        valign: Int = Element.ALIGN_MIDDLE
    ) = PdfPCell(Phrase(tekst, font)).apply {
        horizontalAlignment = align
        verticalAlignment = valign
        this.rowspan = rowspan
        this.colspan = colspan
        setPadding(2f)
        if (ramka) borderWidth = 0.5f else border = Rectangle.NO_BORDER
    }

    private companion object {
        const val NARZUT_MATERIALOW = 0.08 // "Koszt zakupu 8%"
    }
}