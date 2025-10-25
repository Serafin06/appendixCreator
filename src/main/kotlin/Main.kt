package pl.rafapp.appendixCreator

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.hibernate.internal.util.collections.CollectionHelper.listOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Budynki", "Materiały", "Prace")

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Appendix Creator 🏗️") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (selectedTab) {
                        0 -> BudynkiContent()
                        1 -> MaterialyContent()
                        2 -> PraceContent()
                    }
                }
            }
        }
    }
}

@Composable
fun BudynkiContent() {
    var adres by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Zarządzanie budynkami", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = adres,
            onValueChange = { adres = it },
            label = { Text("Adres budynku") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            if (adres.isNotBlank()) {
                println("Dodano budynek: $adres")
                adres = ""
            }
        }) {
            Text("Dodaj budynek")
        }
    }
}

@Composable
fun MaterialyContent() {
    Text("Materiały - wkrótce", style = MaterialTheme.typography.headlineSmall)
}

@Composable
fun PraceContent() {
    Text("Prace - wkrótce", style = MaterialTheme.typography.headlineSmall)
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Appendix Creator"
    ) {
        App()
    }
}