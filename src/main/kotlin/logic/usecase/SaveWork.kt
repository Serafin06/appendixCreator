package pl.rafapp.appendixCreator.logic.usecase

import pl.rafapp.appendixCreator.domena.Budynek
import pl.rafapp.appendixCreator.domena.Praca
import pl.rafapp.appendixCreator.domena.repo.BudynekRepo
import pl.rafapp.appendixCreator.domena.repo.PracaRepo

// Orchestrator - koordynuje zapisywanie pracy z wizarda
// Może stworzyć nowy budynek jeśli trzeba

class SaveWork(
    private val budynekRepository: BudynekRepo,
    private val pracaRepository: PracaRepo
) {
    operator fun invoke(state: PracaWizardState): Result<Praca> {
        return try {
            // Walidacja kompletności
            require(state.isKrok1Valid()) { "Dane budynku są niepełne" }
            require(state.isKrok2Valid()) { "Dane pracy są niepełne" }

            // Pobierz lub utwórz budynek
            val budynekId = if (state.budynekId != null) {
                state.budynekId
            } else {
                val nowyBudynek = budynekRepository.dodaj(
                    Budynek(adres = state.nowyAdres)
                )
                nowyBudynek.id
            }

            // Zapisz pracę
            val praca = state.toPraca(budynekId)
            val zapisanaPraca = pracaRepository.dodaj(praca)

            Result.success(zapisanaPraca)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}