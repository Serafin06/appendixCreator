package pl.rafapp.marko.appendixCreator.presentation.viewmodel

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.rafapp.marko.appendixCreator.application.usecase.budynek.PobierzBudynkiUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.material.PobierzMaterialyUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.praca.*
import pl.rafapp.marko.appendixCreator.domain.model.*
import java.time.LocalDate


/**
 * ViewModel dla zarządzania pracami
 */
class PraceViewModel(
    private val dodajPraceUseCase: DodajPraceUseCase,
    private val pobierzPraceUseCase: PobierzPraceUseCase,
    private val usunPraceUseCase: UsunPraceUseCase,
    private val pobierzBudynkiUseCase: PobierzBudynkiUseCase,
    private val pobierzMaterialyUseCase: PobierzMaterialyUseCase
) {
    var prace by mutableStateOf<List<Praca>>(emptyList())
        private set

    var budynki by mutableStateOf<List<Budynek>>(emptyList())
        private set

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
        zaladujDane()
    }

    fun zaladujDane() {
        scope.launch {
            isLoading = true
            errorMessage = null

            withContext(Dispatchers.IO) {
                pobierzPraceUseCase()
                    .onSuccess { prace = it }
                    .onFailure { errorMessage = "Błąd ładowania prac: ${it.message}" }

                pobierzBudynkiUseCase()
                    .onSuccess { budynki = it }
                    .onFailure { errorMessage = "Błąd ładowania budynków: ${it.message}" }

                pobierzMaterialyUseCase()
                    .onSuccess { materialy = it }
                    .onFailure { errorMessage = "Błąd ładowania materiałów: ${it.message}" }
            }

            isLoading = false
        }
    }

    fun dodajPrace(
        budynekId: Long,
        data: LocalDate,
        opis: String,
        godziny: Int,
        kosztDojazdu: Double,
        vat: Int,
        wybraneMaterialy: List<PracaMaterial>
    ) {
        scope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null

            val praca = Praca(
                budynekId = budynekId,
                data = data,
                opis = opis,
                roboczogodziny = godziny,
                kosztDojazdu = kosztDojazdu,
                vat = vat,
                materialy = wybraneMaterialy
            )

            withContext(Dispatchers.IO) {
                dodajPraceUseCase(praca)
                    .onSuccess {
                        successMessage = "Dodano pracę"
                        zaladujDane()
                    }
                    .onFailure { errorMessage = "Błąd: ${it.message}" }
            }

            isLoading = false
        }
    }

    fun usunPrace(id: Long) {
        scope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null

            withContext(Dispatchers.IO) {
                usunPraceUseCase(id)
                    .onSuccess {
                        successMessage = "Usunięto pracę"
                        zaladujDane()
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