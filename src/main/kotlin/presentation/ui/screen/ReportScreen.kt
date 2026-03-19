package pl.rafapp.marko.appendixCreator.presentation.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pl.rafapp.marko.appendixCreator.application.usecase.raport.DaneRaportu
import pl.rafapp.marko.appendixCreator.application.usecase.raport.DaneRaportuZbiorczego
import pl.rafapp.marko.appendixCreator.application.usecase.raport.WierszRaportu
import pl.rafapp.marko.appendixCreator.presentation.viewmodel.RaportViewModel
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Ekran generowania raportów miesięcznych
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaportScreen(viewModel: RaportViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Komunikaty
        viewModel.errorMessage?.let { error ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Text(error, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(16.dp))
            }
        }

        viewModel.successMessage?.let { success ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Text(success, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(16.dp))
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { PanelKonfiguracji(viewModel) }

            viewModel.daneRaportuZbiorczego?.let { dane ->
                item { PodgladRaportuZbiorczego(dane, viewModel) }
            }

            viewModel.daneRaportu?.let { dane ->
                item { PodgladRaportu(dane, viewModel) }
            }
        }

        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelKonfiguracji(viewModel: RaportViewModel) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Parametry raportu",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // === Selector typu raportu ===
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RaportViewModel.TypRaportu.entries.forEach { typ ->
                    FilterChip(
                        selected = viewModel.typRaportu == typ,
                        onClick = { viewModel.ustawTypRaportu(typ) },
                        label = {
                            Text(
                                when (typ) {
                                    RaportViewModel.TypRaportu.POJEDYNCZY_BUDYNEK -> "Budynek"
                                    RaportViewModel.TypRaportu.ZBIORCZY_MIESIAC -> "Zbiorczy"
                                    RaportViewModel.TypRaportu.WSZYSTKIE_BUDYNKI -> "Wszystkie"
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // === Dropdown budynku - tylko dla trybu pojedynczego ===
            if (viewModel.typRaportu == RaportViewModel.TypRaportu.POJEDYNCZY_BUDYNEK) {
                var budynekRozwiniety by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = budynekRozwiniety,
                    onExpandedChange = { budynekRozwiniety = it }
                ) {
                    OutlinedTextField(
                        value = viewModel.budynki.find { it.id == viewModel.wybranyBudynekId }?.pelnyAdres ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Budynek") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = budynekRozwiniety) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = budynekRozwiniety,
                        onDismissRequest = { budynekRozwiniety = false }
                    ) {
                        viewModel.budynki.forEach { budynek ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(budynek.ulica)
                                        Text(budynek.miasto, style = MaterialTheme.typography.bodySmall)
                                    }
                                },
                                onClick = {
                                    viewModel.wybierzBudynek(budynek.id)
                                    budynekRozwiniety = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
            }

            // === Rok i miesiąc ===
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                var miesiacRozwiniety by remember { mutableStateOf(false) }
                val miesiace = (1..12).map { m ->
                    m to java.time.Month.of(m)
                        .getDisplayName(TextStyle.FULL_STANDALONE, Locale("pl"))
                        .replaceFirstChar { it.uppercase() }
                }

                ExposedDropdownMenuBox(
                    expanded = miesiacRozwiniety,
                    onExpandedChange = { miesiacRozwiniety = it },
                    modifier = Modifier.weight(2f)
                ) {
                    OutlinedTextField(
                        value = miesiace.find { it.first == viewModel.wybranyMiesiac }?.second ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Miesiąc") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = miesiacRozwiniety) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = miesiacRozwiniety,
                        onDismissRequest = { miesiacRozwiniety = false }
                    ) {
                        miesiace.forEach { (num, nazwa) ->
                            DropdownMenuItem(
                                text = { Text(nazwa) },
                                onClick = {
                                    viewModel.ustawMiesiac(num)
                                    miesiacRozwiniety = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = viewModel.wybranyRok.toString(),
                    onValueChange = { it.toIntOrNull()?.let { rok -> viewModel.ustawRok(rok) } },
                    label = { Text("Rok") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(Modifier.height(12.dp))

            // === Stawki ===
            OutlinedTextField(
                value = viewModel.stawkaRoboczogodziny,
                onValueChange = { viewModel.ustawStawke(it) },
                label = { Text("Stawka roboczogodziny (zł/h)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = viewModel.kosztDojazdu,
                onValueChange = { viewModel.ustawKosztDojazdu(it) },
                label = { Text("Domyślny koszt dojazdu (zł)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { viewModel.zapiszStawke() },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Zapisz ustawienia")
            }

            Spacer(Modifier.height(16.dp))

            // === Przycisk generowania ===
            val generujEnabled = when (viewModel.typRaportu) {
                RaportViewModel.TypRaportu.POJEDYNCZY_BUDYNEK -> viewModel.wybranyBudynekId != null
                else -> true
            } && !viewModel.isLoading

            Button(
                onClick = { viewModel.generujPodglad() },
                modifier = Modifier.fillMaxWidth(),
                enabled = generujEnabled
            ) {
                Icon(Icons.Default.Assessment, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Generuj podgląd raportu")
            }
        }
    }
}

// ===================== PODGLĄD POJEDYNCZEGO BUDYNKU =====================

@Composable
fun PodgladRaportu(dane: DaneRaportu, viewModel: RaportViewModel) {
    val nazwaeMiesiaca = java.time.Month.of(dane.miesiac)
        .getDisplayName(TextStyle.FULL_STANDALONE, Locale("pl"))
        .replaceFirstChar { it.uppercase() }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Podgląd raportu", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "$nazwaeMiesiaca ${dane.rok} • ${dane.budynek.pelnyAdres}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = {
                        val chooser = javax.swing.JFileChooser().apply {
                            dialogTitle = "Wybierz folder zapisu"
                            fileSelectionMode = javax.swing.JFileChooser.DIRECTORIES_ONLY
                        }
                        if (chooser.showSaveDialog(null) == javax.swing.JFileChooser.APPROVE_OPTION) {
                            viewModel.exportExcel(chooser.selectedFile)
                        }
                    },
                    enabled = !viewModel.isLoading
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Export to Excel")
                }
            }

            Spacer(Modifier.height(16.dp))
            Divider()
            Spacer(Modifier.height(16.dp))

            dane.wiersze.forEach { wiersz ->
                WierszPracy(wiersz)
                Spacer(Modifier.height(8.dp))
                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(8.dp))
            PodsumowanieRaportu(dane)
        }
    }
}

@Composable
fun WierszPracy(wiersz: WierszRaportu) {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                wiersz.data.format(formatter),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text("VAT: ${wiersz.vat}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(Modifier.height(4.dp))
        Text(wiersz.opis, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Robocizna: ${wiersz.roboczogodziny}h × ${formatKwota(wiersz.stawkaRoboczogodziny)} zł/h", style = MaterialTheme.typography.bodySmall)
            Text(formatKwota(wiersz.kosztRobocizny) + " zł", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }

        if (wiersz.kosztDojazdu > 0) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Dojazd:", style = MaterialTheme.typography.bodySmall)
                Text(formatKwota(wiersz.kosztDojazdu) + " zł", style = MaterialTheme.typography.bodySmall)
            }
        }

        if (wiersz.materialy.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text("Materiały:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            wiersz.materialy.forEach { m ->
                Row(modifier = Modifier.fillMaxWidth().padding(start = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("• ${m.nazwa}: ${m.ilosc} ${m.jednostka} × ${formatKwota(m.cenaZaJednostke)} zł", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text(formatKwota(m.kosztCalkowity) + " zł", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text(
                "Netto: ${formatKwota(wiersz.kosztNetto)} zł  |  Brutto: ${formatKwota(wiersz.kosztBrutto)} zł",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun PodsumowanieRaportu(dane: DaneRaportu) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Podsumowanie", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

            PozycjaPodsumowania("Łączne roboczogodziny:", "${dane.sumaRoboczogodzin} h")
            PozycjaPodsumowania("Koszt robocizny:", "${formatKwota(dane.sumaKosztowRobocizny)} zł")
            PozycjaPodsumowania("Koszt dojazdu:", "${formatKwota(dane.sumaKosztowDojazdu)} zł")
            PozycjaPodsumowania("Koszt materiałów:", "${formatKwota(dane.sumaKosztowMaterialow)} zł")

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            PozycjaPodsumowania("Wartość netto:", "${formatKwota(dane.sumaNetto)} zł")
            PozycjaPodsumowania("Kwota VAT:", "${formatKwota(dane.sumaVat)} zł")

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("WARTOŚĆ BRUTTO:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("${formatKwota(dane.sumaBrutto)} zł", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// ===================== PODGLĄD ZBIORCZY =====================

@Composable
fun PodgladRaportuZbiorczego(dane: DaneRaportuZbiorczego, viewModel: RaportViewModel) {
    val nazwaeMiesiaca = java.time.Month.of(dane.miesiac)
        .getDisplayName(TextStyle.FULL_STANDALONE, Locale("pl"))
        .replaceFirstChar { it.uppercase() }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Raport zbiorczy", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "$nazwaeMiesiaca ${dane.rok} • ${dane.wierszeBudynkow.size} budynków",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Przycisk eksportu tylko dla trybu "Wszystkie budynki"
                if (viewModel.typRaportu == RaportViewModel.TypRaportu.WSZYSTKIE_BUDYNKI) {
                    Button(
                        onClick = {
                            val chooser = javax.swing.JFileChooser().apply {
                                dialogTitle = "Wybierz folder zapisu"
                                fileSelectionMode = javax.swing.JFileChooser.DIRECTORIES_ONLY
                            }
                            if (chooser.showSaveDialog(null) == javax.swing.JFileChooser.APPROVE_OPTION) {
                                viewModel.exportExcel(chooser.selectedFile)
                            }
                        },
                        enabled = !viewModel.isLoading
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Eksportuj wszystkie")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Nagłówek tabeli
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text("Budynek", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                Text("Netto 8%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                Text("Netto 23%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                Text("Brutto", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            }

            Divider()

            // Wiersze budynków
            dane.wierszeBudynkow.forEach { wiersz ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(2f)) {
                        Text(wiersz.budynek.ulica, style = MaterialTheme.typography.bodySmall)
                        Text(wiersz.budynek.miasto, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        if (wiersz.nettoVat8 > 0) formatKwota(wiersz.nettoVat8) + " zł" else "—",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                    Text(
                        if (wiersz.nettoVat23 > 0) formatKwota(wiersz.nettoVat23) + " zł" else "—",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                    Text(
                        formatKwota(wiersz.sumaBrutto) + " zł",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
            }

            // Sumy końcowe
            Spacer(Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("SUMA", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                        Text(formatKwota(dane.lacznaNetto8) + " zł", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        Text(formatKwota(dane.lacznaNetto23) + " zł", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        Text(formatKwota(dane.sumaBrutto) + " zł", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    }
                    Spacer(Modifier.height(6.dp))
                    Divider()
                    Spacer(Modifier.height(6.dp))
                    PozycjaPodsumowania("Wartość netto:", "${formatKwota(dane.sumaNetto)} zł")
                    PozycjaPodsumowania("Kwota VAT:", "${formatKwota(dane.sumaVat)} zł")
                    PozycjaPodsumowania("WARTOŚĆ BRUTTO:", "${formatKwota(dane.sumaBrutto)} zł")
                }
            }
        }
    }
}

// ===================== HELPERS =====================

@Composable
fun PozycjaPodsumowania(label: String, wartosc: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(wartosc, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

fun formatKwota(kwota: Double): String {
    return String.format("%.2f", kwota).replace(".", ",")
}