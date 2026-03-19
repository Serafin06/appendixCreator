package pl.rafapp.marko.appendixCreator.application.usecase.raport

import pl.rafapp.marko.appendixCreator.domain.model.Budynek

data class WierszBudynkuZbiorczy(
    val budynek: Budynek,
    val nettoVat8: Double,
    val vatKwota8: Double,
    val bruttoVat8: Double,
    val nettoVat23: Double,
    val vatKwota23: Double,
    val bruttoVat23: Double,
    val sumaNetto: Double,
    val sumaVat: Double,
    val sumaBrutto: Double
)

data class DaneRaportuZbiorczego(
    val rok: Int,
    val miesiac: Int,
    val wierszeBudynkow: List<WierszBudynkuZbiorczy>,
    val lacznaNetto8: Double,
    val lacznaVat8: Double,
    val lacznaBrutto8: Double,
    val lacznaNetto23: Double,
    val lacznaVat23: Double,
    val lacznaBrutto23: Double,
    val sumaNetto: Double,
    val sumaVat: Double,
    val sumaBrutto: Double
)