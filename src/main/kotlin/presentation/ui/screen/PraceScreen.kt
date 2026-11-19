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
import pl.rafapp.marko.appendixCreator.presentation.viewmodel.BudynkiViewModel
import pl.rafapp.marko.appendixCreator.presentation.viewmodel.PraceViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.Month
import java.time.ZoneOffset
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import pl.rafapp.marko.appendixCreator.presentation.viewmodel.MaterialyViewModel

/**
 * Ekran zarządzania pracami
 * Najbardziej złożony - zawiera wybór budynku, datę i materiały
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PraceScreen(viewModel: PraceViewModel, budynkiViewModel: BudynkiViewModel, materialyViewModel: MaterialyViewModel) {
    var pokazFormularz by remember { mutableStateOf(false) }
    var pracaDoUsuniecia by remember { mutableStateOf<Praca?>(null) }
    var pracaDoEdycji by remember { mutableStateOf<Praca?>(null) }

    LaunchedEffect(Unit) { viewModel.zaladujDane() }

    if (pokazFormularz || pracaDoEdycji != null) {
        FormularzPracy(
            viewModel = viewModel,
            budynkiViewModel = budynkiViewModel,
            materialyViewModel = materialyViewModel,
            pracaDoEdycji = pracaDoEdycji,
            onClose = {
                pokazFormularz = false
                pracaDoEdycji = null
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

            // Nagłówek z przyciskiem dodawania
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Lista prac (${viewModel.przefiltrowanePrace.size})",
                    style = MaterialTheme.typography.titleMedium
                )

                Button(
                    onClick = { viewModel.clearMessages(); pokazFormularz = true },
                    enabled = !viewModel.isLoading
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Dodaj pracę")
                }
            }

            PraceFiltry(viewModel)

            // Lista prac
            if (viewModel.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (viewModel.przefiltrowanePrace.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        "Brak prac w wybranym okresie",
                        modifier = Modifier.padding(32.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.przefiltrowanePrace) { praca ->
                        PracaCard(
                            praca = praca,
                            budynek = viewModel.budynki.find { it.id == praca.budynekId },
                            materialy = viewModel.materialy,
                            onDelete = { pracaDoUsuniecia = praca },
                            onEdit = { viewModel.clearMessages(); pracaDoEdycji = praca }
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
    onDelete: () -> Unit,
    onEdit: () -> Unit
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
                        budynek?.displayNazwa() ?: "Budynek usunięty",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        praca.data.format(formatter),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (!praca.nrFaktury.isNullOrBlank()) {
                        Text(
                            "Faktura: ${praca.nrFaktury}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(
                        praca.opis,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edytuj", tint = MaterialTheme.colorScheme.primary)
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
                            "• ${material.nazwa}: ${pracaMaterial.ilosc} ${material.jednostka} × ${
                                String.format(
                                    "%.2f",
                                    material.cenaZaJednostke
                                )
                            } zł = ${String.format("%.2f", pracaMaterial.ilosc * material.cenaZaJednostke)} zł",
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
    budynkiViewModel: BudynkiViewModel,
    materialyViewModel: MaterialyViewModel,
    pracaDoEdycji: Praca? = null,
    onClose: () -> Unit
) {
    var wybranyBudynekId by remember { mutableStateOf(pracaDoEdycji?.budynekId) }
    var data by remember { mutableStateOf(pracaDoEdycji?.data ?: LocalDate.now()) }
    var opis by remember { mutableStateOf(pracaDoEdycji?.opis ?: "") }
    var godziny by remember { mutableStateOf(pracaDoEdycji?.roboczogodziny?.toString() ?: "3") }
    var czyDojazd by remember { mutableStateOf((pracaDoEdycji?.kosztDojazdu ?: 0.0) > 0) }
    var vat by remember { mutableStateOf(pracaDoEdycji?.vat ?: 23) }
    var nrFaktury by remember {
        mutableStateOf(pracaDoEdycji?.nrFaktury ?: viewModel.sugerujNrFaktury(wybranyBudynekId, data))
    }
    var wybraneMaterialy by remember {
        mutableStateOf(pracaDoEdycji?.materialy?.map { it.materialId to it.ilosc } ?: emptyList<Pair<Long, Double>>())
    }
    val isEditing = pracaDoEdycji != null

    LaunchedEffect(wybranyBudynekId, data) {
        if (!isEditing) nrFaktury = viewModel.sugerujNrFaktury(wybranyBudynekId, data)
    }

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
                if (isEditing) "Edytuj pracę" else "Nowa praca",
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

                        WyborBudynku(
                            budynki = viewModel.budynki,
                            wybranyId = wybranyBudynekId,
                            onWybierz = { wybranyBudynekId = it.id },
                            onWyczysc = { wybranyBudynekId = null },
                            onDodaj = { ulica ->
                                budynkiViewModel.dodajBudynek("Katowice", ulica) { nowy ->
                                    wybranyBudynekId = nowy.id
                                    viewModel.zaladujDane()
                                }
                            }
                        )
                    }
                }
            }

            // Data i podstawowe dane
            item {
                Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("2. Szczegóły pracy", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = nrFaktury,
                            onValueChange = { nrFaktury = it },
                            label = { Text("Nr faktury") },
                            placeholder = { Text("np. 1/2026") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Dojazd (${String.format("%.2f", viewModel.domyslnyKosztDojazdu)} zł)",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Switch(checked = czyDojazd, onCheckedChange = { czyDojazd = it })
                        }

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
                                                "$ilosc ${material.jednostka} × ${
                                                    String.format(
                                                        "%.2f",
                                                        material.cenaZaJednostke
                                                    )
                                                } zł = ${String.format("%.2f", ilosc * material.cenaZaJednostke)} zł",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                wybraneMaterialy = wybraneMaterialy.filter { it.first != materialId }
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Usuń",
                                                tint = MaterialTheme.colorScheme.error
                                            )
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
                val godzinyInt = godziny.toIntOrNull() ?: 3
                val materialyDoPracy = wybraneMaterialy.map { (materialId, ilosc) ->
                    PracaMaterial(materialId = materialId, ilosc = ilosc)
                }
                if (wybranyBudynekId != null) {
                    val praca = Praca(
                        id = pracaDoEdycji?.id ?: 0,
                        budynekId = wybranyBudynekId!!,
                        data = data,
                        opis = opis,
                        roboczogodziny = godzinyInt,
                        kosztDojazdu = if (czyDojazd) viewModel.domyslnyKosztDojazdu else 0.0,
                        vat = vat,
                        nrFaktury = nrFaktury,
                        materialy = materialyDoPracy
                    )
                    if (isEditing) viewModel.edytujPrace(praca)
                    else viewModel.dodajPrace(
                        budynekId = wybranyBudynekId!!,
                        data = data,
                        opis = opis,
                        godziny = godzinyInt,
                        kosztDojazdu = if (czyDojazd) viewModel.domyslnyKosztDojazdu else 0.0,
                        vat = vat,
                        nrFaktury = nrFaktury,
                        wybraneMaterialy = materialyDoPracy
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = wybranyBudynekId != null && opis.isNotBlank() && !viewModel.isLoading
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (isEditing) "Zapisz zmiany" else "Zapisz pracę")
        }
    }

    // Dialog wyboru daty
    if (pokazDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = data.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { pokazDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        data = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    pokazDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { pokazDatePicker = false }) { Text("Anuluj") } }
        ) { DatePicker(state = state) }
    }

    // Dialog dodawania materiału
    if (pokazMaterialDialog) {
        DodajMaterialDialog(
            materialy = viewModel.materialy,
            onDodaj = { materialId, ilosc ->
                wybraneMaterialy = wybraneMaterialy.filter { it.first != materialId } + (materialId to ilosc)
                pokazMaterialDialog = false
            },
            onUtworz = { nazwa, jednostka, cena, onGotowe ->
                materialyViewModel.dodajMaterial(nazwa, jednostka, cena) { nowy ->
                    materialyViewModel.clearMessages()
                    viewModel.zaladujDane()
                    onGotowe(nowy)
                }
            },
            onZmienCene = { material, cena, tylkoDlaPracy, onGotowe ->
                if (tylkoDlaPracy) {
                    // TODO: cena tylko dla tej pracy – wymaga pola cenaNadpisana w PracaMaterial,
                    //  kolumny w PracaMaterialEntity oraz uwzględnienia w RaportBuilder
                } else {
                    materialyViewModel.aktualizujMaterial(material.id, material.nazwa, material.jednostka, cena) { zaktualizowany ->
                        materialyViewModel.clearMessages()
                        viewModel.zaladujDane()
                        onGotowe(zaktualizowany)
                    }
                }
            },
            onDismiss = { pokazMaterialDialog = false }
        )
    }
}

/** Przełącznik checkboxa "tylko dla tej pracy". TODO: włączyć po dodaniu cenaNadpisana w PracaMaterial i bazie. */
private const val CENA_TYLKO_DLA_PRACY_DOSTEPNA = false

