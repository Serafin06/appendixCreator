package pl.rafapp.marko.appendixCreator.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pl.rafapp.marko.appendixCreator.domain.model.Material
import pl.rafapp.marko.appendixCreator.presentation.ui.component.ConfirmDialog
import pl.rafapp.marko.appendixCreator.presentation.viewmodel.MaterialyViewModel

/**
 * Ekran zarządzania materiałami
 */
@Composable
fun MaterialyScreen(viewModel: MaterialyViewModel) {
    var nazwa by remember { mutableStateOf("") }
    var jednostka by remember { mutableStateOf("") }
    var cena by remember { mutableStateOf("") }
    var materialDoUsuniecia by remember { mutableStateOf<Material?>(null) }

    LaunchedEffect(viewModel.successMessage) {
        if (viewModel.successMessage != null) {
            nazwa = ""
            jednostka = ""
            cena = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Komunikaty
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

        // Formularz dodawania
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Dodaj nowy materiał",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = nazwa,
                    onValueChange = {
                        nazwa = it
                        viewModel.clearMessages()
                    },
                    label = { Text("Nazwa materiału") },
                    placeholder = { Text("np. Cement") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !viewModel.isLoading
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = jednostka,
                        onValueChange = {
                            jednostka = it
                            viewModel.clearMessages()
                        },
                        label = { Text("Jednostka") },
                        placeholder = { Text("np. worek, szt, litr") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        enabled = !viewModel.isLoading
                    )

                    OutlinedTextField(
                        value = cena,
                        onValueChange = {
                            cena = it
                            viewModel.clearMessages()
                        },
                        label = { Text("Cena za jedn.") },
                        placeholder = { Text("0.00") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        enabled = !viewModel.isLoading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val cenaDouble = cena.replace(",", ".").toDoubleOrNull()
                        if (cenaDouble != null) {
                            viewModel.dodajMaterial(nazwa, jednostka, cenaDouble)
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    enabled = !viewModel.isLoading &&
                            nazwa.isNotBlank() &&
                            jednostka.isNotBlank() &&
                            cena.isNotBlank()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Dodaj materiał")
                }
            }
        }

        // Lista materiałów
        Text(
            "Lista materiałów (${viewModel.materialy.size})",
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
        } else if (viewModel.materialy.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    "Brak materiałów w bazie danych",
                    modifier = Modifier.padding(32.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.materialy) { material ->
                    MaterialCard(
                        material = material,
                        onDelete = { materialDoUsuniecia = material }
                    )
                }
            }
        }
    }

    // Dialog potwierdzenia usunięcia
    materialDoUsuniecia?.let { material ->
        ConfirmDialog(
            title = "Usunąć materiał?",
            message = "Czy na pewno chcesz usunąć: ${material.nazwa}?",
            onConfirm = {
                viewModel.usunMaterial(material.id)
                materialDoUsuniecia = null
            },
            onDismiss = { materialDoUsuniecia = null }
        )
    }
}

@Composable
fun MaterialCard(material: Material, onDelete: () -> Unit) {
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
                    material.nazwa,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "${String.format("%.2f", material.cenaZaJednostke)} zł / ${material.jednostka}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "ID: ${material.id}",
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