package pl.rafapp.appendixCreator.logic.usecase

import pl.rafapp.appendixCreator.domena.Praca
import pl.rafapp.appendixCreator.domena.repo.PracaRepo

class DodajPraceUseCase(private val repository: PracaRepo) {
    operator fun invoke(praca: Praca): Result<Praca> {
        return try {
            require(praca.opis.isNotBlank()) { "Opis pracy nie może być pusty" }
            require(praca.roboczogodziny in 1..30) { "Godziny muszą być w zakresie 1-30" }
            require(praca.vat == 8 || praca.vat == 23) { "VAT musi być 8% lub 23%" }

            Result.success(repository.dodaj(praca))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class PobierzPraceUseCase(private val repository: PracaRepo) {
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