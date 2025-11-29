package pl.rafapp.marko.appendixCreator.application.usecase.budynek

import pl.rafapp.marko.appendixCreator.domain.model.Budynek
import pl.rafapp.marko.appendixCreator.domain.repository.BudynekRepository

/**
 * Use Case: Dodawanie nowego budynku
 * Single Responsibility: walidacja i delegacja do repozytorium
 */
class DodajBudynekUseCase(private val repository: BudynekRepository) {

    operator fun invoke(miasto: String, ulica: String): Result<Budynek> {
        return try {
            require(miasto.isNotBlank()) { "Miasto nie może być puste" }
            require(ulica.isNotBlank()) { "Ulica nie może być pusta" }

            val budynek = Budynek(
                miasto = miasto.trim(),
                ulica = ulica.trim()
            )
            Result.success(repository.dodaj(budynek))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}