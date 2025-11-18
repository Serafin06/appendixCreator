package pl.rafapp.marko.appendixCreator.presentation.viewmodel

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.rafapp.marko.appendixCreator.application.usecase.material.*
import pl.rafapp.marko.appendixCreator.domain.model.Material


/**
 * ViewModel dla zarządzania materiałami
 */
class MaterialyViewModel(
    private val dodajUseCase: DodajMaterialUseCase,
    private val pobierzUseCase: PobierzMaterialyUseCase,
    private val usunUseCase: UsunMaterialUseCase
) {
    var materialy by mutableStateOf<List<Material>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        zaladujMaterialy()
    }

    fun zaladujMaterialy() {
        scope.launch {
            isLoading = true
            errorMessage = null

            withContext(Dispatchers.IO) {
                pobierzUseCase()
                    .onSuccess { materialy = it }
                    .onFailure { errorMessage = "Błąd ładowania: ${it.message}" }
            }

            isLoading = false
        }
    }

    fun dodajMaterial(nazwa: String, jednostka: String, cena: Double) {
        scope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null

            withContext(Dispatchers.IO) {
                dodajUseCase(nazwa, jednostka, cena)
                    .onSuccess {
                        successMessage = "Dodano materiał: ${it.nazwa}"
                        zaladujMaterialy()
                    }
                    .onFailure { errorMessage = "Błąd: ${it.message}" }
            }

            isLoading = false
        }
    }

    fun usunMaterial(id: Long) {
        scope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null

            withContext(Dispatchers.IO) {
                usunUseCase(id)
                    .onSuccess {
                        successMessage = "Usunięto materiał"
                        zaladujMaterialy()
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