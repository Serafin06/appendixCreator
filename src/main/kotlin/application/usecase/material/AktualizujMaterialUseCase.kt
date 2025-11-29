package pl.rafapp.marko.appendixCreator.application.usecase.material

import pl.rafapp.marko.appendixCreator.domain.model.Material
import pl.rafapp.marko.appendixCreator.domain.repository.MaterialRepository

class AktualizujMaterialUseCase(private val repository: MaterialRepository) {

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
                nazwa = nazwa.trim(),
                jednostka = jednostka.trim(),
                cenaZaJednostke = cenaZaJednostke
            )

            Result.success(repository.aktualizuj(material))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}