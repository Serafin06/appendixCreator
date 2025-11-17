package pl.rafapp.marko.appendixCreator.application.usecase.praca

import pl.rafapp.marko.appendixCreator.domain.model.Praca
import pl.rafapp.marko.appendixCreator.domain.repository.PracaRepository

/**
 * Use Case: Pobieranie listy prac
 */
class PobierzPraceUseCase(private val repository: PracaRepository) {

    operator fun invoke(): Result<List<Praca>> {
        return try {
            Result.success(repository.pobierzWszystkie())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun dlaBudynku(budynekId: Long): Result<List<Praca>> {
        return try {
            Result.success(repository.pobierzDlaBudynku(budynekId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}