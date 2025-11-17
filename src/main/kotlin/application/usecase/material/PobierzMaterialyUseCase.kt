package pl.rafapp.marko.appendixCreator.application.usecase.material

import pl.rafapp.marko.appendixCreator.domain.model.Material
import pl.rafapp.marko.appendixCreator.domain.repository.MaterialRepository

/**
 * Use Case: Pobieranie listy wszystkich materiałów
 */
class PobierzMaterialyUseCase(private val repository: MaterialRepository) {

    operator fun invoke(): Result<List<Material>> {
        return try {
            Result.success(repository.pobierzWszystkie())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}