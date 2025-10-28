package pl.rafapp.appendixCreator.logic.usecase

import pl.rafapp.appendixCreator.domena.repo.MaterialRepo


// Use Case do pobierania materiałów posortowanych alfabetycznie
// Specjalnie dla wizarda - czytelniejsza nazwa i dodatkowa logika sortowania

class PobierzMaterialyZCenamiUseCase(private val repository: MaterialRepo) {
    operator fun invoke(): Result<List<Material>> {
        return try {
            val materialy = repository.pobierzWszystkie()
                .sortedBy { it.nazwa.lowercase() }

            Result.success(materialy)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}