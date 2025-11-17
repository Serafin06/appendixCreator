package pl.rafapp.marko.appendixCreator.application.usecase.material

import pl.rafapp.marko.appendixCreator.domain.repository.MaterialRepository

/**
 * Use Case: Usuwanie materiału
 */

class UsunMaterialUseCase(private val repository: MaterialRepository) {

    operator fun invoke(id: Long): Result<Unit> {
        return try {
            repository.usun(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}