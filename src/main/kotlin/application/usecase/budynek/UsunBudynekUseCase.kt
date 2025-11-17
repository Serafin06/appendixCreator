package pl.rafapp.marko.appendixCreator.application.usecase.budynek

import pl.rafapp.marko.appendixCreator.domain.repository.BudynekRepository

/**
 * Use Case: Usuwanie budynku
 */
class UsunBudynekUseCase(private val repository: BudynekRepository) {

    operator fun invoke(id: Long): Result<Unit> {
        return try {
            repository.usun(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}