package pl.rafapp.marko.appendixCreator.application.usecase.ustawienia

import pl.rafapp.marko.appendixCreator.domain.model.Ustawienia
import pl.rafapp.marko.appendixCreator.domain.repository.UstawieniaRepository

class PobierzUstawieniaUseCase(private val repository: UstawieniaRepository) {
    operator fun invoke(): Result<Ustawienia> {
        return try {
            Result.success(repository.pobierz())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}