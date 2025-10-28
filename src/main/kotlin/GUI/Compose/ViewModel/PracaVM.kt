package pl.rafapp.appendixCreator.GUI.Compose.ViewModel

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.rafapp.appendixCreator.logic.usecase.*
import pl.rafapp.appendixCreator.domena.*
import java.time.LocalDate

// ViewModel zgodnie z MVVM Pattern
// Oddziela logikę prezentacji od UI

class PracaWizardViewModel(
    private val pobierzBudynkiUseCase: PobierzBudynkiUseCase,
    private val pobierzBudynkiZMiesiacaUseCase: ImportBuildings,
    private val pobierzMaterialyUseCase: MaterialPrice,
    private val zapiszPraceUseCase: SaveWork
) {
    var state by mutableStateOf(PracaWizardState())
        private set

    var wszystkieBudynki by mutableStateOf<List<Budynek>>(emptyList())
        private set

    var budynkiZMiesiaca by mutableStateOf<List<Budynek>>(emptyList())
        private set

    var dostepneMaterialy by mutableStateOf<List<Material>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        zaladujDane()
    }

    // === Krok 1: Budynek i data ===

    fun wybierzBudynek(budynekId: Long?) {
        state = state.copy(budynekId = budynekId, nowyAdres = "")
        errorMessage = null
    }

    fun ustawNowyAdres(adres: String) {
        state = state.copy(nowyAdres = adres, budynekId = null)
        errorMessage = null
    }

    fun ustawDate(data: LocalDate) {
        state = state.copy(data = data)
        zaladujBudynkiZMiesiaca(data.year, data.monthValue)
    }

    // === Krok 2: Szczegóły pracy ===

    fun ustawOpis(opis: String) {
        state = state.copy(opis = opis)
    }

    fun ustawGodziny(godziny: Int) {
        state = state.copy(roboczogodziny = godziny.coerceIn(1, 30))
    }

    fun ustawVat(vat: Int) {
        state = state.copy(vat = if (vat == 8) 8 else 23)
    }

    fun ustawKosztDojazdu(koszt: Double) {
        state = state.copy(kosztDojazdu = koszt.coerceAtLeast(0.0))
    }

    // === Krok 3: Materiały ===

    fun dodajMaterial(material: Material, ilosc: Double) {
        if (ilosc <= 0) return

        val wybrany = WybranyMaterial(
            materialId = material.id,
            nazwa = material.nazwa,
            jednostka = material.jednostka,
            cenaZaJednostke = material.cenaZaJednostke,
            ilosc = ilosc
        )

        // Usuń stary jeśli istnieje, dodaj nowy
        val nowaMaterialy = state.wybraneMaterialy
            .filter { it.materialId != material.id } + wybrany

        state = state.copy(wybraneMaterialy = nowaMaterialy)
    }

    fun usunMaterial(materialId: Long) {
        state = state.copy(
            wybraneMaterialy = state.wybraneMaterialy.filter { it.materialId != materialId }
        )
    }

    // === Nawigacja ===

    fun nastepnyKrok() {
        val czyWalidne = when (state.aktualnyKrok) {
            1 -> state.isKrok1Valid()
            2 -> state.isKrok2Valid()
            3 -> state.isKrok3Valid()
            else -> false
        }

        if (!czyWalidne) {
            errorMessage = "Wypełnij wszystkie wymagane pola"
            return
        }

        if (state.aktualnyKrok < 3) {
            state = state.copy(aktualnyKrok = state.aktualnyKrok + 1)
            errorMessage = null
        } else {
            zapiszPrace()
        }
    }

    fun poprzedniKrok() {
        if (state.aktualnyKrok > 1) {
            state = state.copy(aktualnyKrok = state.aktualnyKrok - 1)
            errorMessage = null
        }
    }

    fun resetuj() {
        state = PracaWizardState()
        errorMessage = null
        zaladujDane()
    }

    // === Private ===

    private fun zaladujDane() {
        scope.launch {
            isLoading = true

            pobierzBudynkiUseCase().onSuccess {
                wszystkieBudynki = it
            }

            pobierzMaterialyUseCase().onSuccess {
                dostepneMaterialy = it
            }

            zaladujBudynkiZMiesiaca(
                state.data.year,
                state.data.monthValue
            )

            isLoading = false
        }
    }

    private fun zaladujBudynkiZMiesiaca(rok: Int, miesiac: Int) {
        scope.launch {
            pobierzBudynkiZMiesiacaUseCase(rok, miesiac).onSuccess {
                budynkiZMiesiaca = it
            }
        }
    }

    private fun zapiszPrace() {
        scope.launch {
            isLoading = true
            errorMessage = null

            zapiszPraceUseCase(state)
                .onSuccess {
                    // Sukces - można pokazać komunikat lub zamknąć wizard
                    resetuj()
                }
                .onFailure {
                    errorMessage = "Błąd zapisu: ${it.message}"
                }

            isLoading = false
        }
    }
}