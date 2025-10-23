package pl.rafapp.appendixCreator.logic.usecase

import pl.rafapp.appendixCreator.dataBase.repo.BudynekRepo
import pl.rafapp.appendixCreator.domena.Budynek

class DodajBudynekUseCase(private val repository: BudynekRepo) {
    operator fun invoke(adres: String): Result<Budynek> {
        return try {
            if (adres.isBlank()) {
                return Result.failure(IllegalArgumentException("Adres nie może być pusty"))
            }
            val budynek = Budynek(adres = adres)
            Result.success(repository.dodaj(budynek))
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

