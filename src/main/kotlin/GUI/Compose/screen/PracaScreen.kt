package pl.rafapp.appendixCreator.GUI.Compose.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.rafapp.appendixCreator.GUI.Compose.ViewModel.PracaWizardViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracaWizardScreen(viewModel: PracaWizardViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nowa praca - Krok ${viewModel.state.aktualnyKrok}/3") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            PracaWizardBottomBar(viewModel)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Stepper indicator
            LinearProgressIndicator(
                progress = { viewModel.state.aktualnyKrok / 3f },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )

            // Error message
            viewModel.errorMessage?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Text(
                        error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Content per step
            when (viewModel.state.aktualnyKrok) {
                1 -> Krok1BudynekIData(viewModel)
                2 -> Krok2SzczegolyPracy(viewModel)
                3 -> Krok3Materialy(viewModel)
            }
        }

        if (viewModel.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun PracaWizardBottomBar(viewModel: PracaWizardViewModel) {
    Surface(
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (viewModel.state.aktualnyKrok > 1) {
                OutlinedButton(onClick = { viewModel.poprzedniKrok() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Wstecz")
                }
            } else {
                Spacer(Modifier.width(1.dp))
            }

            Button(
                onClick = { viewModel.nastepnyKrok() },
                enabled = !viewModel.isLoading
            ) {
                Text(
                    if (viewModel.state.aktualnyKrok == 3) "Zapisz" else "Dalej"
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    if (viewModel.state.aktualnyKrok == 3)
                        Icons.Default.Check
                    else
                        Icons.Default.ArrowForward,
                    contentDescription = null
                )
            }
        }
    }
}