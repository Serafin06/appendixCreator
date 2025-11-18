package pl.rafapp.marko.appendixCreator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import pl.rafapp.marko.appendixCreator.presentation.ui.component.AppScaffold
import pl.rafapp.marko.appendixCreator.presentation.ui.screen.BudynkiScreen
import pl.rafapp.marko.appendixCreator.presentation.ui.screen.MaterialyScreen
import pl.rafapp.marko.appendixCreator.presentation.ui.screen.PraceScreen

/**
 * Główny komponent aplikacji
 * Zarządza nawigacją między zakładkami
 */
@Composable
fun App(container: DependencyContainer) {
    var selectedTab by remember { mutableStateOf(0) }

    // ViewModels - tworzymy raz i zapamiętujemy
    val budynkiViewModel = remember { container.createBudynkiViewModel() }
    val materialyViewModel = remember { container.createMaterialyViewModel() }
    val praceViewModel = remember { container.createPraceViewModel() }

    AppScaffold(
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it }
    ) {
        when (selectedTab) {
            0 -> BudynkiScreen(budynkiViewModel)
            1 -> MaterialyScreen(materialyViewModel)
            2 -> PraceScreen(praceViewModel)
        }
    }
}