/** Tryby dialogu: szukanie/wybór materiału, tworzenie nowego, zmiana ceny wybranego. */
private enum class TrybMaterialu { WYSZUKIWANIE, NOWY, ZMIANA_CENY }

/**
 * Dialog dodawania materiału do pracy. Wyszukiwanie z podpowiedziami pod polem (bez popupu),
 * opcja "Dodaj materiał" (nazwa + jednostka z listy + cena) oraz "Zmień cenę" wybranego materiału.
 * Nie zna ViewModeli - całość operacji na bazie przekazana przez callbacki.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DodajMaterialDialog(
    materialy: List<Material>,
    onDodaj: (Long, Double) -> Unit,
    onUtworz: (nazwa: String, jednostka: String, cena: Double, onGotowe: (Material) -> Unit) -> Unit,
    onZmienCene: (material: Material, cena: Double, tylkoDlaPracy: Boolean, onGotowe: (Material) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    var szukaj by remember { mutableStateOf("") }
    var wybrany by remember { mutableStateOf<Material?>(null) }
    var tryb by remember { mutableStateOf(TrybMaterialu.WYSZUKIWANIE) }
    var ilosc by remember { mutableStateOf("") }
    var jednostka by remember { mutableStateOf(Jednostki.DOSTEPNE.first()) }
    var cena by remember { mutableStateOf("") }
    var tylkoDlaPracy by remember { mutableStateOf(false) }

    val tekst = szukaj.trim()
    val szukamy = wybrany == null && tekst.isNotEmpty() && tryb == TrybMaterialu.WYSZUKIWANIE
    val pasujace = if (szukamy) materialy.filter { it.nazwa.contains(tekst, ignoreCase = true) } else emptyList()
    val jestDokladny = pasujace.any { it.nazwa.equals(tekst, ignoreCase = true) }
    val cenaDouble = cena.replace(",", ".").toDoubleOrNull()
    val iloscDouble = ilosc.replace(",", ".").toDoubleOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj materiał") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = szukaj,
                    onValueChange = {
                        szukaj = it
                        wybrany = null
                        tryb = TrybMaterialu.WYSZUKIWANIE
                    },
                    label = { Text("Materiał") },
                    placeholder = { Text("Wpisz nazwę...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Podpowiedzi + dodanie nowego
                if (szukamy) {
                    pasujace.take(5).forEach { m ->
                        TextButton(
                            onClick = { wybrany = m; szukaj = m.nazwa },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "${m.nazwa} (${String.format("%.2f", m.cenaZaJednostke)} zł/${m.jednostka})",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    if (!jestDokladny) {
                        TextButton(
                            onClick = { cena = ""; tryb = TrybMaterialu.NOWY },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Dodaj materiał: $tekst", modifier = Modifier.weight(1f))
                        }
                    }
                }

                // Tworzenie nowego materiału
                if (tryb == TrybMaterialu.NOWY) {
                    Spacer(Modifier.height(8.dp))
                    Text("Jednostka", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Jednostki.DOSTEPNE.forEach { j ->
                            FilterChip(selected = jednostka == j, onClick = { jednostka = j }, label = { Text(j) })
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cena,
                        onValueChange = { cena = it },
                        label = { Text("Cena za jednostkę (zł)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            onUtworz(tekst, jednostka, cenaDouble!!) { nowy ->
                                wybrany = nowy
                                szukaj = nowy.nazwa
                                tryb = TrybMaterialu.WYSZUKIWANIE
                            }
                        },
                        enabled = cenaDouble != null && cenaDouble >= 0,
                        modifier = Modifier.align(Alignment.End)
                    ) { Text("Utwórz materiał") }
                }

                // Wybrany materiał: cena + ilość
                wybrany?.let { m ->
                    if (tryb == TrybMaterialu.WYSZUKIWANIE) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${String.format("%.2f", m.cenaZaJednostke)} zł / ${m.jednostka}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            TextButton(onClick = {
                                cena = String.format("%.2f", m.cenaZaJednostke)
                                tylkoDlaPracy = false
                                tryb = TrybMaterialu.ZMIANA_CENY
                            }) { Text("Zmień cenę") }
                        }
                        OutlinedTextField(
                            value = ilosc,
                            onValueChange = { ilosc = it },
                            label = { Text("Ilość (${m.jednostka})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    // Zmiana ceny
                    if (tryb == TrybMaterialu.ZMIANA_CENY) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = cena,
                            onValueChange = { cena = it },
                            label = { Text("Nowa cena za ${m.jednostka} (zł)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        if (CENA_TYLKO_DLA_PRACY_DOSTEPNA) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = tylkoDlaPracy, onCheckedChange = { tylkoDlaPracy = it })
                                Text("Tylko dla tej pracy", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                        ) {
                            TextButton(onClick = { tryb = TrybMaterialu.WYSZUKIWANIE }) { Text("Anuluj") }
                            Button(
                                onClick = {
                                    onZmienCene(m, cenaDouble!!, tylkoDlaPracy) { zaktualizowany ->
                                        wybrany = zaktualizowany
                                        tryb = TrybMaterialu.WYSZUKIWANIE
                                    }
                                },
                                enabled = cenaDouble != null && cenaDouble >= 0
                            ) { Text("Zapisz cenę") }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onDodaj(wybrany!!.id, iloscDouble!!) },
                enabled = wybrany != null &&
                        tryb == TrybMaterialu.WYSZUKIWANIE &&
                        iloscDouble != null && iloscDouble > 0
            ) { Text("Dodaj") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } }
    )
}

/**
 * Pole wyszukiwania budynku. Podpowiedzi oraz opcja "Dodaj budynek"
 * wyświetlane są pod polem (bez popupu), dzięki czemu nie ma problemów z fokusem.
 */
