package pl.rafapp.marko.appendixCreator

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import pl.rafapp.marko.appendixCreator.config.DatabaseConfig
import androidx.compose.ui.unit.dp
import pl.rafapp.marko.appendixCreator.presentation.ui.theme.AppColors

/**
 * Główna funkcja aplikacji
 * Tworzy Dependency Injection Container i uruchamia GUI
 */
fun main() = application {
    // Inicjalizacja połączenia z bazą
    println("🚀 Uruchamianie Appendix Creator...")

    try {
        // Test połączenia
        DatabaseConfig.sessionFactory
        println("✅ Połączenie z bazą danych OK")
    } catch (e: Exception) {
        println("❌ Błąd połączenia z bazą: ${e.message}")
        e.printStackTrace()
        return@application
    }

    // Dependency Injection Container
    val container = DependencyContainer()

    Window(
        onCloseRequest = {
            println("🔌 Zamykanie aplikacji...")
            DatabaseConfig.shutdown()
            exitApplication()
        },
        title = "Appendix Creator",
        state = rememberWindowState(width = 1200.dp, height = 800.dp)
    ) {
        MaterialTheme(
            colorScheme = AppColors.darkScheme
        ) {
            App(container)
        }
    }
}


