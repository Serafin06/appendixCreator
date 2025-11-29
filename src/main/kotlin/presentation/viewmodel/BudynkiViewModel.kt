package pl.rafapp.marko.appendixCreator.presentation.viewmodel

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.rafapp.marko.appendixCreator.application.usecase.budynek.*
import pl.rafapp.marko.appendixCreator.domain.model.Budynek


/**
 * ViewModel dla zarządzania budynkami
 * MVVM Pattern - oddziela logikę od UI
 */

class BudynkiViewModel(
    private val dodajUseCase: DodajBudynekUseCase,
    private val pobierzUseCase: PobierzBudynkiUseCase,
    private val usunUseCase: UsunBudynekUseCase
) {
    var budynki by mutableStateOf<List<Budynek>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        zaladujBudynki()
    }

    fun zaladujBudynki() {
        scope.launch {
            isLoading = true
            errorMessage = null

            withContext(Dispatchers.IO) {
                pobierzUseCase()
                    .onSuccess { budynki = it }
                    .onFailure { errorMessage = "Błąd ładowania: ${it.message}" }
            }

            isLoading = false
        }
    }

    fun dodajBudynek(miasto: String, ulica: String) {
        scope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null

            withContext(Dispatchers.IO) {
                dodajUseCase(miasto, ulica)
                    .onSuccess {
                        successMessage = "Dodano budynek: ${it.pelnyAdres}"
                        zaladujBudynki()
                    }
                    .onFailure { errorMessage = "Błąd: ${it.message}" }
            }

            isLoading = false
        }
    }

    fun usunBudynek(id: Long) {
        scope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null

            withContext(Dispatchers.IO) {
                usunUseCase(id)
                    .onSuccess {
                        successMessage = "Usunięto budynek"
                        zaladujBudynki()
                    }
                    .onFailure { errorMessage = "Błąd: ${it.message}" }
            }

            isLoading = false
        }
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }
}