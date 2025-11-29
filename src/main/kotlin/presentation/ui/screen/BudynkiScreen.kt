package pl.rafapp.marko.appendixCreator.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.rafapp.marko.appendixCreator.domain.model.Budynek
import pl.rafapp.marko.appendixCreator.presentation.ui.component.ConfirmDialog
import pl.rafapp.marko.appendixCreator.presentation.viewmodel.BudynkiViewModel

/**
 * Ekran zarządzania budynkami
 * Prezentuje listę i formularz dodawania
 */

@Composable
fun BudynkiScreen(viewModel: BudynkiViewModel) {
    var miasto by remember { mutableStateOf("Katowice") }  // Domyślne
    var ulica by remember { mutableStateOf("") }
    var budynekDoUsuniecia by remember { mutableStateOf<Budynek?>(null) }

    LaunchedEffect(viewModel.successMessage) {
        if (viewModel.successMessage != null) {
            ulica = ""  // Czyścimy tylko ulicę, miasto zostaje
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Komunikaty (bez zmian - tylko skopiuj z poprzedniej wersji)
        viewModel.errorMessage?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Text(
                    error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        viewModel.successMessage?.let { success ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Text(
                    success,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Formularz dodawania - ZMIENIONY!
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Dodaj nowy budynek",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Miasto
                OutlinedTextField(
                    value = miasto,
                    onValueChange = {
                        miasto = it
                        viewModel.clearMessages()
                    },
                    label = { Text("Miasto") },
                    placeholder = { Text("Katowice") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !viewModel.isLoading
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Ulica
                OutlinedTextField(
                    value = ulica,
                    onValueChange = {
                        ulica = it
                        viewModel.clearMessages()
                    },
                    label = { Text("Ulica") },
                    placeholder = { Text("np. ul. Kwiatowa 5") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !viewModel.isLoading
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.dodajBudynek(miasto, ulica) },
                    modifier = Modifier.align(Alignment.End),
                    enabled = !viewModel.isLoading && miasto.isNotBlank() && ulica.isNotBlank()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Dodaj budynek")
                }
            }
        }

        // Lista budynków
        Text(
            "Lista budynków (${viewModel.budynki.size})",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (viewModel.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (viewModel.budynki.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    "Brak budynków w bazie danych",
                    modifier = Modifier.padding(32.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.budynki) { budynek ->
                    BudynekCard(
                        budynek = budynek,
                        onDelete = { budynekDoUsuniecia = budynek }
                    )
                }
            }
        }
    }

    // Dialog potwierdzenia usunięcia
    budynekDoUsuniecia?.let { budynek ->
        ConfirmDialog(
            title = "Usunąć budynek?",
            message = "Czy na pewno chcesz usunąć: ${budynek.pelnyAdres}?\nSpowoduje to również usunięcie wszystkich prac dla tego budynku!",
            onConfirm = {
                viewModel.usunBudynek(budynek.id)
                budynekDoUsuniecia = null
            },
            onDismiss = { budynekDoUsuniecia = null }
        )
    }
}

@Composable
fun BudynekCard(budynek: Budynek, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    budynek.ulica,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    budynek.miasto,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "ID: ${budynek.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Usuń",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}