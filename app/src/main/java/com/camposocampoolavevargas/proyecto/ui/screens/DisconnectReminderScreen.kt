package com.camposocampoolavevargas.proyecto.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.camposocampoolavevargas.proyecto.data.repository.DisconnectMode
import com.camposocampoolavevargas.proyecto.ui.screens.disconnect.DisconnectReminderViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisconnectReminderScreen(
    navController: NavController,
    viewModel: DisconnectReminderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración de Desconexión") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        // Icono de volver (puedes añadir uno real si prefieres)
                        Text("<")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Personaliza tu descanso",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Selector de Modo
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Fuente de la hora de dormir",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = uiState.mode == DisconnectMode.WEEKLY_STREAK,
                            onClick = { viewModel.onModeChange(DisconnectMode.WEEKLY_STREAK) }
                        )
                        Text(
                            text = "Racha Semanal (Sugerido)",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = uiState.mode == DisconnectMode.MANUAL,
                            onClick = { viewModel.onModeChange(DisconnectMode.MANUAL) }
                        )
                        Text(
                            text = "Configuración Manual",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Selector de Hora (solo si es manual)
            if (uiState.mode == DisconnectMode.MANUAL) {
                ManualTimePickerSection(
                    bedtimeMillis = uiState.manualBedtimeMillis,
                    onTimeSelected = { h, m -> viewModel.onManualTimeChange(h, m) }
                )
            } else {
                InfoStreakSection(uiState.weeklyGoalBedtimeMillis)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Configuración de la Alarma (Offset)
            AlarmOffsetSection(
                offsetMinutes = uiState.reminderOffsetMinutes,
                onOffsetChange = { viewModel.onOffsetChange(it) },
                onTestNotification = { viewModel.onTestNotification() },
                bedtimeMillis = if (uiState.mode == DisconnectMode.MANUAL) {
                    uiState.manualBedtimeMillis
                } else {
                    uiState.weeklyGoalBedtimeMillis ?: 82800000L
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp)
            ) {
                Text("Guardar y Volver")
            }
        }
    }
}

@Composable
fun AlarmOffsetSection(
    offsetMinutes: Int,
    onOffsetChange: (Int) -> Unit,
    onTestNotification: () -> Unit,
    bedtimeMillis: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Alarma de Desconexión",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "¿Cuántos minutos antes de dormir quieres el aviso?",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$offsetMinutes min",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Row {
                    FilledTonalIconButton(onClick = { if (offsetMinutes > 15) onOffsetChange(offsetMinutes - 15) }) {
                        Text("-15", style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledTonalIconButton(onClick = { if (offsetMinutes < 240) onOffsetChange(offsetMinutes + 15) }) {
                        Text("+15", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Calcular y mostrar la hora real de la alarma
            val alarmTimeMillis = bedtimeMillis - (offsetMinutes * 60000L)
            val now = System.currentTimeMillis()
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.MILLISECOND, alarmTimeMillis.toInt())
                
                // Coincidir con la lógica del scheduler: si ya pasó (o falta < 1 min), es para mañana
                if (timeInMillis <= now + 60000) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            
            val isTomorrow = calendar.get(Calendar.DAY_OF_YEAR) != Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            val dayText = if (isTomorrow) "mañana" else "hoy"
            
            Text(
                text = "La notificación se enviará $dayText a las ${String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))}.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { onTestNotification() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Probar Notificación (5 seg)")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualTimePickerSection(
    bedtimeMillis: Long,
    onTimeSelected: (Int, Int) -> Unit
) {
    val calendar = Calendar.getInstance().apply { 
        timeInMillis = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis + bedtimeMillis
    }
    
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = true
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Selecciona tu hora de dormir",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        TimePicker(state = timePickerState)
        
        // Efecto para actualizar cuando cambie el selector
        LaunchedEffect(timePickerState.hour, timePickerState.minute) {
            onTimeSelected(timePickerState.hour, timePickerState.minute)
        }
    }
}

@Composable
fun InfoStreakSection(goalBedtime: Long?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Acerca de la Racha Semanal",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            if (goalBedtime != null) {
                val hour = goalBedtime / 3600000
                val minute = (goalBedtime % 3600000) / 60000
                Text(
                    text = "Tu meta actual para dormir es a las ${String.format("%02d:%02d", hour, minute)}.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = "Actualmente usas la hora de tu meta semanal (23:00 por defecto).",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "La racha semanal ajustará automáticamente esta hora basándose en tu progreso real (próximamente).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
