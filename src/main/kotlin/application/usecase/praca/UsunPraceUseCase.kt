package pl.rafapp.marko.appendixCreator.application.usecase.praca

import pl.rafapp.marko.appendixCreator.domain.repository.PracaRepository

/**
 * Use Case: Usuwanie pracy
 */
class UsunPraceUseCase(private val repository: PracaRepository) {

    operator fun invoke(id: Long): Result<Unit> {
        return try {
            repository.usun(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}