@Composable
fun WyborBudynku(
    budynki: List<Budynek>,
    wybranyId: Long?,
    onWybierz: (Budynek) -> Unit,
    onWyczysc: () -> Unit,
    onDodaj: (String) -> Unit
) {
    var szukaj by remember { mutableStateOf(budynki.find { it.id == wybranyId }?.displayNazwa() ?: "") }
    val tekst = szukaj.trim()
    val szukamy = wybranyId == null && tekst.isNotEmpty()
    val pasujace = if (szukamy) budynki.filter { it.ulica.contains(tekst, ignoreCase = true) } else emptyList()
    val jestDokladny = pasujace.any { it.ulica.equals(tekst, ignoreCase = true) }

    OutlinedTextField(
        value = szukaj,
        onValueChange = { szukaj = it; onWyczysc() },
        label = { Text("Budynek") },
        placeholder = { Text("Wpisz ulicę...") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    if (szukamy) {
        pasujace.take(5).forEach { b ->
            TextButton(
                onClick = { szukaj = b.displayNazwa(); onWybierz(b) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(b.displayNazwa(), modifier = Modifier.weight(1f))
            }
        }
        if (!jestDokladny) {
            TextButton(onClick = { onDodaj(tekst) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Dodaj budynek: $tekst", modifier = Modifier.weight(1f))
            }
        }
    }
}

private fun nazwaMiesiaca(m: Int) =
    Month.of(m).getDisplayName(TextStyle.FULL_STANDALONE, Locale("pl"))
        .replaceFirstChar { it.uppercase() }

/** Panel filtrów listy prac: miesiąc, rok i budynek. */
@Composable
fun PraceFiltry(viewModel: PraceViewModel) {
    val okres = viewModel.wybranyOkres
    val opcjeBudynkow = buildList<Pair<Long?, String>> {
        add(null to "Wszystkie")
        viewModel.budynkiWOkresie.forEach { add(it.id to it.displayNazwa()) }
    }

    Column(
        modifier = Modifier.padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FiltrDropdown(
                label = "Miesiąc",
                wybrany = nazwaMiesiaca(okres.monthValue),
                opcje = viewModel.dostepneMiesiace(okres.year).map { it to nazwaMiesiaca(it) },
                onWybierz = viewModel::ustawMiesiac,
                modifier = Modifier.weight(1f)
            )
            FiltrDropdown(
                label = "Rok",
                wybrany = okres.year.toString(),
                opcje = viewModel.dostepneLata.map { it to it.toString() },
                onWybierz = viewModel::ustawRok,
                modifier = Modifier.weight(1f)
            )
        }
        FiltrDropdown(
            label = "Budynek",
            wybrany = opcjeBudynkow.find { it.first == viewModel.filtrBudynekId }?.second ?: "Wszystkie",
            opcje = opcjeBudynkow,
            onWybierz = viewModel::ustawFiltrBudynku,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** Generyczna lista rozwijana tylko do wyboru (read-only), używana przez filtry. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> FiltrDropdown(
    label: String,
    wybrany: String,
    opcje: List<Pair<T, String>>,
    onWybierz: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var rozwiniety by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = rozwiniety,
        onExpandedChange = { rozwiniety = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = wybrany,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rozwiniety) },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            singleLine = true
        )
        ExposedDropdownMenu(expanded = rozwiniety, onDismissRequest = { rozwiniety = false }) {
            opcje.forEach { (wartosc, tekst) ->
                DropdownMenuItem(
                    text = { Text(tekst) },
                    onClick = { onWybierz(wartosc); rozwiniety = false }
                )
            }
        }
    }
}