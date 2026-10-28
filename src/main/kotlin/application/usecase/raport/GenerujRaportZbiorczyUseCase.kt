package pl.rafapp.marko.appendixCreator.application.usecase.raport

import pl.rafapp.marko.appendixCreator.domain.repository.BudynekRepository
import pl.rafapp.marko.appendixCreator.domain.repository.MaterialRepository
import pl.rafapp.marko.appendixCreator.domain.repository.PracaRepository
import pl.rafapp.marko.appendixCreator.domain.repository.UstawieniaRepository
import java.time.LocalDate

/** Generuje zbiorczy raport miesięczny dla wszystkich budynków z podziałem na stawki VAT */
class GenerujRaportZbiorczyUseCase(
    private val budynekRepository: BudynekRepository,
    private val pracaRepository: PracaRepository,
    private val materialRepository: MaterialRepository,
    private val ustawieniaRepository: UstawieniaRepository,
    private val raportBuilder: RaportBuilder
) {
    operator fun invoke(rok: Int, miesiac: Int): Result<DaneRaportuZbiorczego> {
        return try {
            val stawka = ustawieniaRepository.pobierz().stawkaRoboczogodziny
            val materialy = materialRepository.pobierzWszystkie().associateBy { it.id }
            val od = LocalDate.of(rok, miesiac, 1)
            val pracePoBudynku = pracaRepository.pobierzZOkresu(od, od.plusMonths(1)).groupBy { it.budynekId }

            val raporty = budynekRepository.pobierzWszystkie().mapNotNull { b ->
                pracePoBudynku[b.id]?.let { raportBuilder.zbuduj(b, it, materialy, stawka, rok, miesiac) }
            }.filter { it.sumaNetto > 0 }

            if (raporty.isEmpty())
                return Result.failure(IllegalStateException("Brak prac w wybranym okresie"))

            val wiersze = raporty.map { dane ->
                val w8 = dane.wiersze.filter { it.vat == 8 }
                val w23 = dane.wiersze.filter { it.vat == 23 }
                WierszBudynkuZbiorczy(
                    budynek = dane.budynek,
                    nettoVat8 = w8.sumOf { it.kosztNetto },
                    vatKwota8 = w8.sumOf { it.kosztBrutto - it.kosztNetto },
                    bruttoVat8 = w8.sumOf { it.kosztBrutto },
                    nettoVat23 = w23.sumOf { it.kosztNetto },
                    vatKwota23 = w23.sumOf { it.kosztBrutto - it.kosztNetto },
                    bruttoVat23 = w23.sumOf { it.kosztBrutto },
                    sumaNetto = dane.sumaNetto, sumaVat = dane.sumaVat, sumaBrutto = dane.sumaBrutto
                )
            }

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
                sumaBrutto = wiersze.sumOf { it.sumaBrutto },
                raporty = raporty
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}