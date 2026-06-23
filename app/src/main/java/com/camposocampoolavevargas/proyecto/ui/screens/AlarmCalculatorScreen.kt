package com.camposocampoolavevargas.proyecto.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import java.util.Calendar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmCalculatorScreen(
    navController: NavController,
    viewModel: AlarmCalculatorViewModel = hiltViewModel()
) {
    val wakeHour by viewModel.wakeHour.collectAsState()
    val wakeMinute by viewModel.wakeMinute.collectAsState()
    val sleepWindows by viewModel.sleepWindows.collectAsState()
    val alarmSet by viewModel.alarmSet.collectAsState()
    val selectedDays by viewModel.selectedDays.collectAsState()
    val selectedWindow by viewModel.selectedWindow.collectAsState()

    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = wakeHour,
        initialMinute = wakeMinute
    )

    Scaffold(
        topBar= {
            TopAppBar(
                title = {
                    Text("Calculadora de Sueño")
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- HORA DE DESPERTAR ---
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "¿A qué hora quieres despertar?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = String.format("%02d:%02d", wakeHour, wakeMinute),
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { showTimePicker = true }) {
                            Text("Cambiar hora")
                        }
                    }
                }
            }

            // --- DÍAS DE RECURRENCIA ---
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Repetir",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val days = listOf(
                            Calendar.MONDAY to "L",
                            Calendar.TUESDAY to "M",
                            Calendar.WEDNESDAY to "Mi",
                            Calendar.THURSDAY to "J",
                            Calendar.FRIDAY to "V",
                            Calendar.SATURDAY to "S",
                            Calendar.SUNDAY to "D"
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            days.forEach { (day, label) ->
                                FilterChip(
                                    selected = selectedDays.contains(day),
                                    onClick = { viewModel.toggleDay(day) },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                }
            }

            // --- VENTANAS DE SUEÑO ---
            item {
                Text(
                    text = "Horarios recomendados para acostarte",

                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                selectedWindow?.let { window ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Horario seleccionado",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = window.bedtime,
                                style = MaterialTheme.typography.headlineMedium
                            )

                            Text(
                                text = "${window.cyclesCount} ciclos de sueño"
                            )
                        }
                    }
                }
            }

            if (sleepWindows.isEmpty()) {
                item {
                    Text(
                        text = "Selecciona una hora de despertar para ver las ventanas de sueño.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(sleepWindows) { window ->

                    val isSelected = selectedWindow == window

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        onClick = {
                            viewModel.selectWindow(window)
                        },
                        colors = CardDefaults.cardColors(
                            containerColor =
                                if (isSelected)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected)
                            androidx.compose.foundation.BorderStroke(
                                2.dp,
                                MaterialTheme.colorScheme.primary
                            )
                        else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = window.bedtime,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color =
                                        if (isSelected)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                )

                                Text(
                                    text = "${window.cyclesCount} ciclos · ${window.cyclesCount * 90} min",
                                    style = MaterialTheme.typography.bodySmall,
                                    color =
                                        if (isSelected)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = if (window.cyclesCount >= 5) "⭐ Ideal" else "✓ OK",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }


            // --- BOTÓN ALARMA ---
            item {
                Button(
                    onClick = {
                        if (alarmSet) {
                            viewModel.cancelAlarm()
                        } else {
                            viewModel.setAlarm()
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (alarmSet)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (alarmSet) "Cancelar Alarma" else "Guardar y Activar Alarma",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // --- TIME PICKER DIALOG ---
        if (showTimePicker) {
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.updateWakeTime(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    }) { Text("Confirmar") }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") }
                },
                text = { TimePicker(state = timePickerState) }
            )
        }
    }
}