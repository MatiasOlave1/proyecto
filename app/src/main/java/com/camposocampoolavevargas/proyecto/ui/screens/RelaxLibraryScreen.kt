package com.camposocampoolavevargas.proyecto.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.camposocampoolavevargas.proyecto.navigation.Screen
import com.camposocampoolavevargas.proyecto.ui.theme.DormiBienUTheme

/**
 * Screen for Relax Library (RF12 — Biblioteca de regulación).
 * Also serves as the bottom bar Bienestar landing screen, providing paths to other wellness tools.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelaxLibraryScreen(navController: NavController) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Bienestar - Relax Library") }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Relax Library",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Implementar RF12 — Biblioteca de regulación",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    // Paths to other wellness/bienestar tools for easy navigation
                    Button(
                        onClick = { navController.navigate(Screen.AlarmCalculator.route) },
                        modifier = Modifier.padding(top = 24.dp)
                    ) {
                        Text(text = "Ir a Calculadora de Ciclos (RF09)")
                    }
                    Button(
                        onClick = { navController.navigate(Screen.DisconnectReminder.route) },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(text = "Ir a Ventana de Desconexión (RF10)")
                    }
                    Button(
                        onClick = { navController.navigate(Screen.Journal.route) },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(text = "Ir a Diario de Preocupaciones (RF11)")
                    }
                    
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.padding(top = 24.dp)
                    ) {
                        Text(text = "Volver")
                    }
                }
            }
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun RelaxLibraryScreenPreview() {
    DormiBienUTheme {
        RelaxLibraryScreen(navController = rememberNavController())
    }
}

