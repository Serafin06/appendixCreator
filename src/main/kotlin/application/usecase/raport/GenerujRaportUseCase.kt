package pl.rafapp.marko.appendixCreator.application.usecase.raport

import pl.rafapp.marko.appendixCreator.domain.model.*
import pl.rafapp.marko.appendixCreator.domain.repository.*
import java.time.LocalDate
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
    val numerFaktury: String = "",
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
    private val ustawieniaRepository: UstawieniaRepository,
    private val raportBuilder: RaportBuilder
) {
    operator fun invoke(budynekId: Long, rok: Int, miesiac: Int): Result<DaneRaportu> = try {
        val budynek = budynekRepository.pobierzPoId(budynekId)
            ?: throw IllegalArgumentException("Budynek nie istnieje")
        val od = LocalDate.of(rok, miesiac, 1)
        val prace = pracaRepository.pobierzZOkresu(od, od.plusMonths(1)).filter { it.budynekId == budynekId }
        if (prace.isEmpty()) Result.failure(BrakPracException())
        else Result.success(
            raportBuilder.zbuduj(
                budynek, prace,
                materialRepository.pobierzWszystkie().associateBy { it.id },
                ustawieniaRepository.pobierz().stawkaRoboczogodziny,
                rok, miesiac
            )
        )
    } catch (e: Exception) {
        Result.failure(e)
    }
}

class BrakPracException : IllegalStateException("Brak prac dla wybranego budynku i okresu")