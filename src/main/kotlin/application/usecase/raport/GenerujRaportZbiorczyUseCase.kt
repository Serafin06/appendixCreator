package pl.rafapp.marko.appendixCreator.application.usecase.raport

import pl.rafapp.marko.appendixCreator.application.usecase.budynek.PobierzBudynkiUseCase

/** Generuje zbiorczy raport miesięczny dla wszystkich budynków z podziałem na stawki VAT */
class GenerujRaportZbiorczyUseCase(
    private val pobierzBudynkiUseCase: PobierzBudynkiUseCase,
    private val generujRaportUseCase: GenerujRaportUseCase
) {
    operator fun invoke(rok: Int, miesiac: Int): Result<DaneRaportuZbiorczego> {
        return try {
            val budynki = pobierzBudynkiUseCase().getOrElse { return Result.failure(it) }

            val wiersze = budynki.mapNotNull { budynek ->
                generujRaportUseCase(budynek.id, rok, miesiac).getOrNull()?.let { dane ->
                    val w8 = dane.wiersze.filter { it.vat == 8 }
                    val w23 = dane.wiersze.filter { it.vat == 23 }
                    WierszBudynkuZbiorczy(
                        budynek = budynek,
                        nettoVat8 = w8.sumOf { it.kosztNetto },
                        vatKwota8 = w8.sumOf { it.kosztBrutto - it.kosztNetto },
                        bruttoVat8 = w8.sumOf { it.kosztBrutto },
                        nettoVat23 = w23.sumOf { it.kosztNetto },
                        vatKwota23 = w23.sumOf { it.kosztBrutto - it.kosztNetto },
                        bruttoVat23 = w23.sumOf { it.kosztBrutto },
                        sumaNetto = dane.sumaNetto,
                        sumaVat = dane.sumaVat,
                        sumaBrutto = dane.sumaBrutto
                    )
                }
            }.filter { it.sumaNetto > 0 }

            if (wiersze.isEmpty())
                return Result.failure(IllegalStateException("Brak prac w wybranym okresie"))

            Result.success(DaneRaportuZbiorczego(
                rok = rok, miesiac = miesiac,
                wierszeBudynkow = wiersze,
                lacznaNetto8 = wiersze.sumOf { it.nettoVat8 },
                lacznaVat8 = wiersze.sumOf { it.vatKwota8 },
                lacznaBrutto8 = wiersze.sumOf { it.bruttoVat8 },
                lacznaNetto23 = wiersze.sumOf { it.nettoVat23 },
                lacznaVat23 = wiersze.sumOf { it.vatKwota23 },
                lacznaBrutto23 = wiersze.sumOf { it.bruttoVat23 },
                sumaNetto = wiersze.sumOf { it.sumaNetto },
                sumaVat = wiersze.sumOf { it.sumaVat },
                sumaBrutto = wiersze.sumOf { it.sumaBrutto }
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}