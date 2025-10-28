package pl.rafapp.appendixCreator.logic.usecase

import pl.rafapp.appendixCreator.domena.Budynek
import pl.rafapp.appendixCreator.domena.repo.BudynekRepo
import pl.rafapp.appendixCreator.domena.repo.PracaRepo


// Pobiera budynki, na których były prace w danym miesiącu
// Single Responsibility: jedna odpowiedzialność - filtrowanie budynków

class ImportBuildings(
    private val budynekRepository: BudynekRepo,
    private val pracaRepository: PracaRepo
) {
    operator fun invoke(rok: Int, miesiac: Int): Result<List<Budynek>> {
        return try {
            val wszystkieBudynki = budynekRepository.pobierzWszystkie()
            val wszystkiePrace = pracaRepository.pobierzWszystkie()

            val budynkiZMiesiaca = wszystkiePrace
                .filter { it.data.year == rok && it.data.monthValue == miesiac }
                .map { it.budynekId }
                .distinct()

            val wynik = wszystkieBudynki.filter { it.id in budynkiZMiesiaca }

            Result.success(wynik)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}