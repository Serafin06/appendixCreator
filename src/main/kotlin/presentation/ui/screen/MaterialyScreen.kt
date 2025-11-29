package pl.rafapp.marko.appendixCreator.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pl.rafapp.marko.appendixCreator.domain.model.Jednostki
import pl.rafapp.marko.appendixCreator.domain.model.Material
import pl.rafapp.marko.appendixCreator.presentation.ui.component.ConfirmDialog
import pl.rafapp.marko.appendixCreator.presentation.viewmodel.MaterialyViewModel

/**
 * Ekran zarządzania materiałami
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialyScreen(viewModel: MaterialyViewModel) {
    var pokazFormularz by remember { mutableStateOf(false) }
    var materialDoEdycji by remember { mutableStateOf<Material?>(null) }
    var materialDoUsuniecia by remember { mutableStateOf<Material?>(null) }

    if (pokazFormularz || materialDoEdycji != null) {
        FormularzMaterialu(
            viewModel = viewModel,
            materialDoEdycji = materialDoEdycji,
            onClose = {
                pokazFormularz = false
                materialDoEdycji = null
            }
        )
    } else {
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

            // Nagłówek z przyciskiem
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Lista materiałów (${viewModel.materialy.size})",
                    style = MaterialTheme.typography.titleMedium
                )

                Button(
                    onClick = { pokazFormularz = true },
                    enabled = !viewModel.isLoading
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Dodaj materiał")
                }
            }

            // Lista materiałów
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
                            onEdit = { materialDoEdycji = material },
                            onDelete = { materialDoUsuniecia = material }
                        )
                    }
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
fun MaterialCard(
    material: Material,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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

            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edytuj",
                        tint = MaterialTheme.colorScheme.primary
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularzMaterialu(
    viewModel: MaterialyViewModel,
    materialDoEdycji: Material?,
    onClose: () -> Unit
) {
    var nazwa by remember { mutableStateOf(materialDoEdycji?.nazwa ?: "") }
    var wybranaJednostka by remember { mutableStateOf(materialDoEdycji?.jednostka ?: "szt") }
    var cena by remember { mutableStateOf(materialDoEdycji?.cenaZaJednostke?.toString() ?: "") }
    var rozwiniety by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.successMessage) {
        if (viewModel.successMessage != null) {
            onClose()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Nagłówek
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (materialDoEdycji != null) "Edytuj materiał" else "Nowy materiał",
                style = MaterialTheme.typography.headlineSmall
            )

            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Zamknij")
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Nazwa
                OutlinedTextField(
                    value = nazwa,
                    onValueChange = { nazwa = it },
                    label = { Text("Nazwa materiału") },
                    placeholder = { Text("np. Cement") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !viewModel.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Jednostka - dropdown
                ExposedDropdownMenuBox(
                    expanded = rozwiniety,
                    onExpandedChange = { rozwiniety = it }
                ) {
                    OutlinedTextField(
                        value = wybranaJednostka,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Jednostka") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rozwiniety) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = rozwiniety,
                        onDismissRequest = { rozwiniety = false }
                    ) {
                        Jednostki.DOSTEPNE.forEach { jednostka ->
                            DropdownMenuItem(
                                text = { Text(jednostka) },
                                onClick = {
                                    wybranaJednostka = jednostka
                                    rozwiniety = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cena
                OutlinedTextField(
                    value = cena,
                    onValueChange = { cena = it },
                    label = { Text("Cena za jednostkę (zł)") },
                    placeholder = { Text("0.00") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !viewModel.isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Przyciski
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onClose,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Anuluj")
                    }

                    Button(
                        onClick = {
                            val cenaDouble = cena.replace(",", ".").toDoubleOrNull()
                            if (cenaDouble != null) {
                                if (materialDoEdycji != null) {
                                    viewModel.aktualizujMaterial(
                                        materialDoEdycji.id,
                                        nazwa,
                                        wybranaJednostka,
                                        cenaDouble
                                    )
                                } else {
                                    viewModel.dodajMaterial(nazwa, wybranaJednostka, cenaDouble)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !viewModel.isLoading &&
                                nazwa.isNotBlank() &&
                                cena.isNotBlank()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (materialDoEdycji != null) "Zapisz" else "Dodaj")
                    }
                }
            }
        }
    }
}