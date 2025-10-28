package pl.rafapp.appendixCreator.logic.usecase

import pl.rafapp.appendixCreator.domena.Material
import pl.rafapp.appendixCreator.domena.repo.MaterialRepo

class DodajMaterialUseCase(private val repository: MaterialRepo) {
    operator fun invoke(
        nazwa: String,
        jednostka: String,
        cenaZaJednostke: Double
    ): Result<Material> {
        return try {
            require(nazwa.isNotBlank()) { "Nazwa materiału nie może być pusta" }
            require(jednostka.isNotBlank()) { "Jednostka nie może być pusta" }
            require(cenaZaJednostke > 0) { "Cena musi być większa od 0" }

            val material = Material(
                nazwa = nazwa,
                jednostka = jednostka,
                cenaZaJednostke = cenaZaJednostke
            )

            Result.success(repository.dodaj(material))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class AktualizujMaterialUseCase(private val repository: MaterialRepo) {
    operator fun invoke(
        id: Long,
        nazwa: String,
        jednostka: String,
        cenaZaJednostke: Double
    ): Result<Material> {
        return try {
            require(nazwa.isNotBlank()) { "Nazwa materiału nie może być pusta" }
            require(jednostka.isNotBlank()) { "Jednostka nie może być pusta" }
            require(cenaZaJednostke > 0) { "Cena musi być większa od 0" }

            val material = Material(
                id = id,
                nazwa = nazwa,
                jednostka = jednostka,
                cenaZaJednostke = cenaZaJednostke
            )

            Result.success(repository.aktualizuj(material))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class PobierzMaterialyUseCase(private val repository: MaterialRepo) {
    operator fun invoke(): Result<List<Material>> {
        return try {
            Result.success(repository.pobierzWszystkie())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class UsunMaterialUseCase(private val repository: MaterialRepo) {
    operator fun invoke(id: Long): Result<Unit> {
        return try {
            repository.usun(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}