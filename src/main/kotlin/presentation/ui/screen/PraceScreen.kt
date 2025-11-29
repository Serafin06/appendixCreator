package pl.rafapp.marko.appendixCreator.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pl.rafapp.marko.appendixCreator.domain.model.*
import pl.rafapp.marko.appendixCreator.presentation.ui.component.ConfirmDialog
import pl.rafapp.marko.appendixCreator.presentation.viewmodel.PraceViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Ekran zarządzania pracami
 * Najbardziej złożony - zawiera wybór budynku, datę i materiały
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PraceScreen(viewModel: PraceViewModel) {
    var pokazFormularz by remember { mutableStateOf(false) }
    var pracaDoUsuniecia by remember { mutableStateOf<Praca?>(null) }

    if (pokazFormularz) {
        FormularzPracy(
            viewModel = viewModel,
            onClose = { pokazFormularz = false }
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

            // Nagłówek z przyciskiem dodawania
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Lista prac (${viewModel.prace.size})",
                    style = MaterialTheme.typography.titleMedium
                )

                Button(
                    onClick = { pokazFormularz = true },
                    enabled = !viewModel.isLoading
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Dodaj pracę")
                }
            }

            // Lista prac
            if (viewModel.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (viewModel.prace.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        "Brak prac w bazie danych",
                        modifier = Modifier.padding(32.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.prace) { praca ->
                        PracaCard(
                            praca = praca,
                            budynek = viewModel.budynki.find { it.id == praca.budynekId },
                            materialy = viewModel.materialy,
                            onDelete = { pracaDoUsuniecia = praca }
                        )
                    }
                }
            }
        }
    }

    // Dialog potwierdzenia usunięcia
    pracaDoUsuniecia?.let { praca ->
        ConfirmDialog(
            title = "Usunąć pracę?",
            message = "Czy na pewno chcesz usunąć tę pracę z dnia ${praca.data}?",
            onConfirm = {
                viewModel.usunPrace(praca.id)
                pracaDoUsuniecia = null
            },
            onDismiss = { pracaDoUsuniecia = null }
        )
    }
}

