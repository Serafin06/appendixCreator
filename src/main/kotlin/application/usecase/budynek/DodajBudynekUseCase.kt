package pl.rafapp.marko.appendixCreator.application.usecase.budynek

import pl.rafapp.marko.appendixCreator.domain.model.Budynek
import pl.rafapp.marko.appendixCreator.domain.repository.BudynekRepository

/**
 * Use Case: Dodawanie nowego budynku
 * Single Responsibility: walidacja i delegacja do repozytorium
 */
class DodajBudynekUseCase(private val repository: BudynekRepository) {

    operator fun invoke(adres: String): Result<Budynek> {
        return try {
            require(adres.isNotBlank()) { "Adres nie może być pusty" }

            val budynek = Budynek(adres = adres.trim())
            Result.success(repository.dodaj(budynek))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}