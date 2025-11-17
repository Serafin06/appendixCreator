package pl.rafapp.marko.appendixCreator.application.usecase.praca

import pl.rafapp.marko.appendixCreator.domain.model.Praca
import pl.rafapp.marko.appendixCreator.domain.repository.PracaRepository

/**
 * Use Case: Dodawanie nowej pracy
 */
class DodajPraceUseCase(private val repository: PracaRepository) {

    operator fun invoke(praca: Praca): Result<Praca> {
        return try {
            require(praca.opis.isNotBlank()) { "Opis pracy nie może być pusty" }
            require(praca.roboczogodziny in 1..30) { "Godziny muszą być w zakresie 1-30" }
            require(praca.vat in listOf(8, 23)) { "VAT musi być 8% lub 23%" }
            require(praca.kosztDojazdu >= 0) { "Koszt dojazdu nie może być ujemny" }

            Result.success(repository.dodaj(praca))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}