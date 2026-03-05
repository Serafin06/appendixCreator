package pl.rafapp.marko.appendixCreator.application.usecase.ustawienia

import pl.rafapp.marko.appendixCreator.domain.model.Ustawienia
import pl.rafapp.marko.appendixCreator.domain.repository.UstawieniaRepository

class ZapiszUstawieniaUseCase(private val repository: UstawieniaRepository) {
    operator fun invoke(stawka: Double, kosztDojazdu: Double): Result<Ustawienia> {
        return try {
            require(stawka > 0) { "Stawka musi być większa od 0" }
            require(kosztDojazdu >= 0) { "Koszt dojazdu nie może być ujemny" }
            Result.success(repository.zapisz(Ustawienia(
                stawkaRoboczogodziny = stawka,
                kosztDojazdu = kosztDojazdu
            )))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}