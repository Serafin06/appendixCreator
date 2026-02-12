package pl.rafapp.marko.appendixCreator.application.usecase.raport

import pl.rafapp.marko.appendixCreator.domain.model.*
import pl.rafapp.marko.appendixCreator.domain.repository.*
import java.time.Month

data class WierszRaportu(
    val data: java.time.LocalDate,
    val opis: String,
    val roboczogodziny: Int,
    val stawkaRoboczogodziny: Double,
    val kosztRobocizny: Double,
    val kosztDojazdu: Double,
    val materialy: List<WierszMaterialuRaportu>,
    val kosztMaterialow: Double,
    val kosztNetto: Double,
    val vat: Int,
    val kosztBrutto: Double
)

data class WierszMaterialuRaportu(
    val nazwa: String,
    val jednostka: String,
    val ilosc: Double,
    val cenaZaJednostke: Double,
    val kosztCalkowity: Double
)

data class DaneRaportu(
    val budynek: Budynek,
    val rok: Int,
    val miesiac: Int,
    val wiersze: List<WierszRaportu>,
    val stawkaRoboczogodziny: Double,
    // Podsumowanie
    val sumaRoboczogodzin: Int,
    val sumaKosztowRobocizny: Double,
    val sumaKosztowDojazdu: Double,
    val sumaKosztowMaterialow: Double,
    val sumaNetto: Double,
    val sumaVat: Double,
    val sumaBrutto: Double
)

/**
 * Use Case: Generowanie danych do raportu miesięcznego
 * Single Responsibility: tylko przygotowanie danych, nie generuje pliku
 */
class GenerujRaportUseCase(
    private val budynekRepository: BudynekRepository,
    private val pracaRepository: PracaRepository,
    private val materialRepository: MaterialRepository,
    private val ustawieniaRepository: UstawieniaRepository
) {
    operator fun invoke(budynekId: Long, rok: Int, miesiac: Int): Result<DaneRaportu> {
        return try {
            val budynek = budynekRepository.pobierzPoId(budynekId)
                ?: throw IllegalArgumentException("Budynek nie istnieje")

            val stawka = ustawieniaRepository.pobierz().stawkaRoboczogodziny

            val wszystkieMaterialy = materialRepository.pobierzWszystkie()
                .associateBy { it.id }

            val prace = pracaRepository.pobierzDlaBudynku(budynekId)
                .filter { it.data.year == rok && it.data.monthValue == miesiac }
                .sortedBy { it.data }

            if (prace.isEmpty()) {
                return Result.failure(
                    IllegalStateException("Brak prac dla wybranego budynku i okresu")
                )
            }

            val wiersze = prace.map { praca ->
                val kosztRobocizny = praca.roboczogodziny * stawka

                val wierszeMaterialow = praca.materialy.mapNotNull { pm ->
                    val material = wszystkieMaterialy[pm.materialId] ?: return@mapNotNull null
                    WierszMaterialuRaportu(
                        nazwa = material.nazwa,
                        jednostka = material.jednostka,
                        ilosc = pm.ilosc,
                        cenaZaJednostke = material.cenaZaJednostke,
                        kosztCalkowity = pm.ilosc * material.cenaZaJednostke
                    )
                }

                val kosztMaterialow = wierszeMaterialow.sumOf { it.kosztCalkowity }
                val kosztNetto = kosztRobocizny + praca.kosztDojazdu + kosztMaterialow
                val kosztBrutto = kosztNetto * (1 + praca.vat / 100.0)

                WierszRaportu(
                    data = praca.data,
                    opis = praca.opis,
                    roboczogodziny = praca.roboczogodziny,
                    stawkaRoboczogodziny = stawka,
                    kosztRobocizny = kosztRobocizny,
                    kosztDojazdu = praca.kosztDojazdu,
                    materialy = wierszeMaterialow,
                    kosztMaterialow = kosztMaterialow,
                    kosztNetto = kosztNetto,
                    vat = praca.vat,
                    kosztBrutto = kosztBrutto
                )
            }

            // Podsumowanie
            val sumaNetto = wiersze.sumOf { it.kosztNetto }
            // VAT liczymy jako brutto - netto (bo każda praca może mieć inny VAT)
            val sumaBrutto = wiersze.sumOf { it.kosztBrutto }
            val sumaVat = sumaBrutto - sumaNetto

            Result.success(
                DaneRaportu(
                    budynek = budynek,
                    rok = rok,
                    miesiac = miesiac,
                    wiersze = wiersze,
                    stawkaRoboczogodziny = stawka,
                    sumaRoboczogodzin = wiersze.sumOf { it.roboczogodziny },
                    sumaKosztowRobocizny = wiersze.sumOf { it.kosztRobocizny },
                    sumaKosztowDojazdu = wiersze.sumOf { it.kosztDojazdu },
                    sumaKosztowMaterialow = wiersze.sumOf { it.kosztMaterialow },
                    sumaNetto = sumaNetto,
                    sumaVat = sumaVat,
                    sumaBrutto = sumaBrutto
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}