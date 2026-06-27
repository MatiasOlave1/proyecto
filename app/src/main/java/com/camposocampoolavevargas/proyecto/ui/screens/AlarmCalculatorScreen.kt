package com.camposocampoolavevargas.proyecto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmCalculatorScreen(
    navController: NavController,
    viewModel: AlarmCalculatorViewModel = hiltViewModel()
) {
    val bedtimeHour by viewModel.bedtimeHour.collectAsState()
    val bedtimeMinute by viewModel.bedtimeMinute.collectAsState()
    val selectedCycles by viewModel.selectedCycles.collectAsState()
    val wakeHour by viewModel.wakeHour.collectAsState()
    val wakeMinute by viewModel.wakeMinute.collectAsState()
    val alarmSet by viewModel.alarmSet.collectAsState()
    val selectedDays by viewModel.selectedDays.collectAsState()

    var showTimePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val bedtimeAmPm = if (bedtimeHour < 12) "AM" else "PM"
    val bedtimeHour12 = when {
        bedtimeHour == 0 -> 12
        bedtimeHour > 12 -> bedtimeHour - 12
        else -> bedtimeHour
    }
    val bedtimeFormatted = String.format("%d:%02d %s", bedtimeHour12, bedtimeMinute, bedtimeAmPm)

    val wakeAmPm = if (wakeHour < 12) "AM" else "PM"
    val wakeHour12 = when {
        wakeHour == 0 -> 12
        wakeHour > 12 -> wakeHour - 12
        else -> wakeHour
    }
    val wakeFormatted = String.format("%d:%02d %s", wakeHour12, wakeMinute, wakeAmPm)



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculadora de Sueño") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
            // --- ESTADO DE LA ALARMA ACTIVA ---
            item {
                if (alarmSet) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "⏰ Alarma Activa",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Sonará a las $wakeFormatted",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            Button(
                                onClick = { 
                                    viewModel.cancelAlarm()
                                    android.widget.Toast.makeText(context, "Alarma cancelada", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Apagar", color = Color.White)
                            }
                        }
                    }
                }
            }

            // --- 1. CARD: PLANIFICAR HORA DE ACOSTARSE ---
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "¿A qué hora vas a acostarte?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                             Text(
                                 text = bedtimeFormatted,
                                 style = MaterialTheme.typography.displaySmall,
                                 color = MaterialTheme.colorScheme.primary,
                                 fontWeight = FontWeight.Bold
                             )
                            Button(
                                onClick = { viewModel.setBedtimeToNow() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Text("Dormir ahora 😴")
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showTimePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cambiar hora de acostarse")
                        }
                    }
                }
            }

            // --- 2. CARD: SELECCIÓN DE CICLOS ---
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "¿Cuántos ciclos quieres dormir?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val cycleOptions = listOf(3, 4, 5, 6, 7)
                            cycleOptions.forEach { cycles ->
                                val isSelected = selectedCycles == cycles
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable { viewModel.updateCycles(cycles) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cycles.toString(),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val durationHours = selectedCycles * 1.5
                        val cyclesDescription = when (selectedCycles) {
                            3 -> "4.5 horas (Ideal para siestas reconstituyentes)"
                            4 -> "6.0 horas (Sueño mínimo recomendado)"
                            5 -> "7.5 horas (¡Recomendado para la mayoría!)"
                            6 -> "9.0 horas (Excelente para recuperación deportiva)"
                            7 -> "10.5 horas (Recuperación profunda tras desvelo)"
                            else -> ""
                        }

                        Text(
                            text = "Duración: $durationHours horas de sueño",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = cyclesDescription,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // --- 3. CARD: HORA DE DESPERTAR CALCULADA ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⏰ Hora de despertar recomendada:",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                         Text(
                             text = wakeFormatted,
                             style = MaterialTheme.typography.displayMedium,
                             fontWeight = FontWeight.Black,
                             color = MaterialTheme.colorScheme.onSecondaryContainer
                         )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "(Incluye 14 minutos para conciliar el sueño al principio del ciclo)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // --- 4. CARD: DÍAS DE RECURRENCIA ---
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Días de repetición",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        val days = listOf(
                            Calendar.MONDAY to "L",
                            Calendar.TUESDAY to "M",
                            Calendar.WEDNESDAY to "X",
                            Calendar.THURSDAY to "J",
                            Calendar.FRIDAY to "V",
                            Calendar.SATURDAY to "S",
                            Calendar.SUNDAY to "D"
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            days.forEach { (day, label) ->
                                val isSelected = selectedDays.contains(day)
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable { viewModel.toggleDay(day) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- 5. ACCIONES Y BOTONES ---
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.setAlarm()
                            android.widget.Toast.makeText(context, "¡Alarma inteligente guardada y activada!", android.widget.Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Guardar y Activar Alarma Inteligente",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    if (alarmSet) {
                        OutlinedButton(
                            onClick = {
                                viewModel.cancelAlarm()
                                android.widget.Toast.makeText(context, "Alarma inteligente desactivada", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Cancelar Alarma",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.simulateAlarm() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "⚡ Simular Despertar (Probar Flujo)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- TIME PICKER DIALOG ---
        if (showTimePicker) {
            val state = rememberTimePickerState(
                initialHour = bedtimeHour,
                initialMinute = bedtimeMinute
            )
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.updateBedtime(
                                state.hour,
                                state.minute
                            )
                            showTimePicker = false
                        }
                    ) {
                        Text("Confirmar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("Cancelar")
                    }
                },
                text = {
                    TimePicker(state = state)
                }
            )
        }
    }
}