@Composable
fun PracaCard(
    praca: Praca,
    budynek: Budynek?,
    materialy: List<Material>,
    onDelete: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        budynek?.pelnyAdres ?: "Budynek usunięty",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        praca.data.format(formatter),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        praca.opis,
                        style = MaterialTheme.typography.bodyMedium
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

            Spacer(Modifier.height(8.dp))

            // Szczegóły
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        "Godziny: ${praca.roboczogodziny}h",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "Dojazd: ${String.format("%.2f", praca.kosztDojazdu)} zł",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "VAT: ${praca.vat}%",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Materiały
            if (praca.materialy.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Divider()
                Spacer(Modifier.height(8.dp))

                Text(
                    "Materiały:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                praca.materialy.forEach { pracaMaterial ->
                    val material = materialy.find { it.id == pracaMaterial.materialId }
                    if (material != null) {
                        Text(
                            "• ${material.nazwa}: ${pracaMaterial.ilosc} ${material.jednostka} × ${String.format("%.2f", material.cenaZaJednostke)} zł = ${String.format("%.2f", pracaMaterial.ilosc * material.cenaZaJednostke)} zł",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularzPracy(
    viewModel: PraceViewModel,
    onClose: () -> Unit
) {
    var wybranyBudynekId by remember { mutableStateOf<Long?>(null) }
    var data by remember { mutableStateOf(LocalDate.now()) }
    var opis by remember { mutableStateOf("") }
    var godziny by remember { mutableStateOf("8") }
    var kosztDojazdu by remember { mutableStateOf("0") }
    var vat by remember { mutableStateOf(23) }
    var wybraneMaterialy by remember { mutableStateOf<List<Pair<Long, Double>>>(emptyList()) }

    var pokazDatePicker by remember { mutableStateOf(false) }
    var pokazMaterialDialog by remember { mutableStateOf(false) }

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
                "Nowa praca",
                style = MaterialTheme.typography.headlineSmall
            )

            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Zamknij")
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Wybór budynku
            item {
                Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("1. Wybierz budynek", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))

                        var rozwiniety by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = rozwiniety,
                            onExpandedChange = { rozwiniety = it }
                        ) {
                            OutlinedTextField(
                                value = viewModel.budynki.find { it.id == wybranyBudynekId }?.ulica ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Budynek") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rozwiniety) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )

                            ExposedDropdownMenu(
                                expanded = rozwiniety,
                                onDismissRequest = { rozwiniety = false }
                            ) {
                                viewModel.budynki.forEach { budynek ->
                                    DropdownMenuItem(
                                        text = { Text(budynek.ulica) },
                                        onClick = {
                                            wybranyBudynekId = budynek.id
                                            rozwiniety = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Data i podstawowe dane
            item {
                Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("2. Szczegóły pracy", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))

                        // Data
                        OutlinedButton(
                            onClick = { pokazDatePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Data: ${data.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}")
                        }

                        Spacer(Modifier.height(8.dp))

                        // Opis
                        OutlinedTextField(
                            value = opis,
                            onValueChange = { opis = it },
                            label = { Text("Opis pracy") },
                            placeholder = { Text("Co zostało wykonane?") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5
                        )

                        Spacer(Modifier.height(8.dp))

                        // Godziny
                        OutlinedTextField(
                            value = godziny,
                            onValueChange = { if (it.all { char -> char.isDigit() }) godziny = it },
                            label = { Text("Roboczogodziny (1-30)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        Spacer(Modifier.height(8.dp))

                        // Koszt dojazdu
                        OutlinedTextField(
                            value = kosztDojazdu,
                            onValueChange = { kosztDojazdu = it },
                            label = { Text("Koszt dojazdu (zł)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )

                        Spacer(Modifier.height(8.dp))

                        // VAT
                        Text("VAT:", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = vat == 8,
                                onClick = { vat = 8 },
                                label = { Text("8%") }
                            )
                            FilterChip(
                                selected = vat == 23,
                                onClick = { vat = 23 },
                                label = { Text("23%") }
                            )
                        }
                    }
                }
            }

            // Materiały
            item {
                Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("3. Materiały (opcjonalnie)", style = MaterialTheme.typography.titleMedium)

                            Button(
                                onClick = { pokazMaterialDialog = true },
                                enabled = viewModel.materialy.isNotEmpty()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Dodaj")
                            }
                        }

                        if (wybraneMaterialy.isEmpty()) {
                            Text(
                                "Brak dodanych materiałów",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        } else {
                            Spacer(Modifier.height(8.dp))
                            wybraneMaterialy.forEach { (materialId, ilosc) ->
                                val material = viewModel.materialy.find { it.id == materialId }
                                if (material != null) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(material.nazwa, style = MaterialTheme.typography.bodyMedium)
                                            Text(
                                                "$ilosc ${material.jednostka} × ${String.format("%.2f", material.cenaZaJednostke)} zł = ${String.format("%.2f", ilosc * material.cenaZaJednostke)} zł",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                wybraneMaterialy = wybraneMaterialy.filter { it.first != materialId }
                                            }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                    Divider()
                                }
                            }
                        }
                    }
                }
            }
        }

        // Przycisk zapisu
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                val godzinyInt = godziny.toIntOrNull() ?: 8
                val kosztDouble = kosztDojazdu.replace(",", ".").toDoubleOrNull() ?: 0.0
                val materialyDoPracy = wybraneMaterialy.map { (materialId, ilosc) ->
                    PracaMaterial(materialId = materialId, ilosc = ilosc)
                }

                if (wybranyBudynekId != null) {
                    viewModel.dodajPrace(
                        budynekId = wybranyBudynekId!!,
                        data = data,
                        opis = opis,
                        godziny = godzinyInt,
                        kosztDojazdu = kosztDouble,
                        vat = vat,
                        wybraneMaterialy = materialyDoPracy
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = wybranyBudynekId != null && opis.isNotBlank() && !viewModel.isLoading
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Zapisz pracę")
        }
    }

    // Dialog dodawania materiału
    if (pokazMaterialDialog) {
        DodajMaterialDialog(
            materialy = viewModel.materialy,
            onDodaj = { materialId, ilosc ->
                wybraneMaterialy = wybraneMaterialy.filter { it.first != materialId } + (materialId to ilosc)
                pokazMaterialDialog = false
            },
            onDismiss = { pokazMaterialDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DodajMaterialDialog(
    materialy: List<Material>,
    onDodaj: (Long, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var wybranyMaterialId by remember { mutableStateOf<Long?>(null) }
    var ilosc by remember { mutableStateOf("") }
    var rozwiniety by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj materiał") },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = rozwiniety,
                    onExpandedChange = { rozwiniety = it }
                ) {
                    OutlinedTextField(
                        value = materialy.find { it.id == wybranyMaterialId }?.nazwa ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Materiał") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rozwiniety) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = rozwiniety,
                        onDismissRequest = { rozwiniety = false }
                    ) {
                        materialy.forEach { material ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(material.nazwa)
                                        Text(
                                            "${material.cenaZaJednostke} zł/${material.jednostka}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                },
                                onClick = {
                                    wybranyMaterialId = material.id
                                    rozwiniety = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = ilosc,
                    onValueChange = { ilosc = it },
                    label = {
                        val jednostka = materialy.find { it.id == wybranyMaterialId }?.jednostka ?: "szt"
                        Text("Ilość ($jednostka)")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val iloscDouble = ilosc.replace(",", ".").toDoubleOrNull()
                    if (wybranyMaterialId != null && iloscDouble != null && iloscDouble > 0) {
                        onDodaj(wybranyMaterialId!!, iloscDouble)
                    }
                },
                enabled = wybranyMaterialId != null && ilosc.isNotBlank()
            ) {
                Text("Dodaj")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}