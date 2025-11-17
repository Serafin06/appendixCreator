package pl.rafapp.marko.appendixCreator.application.usecase.budynek

import pl.rafapp.marko.appendixCreator.domain.model.Budynek
import pl.rafapp.marko.appendixCreator.domain.repository.BudynekRepository

/**
 * Use Case: Pobieranie listy wszystkich budynków
 */
class PobierzBudynkiUseCase(private val repository: BudynekRepository) {

    operator fun invoke(): Result<List<Budynek>> {
        return try {
            Result.success(repository.pobierzWszystkie())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}