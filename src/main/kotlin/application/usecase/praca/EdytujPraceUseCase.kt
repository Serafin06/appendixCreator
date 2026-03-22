package pl.rafapp.marko.appendixCreator.application.usecase.praca

import pl.rafapp.marko.appendixCreator.domain.model.Praca
import pl.rafapp.marko.appendixCreator.domain.repository.PracaRepository

/** Use Case: Aktualizacja istniejącej pracy */
class EdytujPraceUseCase(private val repository: PracaRepository) {
    operator fun invoke(praca: Praca): Result<Praca> = try {
        Result.success(repository.aktualizuj(praca))
    } catch (e: Exception) {
        Result.failure(e)
    }
}