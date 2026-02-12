package pl.rafapp.marko.appendixCreator.presentation.viewmodel

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.rafapp.marko.appendixCreator.application.usecase.budynek.PobierzBudynkiUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.raport.DaneRaportu
import pl.rafapp.marko.appendixCreator.application.usecase.raport.ExportToExcelUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.raport.GenerujRaportUseCase
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
    private val generujRaportUseCase: GenerujRaportUseCase,
    private val exportToExcelUseCase: ExportToExcelUseCase
) {
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
                    .onSuccess { stawkaRoboczogodziny = String.format("%.2f", it.stawkaRoboczogodziny) }
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

    fun zapiszStawke() {
        val stawkaDouble = stawkaRoboczogodziny.replace(",", ".").toDoubleOrNull()
            ?: return

        scope.launch {
            withContext(Dispatchers.IO) {
                zapiszUstawieniaUseCase(stawkaDouble)
                    .onSuccess { successMessage = "Stawka zapisana: ${String.format("%.2f", stawkaDouble)} zł/h" }
                    .onFailure { errorMessage = "Błąd zapisu stawki: ${it.message}" }
            }
        }
    }

    fun generujPodglad() {
        val budynekId = wybranyBudynekId ?: run {
            errorMessage = "Wybierz budynek"
            return
        }

        scope.launch {
            isLoading = true
            errorMessage = null
            daneRaportu = null

            withContext(Dispatchers.IO) {
                generujRaportUseCase(budynekId, wybranyRok, wybranyMiesiac)
                    .onSuccess { daneRaportu = it }
                    .onFailure { errorMessage = "Błąd: ${it.message}" }
            }

            isLoading = false
        }
    }

    fun exportExcel(folder: File) {
        val dane = daneRaportu ?: run {
            errorMessage = "Najpierw wygeneruj podgląd"
            return
        }

        scope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null

            val nazwaPliku = "Raport_${dane.budynek.miasto}_${dane.miesiac}_${dane.rok}.xlsx"
                .replace(" ", "_")
            val plik = File(folder, nazwaPliku)

            withContext(Dispatchers.IO) {
                eksportujDoExcelUseCase(dane, plik)
                    .onSuccess { successMessage = "Zapisano: ${it.absolutePath}" }
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