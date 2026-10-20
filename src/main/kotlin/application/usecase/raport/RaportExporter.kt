package pl.rafapp.marko.appendixCreator.application.usecase.raport

import java.io.File

enum class FormatEksportu { EXCEL, PDF }

/** Kontrakt eksportera raportów (Strategy). Każdy format ma własną implementację. */
interface RaportExporter {
    val rozszerzenie: String
    fun eksportuj(raporty: List<DaneRaportu>, plik: File): Result<File>
    fun eksportujZbiorczy(dane: DaneRaportuZbiorczego, plik: File): Result<File>
}

// Wspólne dla eksporterów zestawienia zbiorczego
val NAGLOWKI_ZBIORCZE = listOf(
    "Budynek", "Netto 8%", "VAT 8%", "Brutto 8%",
    "Netto 23%", "VAT 23%", "Brutto 23%", "Suma netto", "Suma VAT", "Suma brutto"
)

fun WierszBudynkuZbiorczy.kwoty() = listOf(
    nettoVat8, vatKwota8, bruttoVat8, nettoVat23, vatKwota23, bruttoVat23, sumaNetto, sumaVat, sumaBrutto
)

fun DaneRaportuZbiorczego.kwotyRazem() = listOf(
    lacznaNetto8, lacznaVat8, lacznaBrutto8, lacznaNetto23, lacznaVat23, lacznaBrutto23, sumaNetto, sumaVat, sumaBrutto
)

