package pl.rafapp.marko.appendixCreator

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import pl.rafapp.marko.appendixCreator.application.usecase.budynek.*
import pl.rafapp.marko.appendixCreator.application.usecase.material.*
import pl.rafapp.marko.appendixCreator.application.usecase.praca.*
import pl.rafapp.marko.appendixCreator.config.DatabaseConfig
import pl.rafapp.marko.appendixCreator.data.repository.*
import pl.rafapp.marko.appendixCreator.presentation.ui.screen.*
import pl.rafapp.marko.appendixCreator.presentation.viewmodel.*
import androidx.compose.ui.unit.dp

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
        MaterialTheme {
            App(container)
        }
    }
}


