package pl.rafapp.appendixCreator.logic.usecase

import pl.rafapp.appendixCreator.domena.Material
import pl.rafapp.appendixCreator.domena.repo.MaterialRepo

class MaterialPrice {
    private val repository: MaterialRepo
    ) {
        operator fun invoke(): Result<List<Material>> {
            return try {
                Result.success(repository.pobierzWszystkie().sortedBy { it.nazwa })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}