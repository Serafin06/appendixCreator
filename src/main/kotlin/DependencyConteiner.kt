package pl.rafapp.marko.appendixCreator

import pl.rafapp.marko.appendixCreator.application.usecase.budynek.DodajBudynekUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.budynek.PobierzBudynkiUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.budynek.UsunBudynekUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.material.AktualizujMaterialUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.material.DodajMaterialUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.material.PobierzMaterialyUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.material.UsunMaterialUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.praca.DodajPraceUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.praca.PobierzPraceUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.praca.UsunPraceUseCase
import pl.rafapp.marko.appendixCreator.data.repository.BudynekRepositoryImpl
import pl.rafapp.marko.appendixCreator.data.repository.MaterialRepositoryImpl
import pl.rafapp.marko.appendixCreator.data.repository.PracaRepositoryImpl
import pl.rafapp.marko.appendixCreator.presentation.viewmodel.BudynkiViewModel
import pl.rafapp.marko.appendixCreator.presentation.viewmodel.MaterialyViewModel
import pl.rafapp.marko.appendixCreator.presentation.viewmodel.PraceViewModel

/**
 * Dependency Injection Container
 * Single Responsibility: tworzenie i zarządzanie zależnościami
 */
class DependencyContainer {
    // Repositories
    private val budynekRepository = BudynekRepositoryImpl()
    private val materialRepository = MaterialRepositoryImpl()
    private val pracaRepository = PracaRepositoryImpl()

    // Use Cases - Budynki
    val dodajBudynekUseCase = DodajBudynekUseCase(budynekRepository)
    val pobierzBudynkiUseCase = PobierzBudynkiUseCase(budynekRepository)
    val usunBudynekUseCase = UsunBudynekUseCase(budynekRepository)

    // Use Cases - Materiały
    val dodajMaterialUseCase = DodajMaterialUseCase(materialRepository)
    val aktualizujMaterialUseCase = AktualizujMaterialUseCase(materialRepository)
    val pobierzMaterialyUseCase = PobierzMaterialyUseCase(materialRepository)
    val usunMaterialUseCase = UsunMaterialUseCase(materialRepository)

    // Use Cases - Prace
    val dodajPraceUseCase = DodajPraceUseCase(pracaRepository)
    val pobierzPraceUseCase = PobierzPraceUseCase(pracaRepository)
    val usunPraceUseCase = UsunPraceUseCase(pracaRepository)

    // ViewModels
    fun createBudynkiViewModel() = BudynkiViewModel(
        dodajUseCase = dodajBudynekUseCase,
        pobierzUseCase = pobierzBudynkiUseCase,
        usunUseCase = usunBudynekUseCase
    )

    fun createMaterialyViewModel() = MaterialyViewModel(
        dodajUseCase = dodajMaterialUseCase,
        aktualizujUseCase = aktualizujMaterialUseCase,
        pobierzUseCase = pobierzMaterialyUseCase,
        usunUseCase = usunMaterialUseCase
    )

    fun createPraceViewModel() = PraceViewModel(
        dodajPraceUseCase = dodajPraceUseCase,
        pobierzPraceUseCase = pobierzPraceUseCase,
        usunPraceUseCase = usunPraceUseCase,
        pobierzBudynkiUseCase = pobierzBudynkiUseCase,
        pobierzMaterialyUseCase = pobierzMaterialyUseCase
    )
}