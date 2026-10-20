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
    private val szerokosci = floatArrayOf(3.3f, 13.3f, 39.7f, 6.4f, 7.9f, 20.4f, 4.9f, 9.6f, 7.9f, 7.4f, 9.3f, 4.9f, 7.9f, 8.9f)

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
        ).forEach { doc.add(Paragraph(it, font)) }
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
        listOf("Ilość godzin", "Wartość", "Nazwa", "Ilość", "Jednostka", "Wartość", "Nazwa", "Ilość", "Wartość")
            .forEach { t.addCell(c(it)) }
    }

    /** Praca zajmuje tyle wierszy, ile ma materiałów (min. 1); robocizna/dojazd/suma w ostatnim wierszu. */
    private fun dodajPrace(t: PdfPTable, lp: Int, w: WierszRaportu) {
        val n = maxOf(w.materialy.size, 1)
        for (i in 0 until n) {
            val ostatni = i == n - 1
            if (i == 0) {
                t.addCell(c(lp.toString(), rowspan = n))
                t.addCell(c(w.data.format(formatData), rowspan = n))
                t.addCell(c(w.opis, Element.ALIGN_LEFT, rowspan = n))
            }
            t.addCell(c(if (ostatni) w.roboczogodziny.toString() else ""))
            t.addCell(c(if (ostatni) kwota(w.kosztRobocizny) else "", Element.ALIGN_RIGHT))

            val m = w.materialy.getOrNull(i)
            t.addCell(c(m?.nazwa ?: "", Element.ALIGN_LEFT))
            t.addCell(c(m?.let { formatIlosci.format(it.ilosc) } ?: ""))
            t.addCell(c(m?.jednostka ?: ""))
            t.addCell(c(m?.let { kwota(it.kosztCalkowity) } ?: "", Element.ALIGN_RIGHT))
            t.addCell(c(m?.let { kwota(it.kosztCalkowity * NARZUT_MATERIALOW) } ?: "", Element.ALIGN_RIGHT))

            val dojazd = ostatni && w.kosztDojazdu > 0
            t.addCell(c(if (dojazd) "dojazd" else ""))
            t.addCell(c(if (dojazd) "1" else ""))
            t.addCell(c(if (dojazd) kwota(w.kosztDojazdu) else "", Element.ALIGN_RIGHT))

            t.addCell(c(if (ostatni) kwota(sumaPracy(w)) else "", Element.ALIGN_RIGHT))
        }
    }

    private fun dodajSume(t: PdfPTable, dane: DaneRaportu) {
        t.addCell(c("", colspan = 13, ramka = false))
        t.addCell(c(kwota(dane.wiersze.sumOf { sumaPracy(it) }), Element.ALIGN_RIGHT))
    }

    private fun sumaPracy(w: WierszRaportu) =
        w.kosztRobocizny + w.kosztDojazdu + w.materialy.sumOf { it.kosztCalkowity } * (1 + NARZUT_MATERIALOW)

    private fun kwota(v: Double) = formatKwoty.format(v)

    private fun c(
        tekst: String,
        align: Int = Element.ALIGN_CENTER,
        rowspan: Int = 1,
        colspan: Int = 1,
        ramka: Boolean = true
    ) = PdfPCell(Phrase(tekst, font)).apply {
        horizontalAlignment = align
        verticalAlignment = Element.ALIGN_MIDDLE
        this.rowspan = rowspan
        this.colspan = colspan
        setPadding(2f)
        if (ramka) borderWidth = 0.5f else border = Rectangle.NO_BORDER
    }

    private companion object {
        const val NARZUT_MATERIALOW = 0.08 // "Koszt zakupu 8%"
    }
}