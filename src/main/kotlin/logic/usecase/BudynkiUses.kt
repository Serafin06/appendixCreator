package pl.rafapp.appendixCreator.logic.usecase

import pl.rafapp.appendixCreator.dataBase.repo.BudynekRepo
import pl.rafapp.appendixCreator.domena.Budynek


class DodajBudynekUseCase(private val repository: BudynekRepo) {
    operator fun invoke(adres: String): Result<Budynek> {
        return try {
            require(adres.isNotBlank()) { "Adres nie może być pusty" }
            Result.success(repository.dodaj(Budynek(adres = adres)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class AktualizujBudynekUseCase(private val repository: BudynekRepo) {
    operator fun invoke(id: Long, adres: String): Result<Budynek> {
        return try {
            require(adres.isNotBlank()) { "Adres nie może być pusty" }
            Result.success(repository.aktualizuj(Budynek(id = id, adres = adres)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


class PobierzBudynkiUseCase(private val repository: BudynekRepo) {
    operator fun invoke(): Result<List<Budynek>> {
        return try {
            Result.success(repository.pobierzWszystkie())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class UsunBudynekUseCase(private val repository: BudynekRepo) {
    operator fun invoke(id: Long): Result<Unit> {
        return try {
            repository.usun(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

