package pl.rafapp.marko.appendixCreator.presentation.ui.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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

            OutlinedTextField(
                value = viewModel.nrFaktury,
                onValueChange = { viewModel.ustawNrFaktury(it) },
                label = { Text("Nr faktury") },
                placeholder = { Text("np. 1/2026") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

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

    Card(elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Załącznik do faktury nr ${dane.numerFaktury}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Kalkulacja kosztów czynności wykonanych przez F.H.U. Marko Marek Grabowski",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text("Dotyczy ul. ${dane.budynek.pelnyAdres}", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "Stawka roboczogodziny ${dane.stawkaRoboczogodziny.toInt()} zł",
                        style = MaterialTheme.typography.bodySmall
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

            Spacer(Modifier.height(12.dp))

            Box(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                Column {
                    TabelaNaglowek()
                    dane.wiersze.forEachIndexed { idx, wiersz ->
                        TabelaWierszZlecenia(idx + 1, wiersz)
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                    TabelaSuma(dane)
                }
            }
        }
    }
}

// Szerokości kolumn
private val colLP = 28.dp
private val colData = 72.dp
private val colUsluga = 210.dp
private val colGodz = 48.dp
private val colWartRob = 64.dp
private val colNazwaMat = 130.dp
private val colIloscMat = 44.dp
private val colJedn = 44.dp
private val colWartMat = 64.dp
private val colKoszt8 = 56.dp
private val colTransNazwa = 64.dp
private val colTransIl = 32.dp
private val colTransWart = 52.dp
private val colSuma = 68.dp
private val colVat = 40.dp

@Composable
private fun TabelaNaglowek() {
    val s = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(modifier = Modifier.padding(vertical = 3.dp, horizontal = 2.dp)) {
            listOf(
                colLP to "LP", colData to "Data", colUsluga to "Usługa",
                colGodz to "Godz", colWartRob to "Roboc.",
                colNazwaMat to "Materiał", colIloscMat to "Il.", colJedn to "Jedn.",
                colWartMat to "W.mat", colKoszt8 to "8%",
                colTransNazwa to "Transp.", colTransIl to "Il.", colTransWart to "W.tr.",
                colSuma to "Suma", colVat to "VAT"
            ).forEach { (w, label) ->
                Text(label, style = s, modifier = Modifier.width(w).padding(horizontal = 2.dp))
            }
        }
    }
}

@Composable
private fun TabelaWierszZlecenia(lp: Int, wiersz: WierszRaportu) {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yy")
    val s = MaterialTheme.typography.bodySmall
    val rows = maxOf(1, wiersz.materialy.size)

    (0 until rows).forEach { i ->
        val isFirst = i == 0
        val isLast = i == rows - 1
        val mat = wiersz.materialy.getOrNull(i)

        Row(modifier = Modifier.padding(vertical = 1.dp, horizontal = 2.dp)) {
            Text(if (isFirst) lp.toString() else "", style = s, modifier = Modifier.width(colLP))
            Text(
                if (isFirst) wiersz.data.format(formatter) else "",
                style = s.copy(color = MaterialTheme.colorScheme.primary),
                modifier = Modifier.width(colData)
            )
            Text(
                if (isFirst) wiersz.opis else "",
                style = s,
                modifier = Modifier.width(colUsluga),
                maxLines = 4,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                if (isLast) wiersz.roboczogodziny.toString() else "",
                style = s,
                modifier = Modifier.width(colGodz),
                textAlign = TextAlign.End
            )
            Text(
                if (isLast) formatKwota(wiersz.kosztRobocizny) else "",
                style = s,
                modifier = Modifier.width(colWartRob),
                textAlign = TextAlign.End
            )
            Text(mat?.nazwa ?: "", style = s, modifier = Modifier.width(colNazwaMat), maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            Text(
                mat?.let { formatKwota(it.ilosc) } ?: "",
                style = s,
                modifier = Modifier.width(colIloscMat),
                textAlign = TextAlign.End
            )
            Text(mat?.jednostka ?: "", style = s, modifier = Modifier.width(colJedn))
            Text(
                mat?.let { formatKwota(it.kosztCalkowity) } ?: "",
                style = s,
                modifier = Modifier.width(colWartMat),
                textAlign = TextAlign.End
            )
            Text(
                mat?.let { formatKwota(it.kosztCalkowity * 0.08) } ?: "",
                style = s,
                modifier = Modifier.width(colKoszt8),
                textAlign = TextAlign.End
            )
            Text(
                if (isLast && wiersz.kosztDojazdu > 0) "dojazd" else "",
                style = s,
                modifier = Modifier.width(colTransNazwa)
            )
            Text(
                if (isLast && wiersz.kosztDojazdu > 0) "1" else "",
                style = s,
                modifier = Modifier.width(colTransIl),
                textAlign = TextAlign.Center
            )
            Text(
                if (isLast && wiersz.kosztDojazdu > 0) formatKwota(wiersz.kosztDojazdu) else "",
                style = s,
                modifier = Modifier.width(colTransWart),
                textAlign = TextAlign.End
            )
            Text(
                if (isLast) formatKwota(wiersz.kosztBrutto) else "",
                style = s.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.width(colSuma),
                textAlign = TextAlign.End
            )
            Text(
                if (isLast) "${wiersz.vat}%" else "",
                style = s,
                modifier = Modifier.width(colVat),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TabelaSuma(dane: DaneRaportu) {
    val labelWidth = colLP + colData + colUsluga + colGodz + colWartRob +
            colNazwaMat + colIloscMat + colJedn + colWartMat + colKoszt8 +
            colTransNazwa + colTransIl + colTransWart
    Surface(color = MaterialTheme.colorScheme.primaryContainer) {
        Row(modifier = Modifier.padding(vertical = 3.dp, horizontal = 2.dp)) {
            Text(
                "RAZEM",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.width(labelWidth)
            )
            Text(
                formatKwota(dane.sumaBrutto) + " zł",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.width(colSuma),
                textAlign = TextAlign.End
            )
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