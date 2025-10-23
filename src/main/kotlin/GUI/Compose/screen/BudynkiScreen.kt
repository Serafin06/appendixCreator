package pl.rafapp.appendixCreator.GUI.Compose.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.rafapp.appendixCreator.domena.Budynek
import pl.rafapp.appendixCreator.logic.usecase.*


@Composable
fun BudynkiScreen(
    dodajUseCase: DodajBudynekUseCase,
    pobierzUseCase: PobierzBudynkiUseCase,
    usunUseCase: UsunBudynekUseCase
) {
    var budynki by remember { mutableStateOf<List<Budynek>>(emptyList()) }
    var adres by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            pobierzUseCase().onSuccess { budynki = it }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Budynki", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = adres,
            onValueChange = { adres = it },
            label = { Text("Adres") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            scope.launch {
                withContext(Dispatchers.IO) {
                    dodajUseCase(adres).onSuccess {
                        adres = ""
                        pobierzUseCase().onSuccess { budynki = it }
                        message = "Dodano budynek"
                    }.onFailure {
                        message = "Błąd: ${it.message}"
                    }
                }
            }
        }) {
            Text("Dodaj")
        }

        if (message.isNotEmpty()) {
            Text(message, color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        budynki.forEach { budynek ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(budynek.adres, modifier = Modifier.weight(1f))
                Button(onClick = {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            usunUseCase(budynek.id)
                            pobierzUseCase().onSuccess { budynki = it }
                        }
                    }
                }) {
                    Text("Usuń")
                }
            }
        }
    }
}