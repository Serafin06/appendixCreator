package pl.rafapp.marko.appendixCreator.presentation.viewmodel

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.rafapp.marko.appendixCreator.application.usecase.budynek.PobierzBudynkiUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.material.PobierzMaterialyUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.praca.*
import pl.rafapp.marko.appendixCreator.application.usecase.ustawienia.PobierzUstawieniaUseCase
import pl.rafapp.marko.appendixCreator.domain.model.*
import java.time.LocalDate
import java.time.YearMonth


/**
 * ViewModel dla zarządzania pracami
 */
class PraceViewModel(
    private val dodajPraceUseCase: DodajPraceUseCase,
    private val pobierzPraceUseCase: PobierzPraceUseCase,
    private val usunPraceUseCase: UsunPraceUseCase,
    private val edytujPraceUseCase: EdytujPraceUseCase,
    private val pobierzBudynkiUseCase: PobierzBudynkiUseCase,
    private val pobierzMaterialyUseCase: PobierzMaterialyUseCase,
    private val pobierzUstawieniaUseCase: PobierzUstawieniaUseCase
) {
    var prace by mutableStateOf<List<Praca>>(emptyList())
        private set

    var budynki by mutableStateOf<List<Budynek>>(emptyList())
        private set

    var materialy by mutableStateOf<List<Material>>(emptyList())
        private set

    var domyslnyKosztDojazdu by mutableStateOf(0.0)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    var wybranyOkres by mutableStateOf(YearMonth.now())
        private set

    var filtrBudynekId by mutableStateOf<Long?>(null)
        private set

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        zaladujDane()
    }

    // --- Filtrowanie: dostępne opcje wynikają wyłącznie z istniejących prac ---

    val dostepneLata: List<Int>
        get() = (prace.map { it.data.year } + YearMonth.now().year)
            .distinct().sortedDescending()

    fun dostepneMiesiace(rok: Int): List<Int> {
        val teraz = YearMonth.now()
        val zPrac = prace.filter { it.data.year == rok }.map { it.data.monthValue }
        val biezacy = if (rok == teraz.year) listOf(teraz.monthValue) else emptyList()
        return (zPrac + biezacy).distinct().sorted()
    }

    private val pracePoOkresie: List<Praca>
        get() = prace.filter { YearMonth.from(it.data) == wybranyOkres }

    /** Budynki, w których w wybranym miesiącu była praca */
    val budynkiWOkresie: List<Budynek>
        get() = budynki.filter { b -> pracePoOkresie.any { it.budynekId == b.id } }

    val przefiltrowanePrace: List<Praca>
        get() = pracePoOkresie.filter { filtrBudynekId == null || it.budynekId == filtrBudynekId }

    fun ustawRok(rok: Int) {
        val miesiace = dostepneMiesiace(rok)
        val miesiac = wybranyOkres.monthValue.takeIf { it in miesiace } ?: miesiace.last()
        wybranyOkres = YearMonth.of(rok, miesiac)
        filtrBudynekId = null
    }

    fun ustawMiesiac(miesiac: Int) {
        wybranyOkres = YearMonth.of(wybranyOkres.year, miesiac)
        filtrBudynekId = null
    }

    fun ustawFiltrBudynku(id: Long?) {
        filtrBudynekId = id
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

                pobierzUstawieniaUseCase()
                    .onSuccess { domyslnyKosztDojazdu = it.kosztDojazdu }
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
        nrFaktury: String,
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
                nrFaktury = nrFaktury,
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

    fun edytujPrace(praca: Praca) {
        scope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null

            withContext(Dispatchers.IO) {
                edytujPraceUseCase(praca)
                    .onSuccess {
                        successMessage = "Zapisano zmiany"
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

    fun sugerujNrFaktury(budynekId: Long?, data: LocalDate): String {
        val okres = YearMonth.from(data)

        if (budynekId != null) {
            prace.firstOrNull {
                it.budynekId == budynekId &&
                        YearMonth.from(it.data) == okres &&
                        !it.nrFaktury.isNullOrBlank()
            }?.nrFaktury?.let { return it }
        }

        val rok = data.year
        val maxNr = prace
            .mapNotNull { it.nrFaktury?.split("/") }
            .filter { it.getOrNull(1)?.trim() == rok.toString() }
            .mapNotNull { it[0].trim().toIntOrNull() }
            .maxOrNull() ?: 0
        return "${maxNr + 1}/$rok"
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }
}