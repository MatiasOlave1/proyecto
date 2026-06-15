package com.camposocampoolavevargas.proyecto.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.camposocampoolavevargas.proyecto.ui.BaseViewModel.UiState
import com.camposocampoolavevargas.proyecto.ui.theme.DormiBienUTheme
import java.util.Locale

/**
 * Screen for Weekly Goals (RF04 — Metas semanales).
 * Connects to WeeklyGoalsViewModel and provides interactive forms to configure targets.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyGoalsScreen(
    navController: NavController,
    viewModel: WeeklyGoalsViewModel = hiltViewModel()
) {
    val goal by viewModel.goalState.collectAsState()
    val saveState by viewModel.saveState.collectAsState()

    var minHours by remember { mutableStateOf(8.0f) }
    var requiredDays by remember { mutableStateOf(5) }
    var bedtimeHour by remember { mutableStateOf(23) }
    var bedtimeMinute by remember { mutableStateOf(0) }
    
    var showTimePicker by remember { mutableStateOf(false) }

    // Initialize values when the active goal is loaded from database
    LaunchedEffect(goal) {
        goal?.let {
            minHours = it.minHours
            requiredDays = it.requiredDays
            
            val millis = it.bedtimeLimitMillis
            bedtimeHour = (millis / (1000 * 60 * 60)).toInt()
            bedtimeMinute = ((millis % (1000 * 60 * 60)) / (1000 * 60)).toInt()
        }
    }

    // Clear save message automatically after 3 seconds
    LaunchedEffect(saveState) {
        if (saveState is UiState.Success && (saveState as UiState.Success<String>).data.isNotEmpty()) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearSaveState()
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = bedtimeHour,
            initialMinute = bedtimeMinute,
            is24Hour = true
        )
        
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    bedtimeHour = timePickerState.hour
                    bedtimeMinute = timePickerState.minute
                    showTimePicker = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancelar")
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Hora límite de acostarse",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    TimePicker(state = timePickerState)
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Metas de Sueño", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Week Indicator Header
                    Text(
                        text = viewModel.getCurrentWeekInfo(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Text(
                        text = "Objetivos de Higiene",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Active Goal Summary Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (goal != null) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            }
                        ),
                        border = if (goal != null) {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                        } else null
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (goal != null) "Meta Activa esta Semana" else "Sin Meta Configurada",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (goal != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (goal != null) {
                                    "Tu sistema de gamificación está evaluando tus registros con base en estos objetivos."
                                } else {
                                    "Por favor establece tus objetivos de descanso semanales para activar el motor de rachas y logros."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                            )
                            if (goal != null) {
                                val goalBedtime = String.format(Locale.getDefault(), "%02d:%02d", bedtimeHour, bedtimeMinute)
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text("Dormir Mínimo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${String.format(Locale.getDefault(), "%.1f", minHours)} horas", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                    Column {
                                        Text("Días Requeridos", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("$requiredDays días", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                    Column {
                                        Text("Hora Límite", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(goalBedtime, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Configuration Controls
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // 1. Sleep Hours Slider
                            Column {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = "Mínimo de horas por noche", fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = "${String.format(Locale.getDefault(), "%.1f", minHours)} hrs",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = minHours,
                                    onValueChange = { minHours = it },
                                    valueRange = 4.0f..12.0f,
                                    steps = 15 // 0.5 hour increments
                                )
                            }

                            // 2. Required Days Selector
                            Column {
                                Text(
                                    text = "Días requeridos en la semana",
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    (1..7).forEach { day ->
                                        val isSelected = requiredDays == day
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(42.dp)
                                                .background(
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                                    shape = CircleShape
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline,
                                                    shape = CircleShape
                                                )
                                                .clickable { requiredDays = day }
                                        ) {
                                            Text(
                                                text = day.toString(),
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            // 3. Bedtime TimePicker Button
                            Column {
                                Text(
                                    text = "Hora límite de acostarse",
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                val timeLabel = String.format(Locale.getDefault(), "%02d:%02d", bedtimeHour, bedtimeMinute)
                                Button(
                                    onClick = { showTimePicker = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    ) {
                                        Text("Establecer horario de descanso")
                                        Text(
                                            text = timeLabel,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Show success/error banners
                    if (saveState is UiState.Error) {
                        Text(
                            text = (saveState as UiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    if (saveState is UiState.Success && (saveState as UiState.Success<String>).data.isNotEmpty()) {
                        Text(
                            text = (saveState as UiState.Success<String>).data,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    // Save Button
                    if (saveState is UiState.Loading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    } else {
                        Button(
                            onClick = {
                                viewModel.saveGoal(minHours, requiredDays, bedtimeHour, bedtimeMinute)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(text = "Guardar Meta", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun WeeklyGoalsScreenPreview() {
    DormiBienUTheme {
        WeeklyGoalsScreen(navController = rememberNavController())
    }
}
