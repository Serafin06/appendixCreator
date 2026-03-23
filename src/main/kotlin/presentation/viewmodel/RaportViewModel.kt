package pl.rafapp.marko.appendixCreator.presentation.viewmodel

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.rafapp.marko.appendixCreator.application.usecase.budynek.PobierzBudynkiUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.raport.DaneRaportu
import pl.rafapp.marko.appendixCreator.application.usecase.raport.DaneRaportuZbiorczego
import pl.rafapp.marko.appendixCreator.application.usecase.raport.ExportToExcelUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.raport.ExportWszystkichBudynkowUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.raport.GenerujRaportUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.raport.GenerujRaportZbiorczyUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.ustawienia.PobierzUstawieniaUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.ustawienia.ZapiszUstawieniaUseCase
import pl.rafapp.marko.appendixCreator.domain.model.Budynek
import java.io.File
import java.time.LocalDate

/**
 * ViewModel dla generowania raportów
 * MVVM Pattern - zarządza stanem ekranu raportu
 */

class RaportViewModel(
    private val pobierzBudynkiUseCase: PobierzBudynkiUseCase,
    private val pobierzUstawieniaUseCase: PobierzUstawieniaUseCase,
    private val zapiszUstawieniaUseCase: ZapiszUstawieniaUseCase,
    private val generujRaportZbiorczyUseCase: GenerujRaportZbiorczyUseCase,
    private val generujRaportUseCase: GenerujRaportUseCase,
    private val exportToExcelUseCase: ExportToExcelUseCase,
    private val exportWszystkichBudynkowUseCase: ExportWszystkichBudynkowUseCase
) {
    enum class TypRaportu { POJEDYNCZY_BUDYNEK, ZBIORCZY_MIESIAC, WSZYSTKIE_BUDYNKI }

    var budynki by mutableStateOf<List<Budynek>>(emptyList())
        private set

    var wybranyBudynekId by mutableStateOf<Long?>(null)
        private set

    var wybranyRok by mutableStateOf(LocalDate.now().year)
        private set

    var wybranyMiesiac by mutableStateOf(LocalDate.now().monthValue)
        private set

    var stawkaRoboczogodziny by mutableStateOf("50.00")
        private set

    var daneRaportu by mutableStateOf<DaneRaportu?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    var kosztDojazdu by mutableStateOf("25.00")
        private set

    var nrFaktury by mutableStateOf("")
        private set

    fun ustawNrFaktury(nr: String) {
        nrFaktury = nr
        clearMessages()
    }

    var typRaportu by mutableStateOf(TypRaportu.POJEDYNCZY_BUDYNEK)
        private set

    var daneRaportuZbiorczego by mutableStateOf<DaneRaportuZbiorczego?>(null)
        private set

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        zaladujDane()
    }

    fun zaladujDane() {
        scope.launch {
            isLoading = true

            withContext(Dispatchers.IO) {
                pobierzBudynkiUseCase()
                    .onSuccess { budynki = it }

                pobierzUstawieniaUseCase()
                    .onSuccess {
                        stawkaRoboczogodziny = String.format("%.2f", it.stawkaRoboczogodziny)
                        kosztDojazdu = String.format("%.2f", it.kosztDojazdu)
                    }
            }

            isLoading = false
        }
    }

    fun wybierzBudynek(id: Long?) {
        wybranyBudynekId = id
        daneRaportu = null
        clearMessages()
    }

    fun ustawRok(rok: Int) {
        wybranyRok = rok
        daneRaportu = null
        clearMessages()
    }

    fun ustawMiesiac(miesiac: Int) {
        wybranyMiesiac = miesiac
        daneRaportu = null
        clearMessages()
    }

    fun ustawStawke(stawka: String) {
        stawkaRoboczogodziny = stawka
        clearMessages()
    }

    fun ustawKosztDojazdu(koszt: String) {
        kosztDojazdu = koszt
        clearMessages()
    }

    fun zapiszStawke() {
        val stawkaDouble = stawkaRoboczogodziny.replace(",", ".").toDoubleOrNull()
            ?: return

        val dojazdDouble = kosztDojazdu.replace(",", ".").toDoubleOrNull() ?: return

        scope.launch {
            withContext(Dispatchers.IO) {
                zapiszUstawieniaUseCase(stawkaDouble, dojazdDouble)
                    .onSuccess { successMessage = "Stawka zapisana: ${String.format("%.2f", stawkaDouble)} zł/h" }
                    .onFailure { errorMessage = "Błąd zapisu stawki: ${it.message}" }
            }
        }
    }

    fun ustawTypRaportu(typ: TypRaportu) {
        typRaportu = typ
        daneRaportu = null
        daneRaportuZbiorczego = null
        clearMessages()
    }

    fun generujPodglad() {
        when (typRaportu) {
            TypRaportu.POJEDYNCZY_BUDYNEK -> generujPodgladJednego()
            TypRaportu.ZBIORCZY_MIESIAC,
            TypRaportu.WSZYSTKIE_BUDYNKI -> generujPodgladZbiorczy()
        }
    }

    private fun generujPodgladJednego() {
        val budynekId = wybranyBudynekId ?: run { errorMessage = "Wybierz budynek"; return }
        scope.launch {
            isLoading = true
            errorMessage = null
            daneRaportu = null
            withContext(Dispatchers.IO) {
                generujRaportUseCase(budynekId, wybranyRok, wybranyMiesiac, nrFaktury)
                    .onSuccess { daneRaportu = it }
                    .onFailure { errorMessage = "Błąd: ${it.message}" }
            }
            isLoading = false
        }
    }

    private fun generujPodgladZbiorczy() {
        scope.launch {
            isLoading = true
            errorMessage = null
            daneRaportuZbiorczego = null
            withContext(Dispatchers.IO) {
                generujRaportZbiorczyUseCase(wybranyRok, wybranyMiesiac)
                    .onSuccess { daneRaportuZbiorczego = it }
                    .onFailure { errorMessage = "Błąd: ${it.message}" }
            }
            isLoading = false
        }
    }

    fun exportExcel(folder: File) {
        when (typRaportu) {
            TypRaportu.POJEDYNCZY_BUDYNEK -> exportJednego(folder)
            TypRaportu.ZBIORCZY_MIESIAC -> { /* TODO: export zbiorczy do jednego pliku */ }
            TypRaportu.WSZYSTKIE_BUDYNKI -> exportWszystkich(folder)
        }
    }

    private fun exportJednego(folder: File) {
        val dane = daneRaportu ?: run { errorMessage = "Najpierw wygeneruj podgląd"; return }
        scope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            val nazwaPliku = "Raport_${dane.budynek.miasto}_${dane.miesiac}_${dane.rok}.xlsx".replace(" ", "_")
            withContext(Dispatchers.IO) {
                exportToExcelUseCase(dane, File(folder, nazwaPliku))
                    .onSuccess { successMessage = "Zapisano: ${it.absolutePath}" }
                    .onFailure { errorMessage = "Błąd eksportu: ${it.message}" }
            }
            isLoading = false
        }
    }

    private fun exportWszystkich(folder: File) {
        scope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            withContext(Dispatchers.IO) {
                exportWszystkichBudynkowUseCase(folder, wybranyRok, wybranyMiesiac)
                    .onSuccess { successMessage = "Zapisano ${it.size} plików w: ${folder.absolutePath}" }
                    .onFailure { errorMessage = "Błąd eksportu: ${it.message}" }
            }
            isLoading = false
        }
    }
    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }
}