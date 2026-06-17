package com.camposocampoolavevargas.proyecto.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.camposocampoolavevargas.proyecto.data.local.model.SleepQuality
import com.camposocampoolavevargas.proyecto.ui.theme.DormiBienUTheme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Screen for logging daily sleep (RF02 — Registro diario de sueño).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepLogScreen(
    navController: NavController,
    viewModel: SleepLogViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val selectedDate by viewModel.selectedDate.collectAsState()
    val sleepTime by viewModel.sleepTime.collectAsState()
    val wakeTime by viewModel.wakeTime.collectAsState()
    val quality by viewModel.quality.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isSaveSuccessful by viewModel.isSaveSuccessful.collectAsState()

    // State guards to prevent opening multiple dialog instances
    var isSleepTimePickerShowing by remember { mutableStateOf(false) }
    var isWakeTimePickerShowing by remember { mutableStateOf(false) }

    // Handle navigation redirection upon successful save
    LaunchedEffect(isSaveSuccessful) {
        if (isSaveSuccessful) {
            Toast.makeText(context, "¡Registro de sueño guardado exitosamente!", Toast.LENGTH_SHORT).show()
            viewModel.resetSaveStatus()
            navController.popBackStack()
        }
    }

    // Format helpers matching locale preferences
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale.forLanguageTag("es-ES")) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    // Calculated duration on the fly for rich UI feedback
    val sleepDateTime = if (sleepTime.isAfter(wakeTime)) {
        LocalDateTime.of(selectedDate.minusDays(1), sleepTime)
    } else {
        LocalDateTime.of(selectedDate, sleepTime)
    }
    val wakeDateTime = LocalDateTime.of(selectedDate, wakeTime)
    val durationMin = java.time.Duration.between(sleepDateTime, wakeDateTime).toMinutes()
    val durHours = durationMin / 60
    val durMins = durationMin % 60



    val onPickSleepTime = {
        if (!isSleepTimePickerShowing) {
            isSleepTimePickerShowing = true
            android.app.TimePickerDialog(
                context,
                { _, hour, minute ->
                    viewModel.updateSleepTime(LocalTime.of(hour, minute))
                },
                sleepTime.hour,
                sleepTime.minute,
                true
            ).apply {
                setOnDismissListener { isSleepTimePickerShowing = false }
                show()
            }
        }
    }

    val onPickWakeTime = {
        if (!isWakeTimePickerShowing) {
            isWakeTimePickerShowing = true
            android.app.TimePickerDialog(
                context,
                { _, hour, minute ->
                    viewModel.updateWakeTime(LocalTime.of(hour, minute))
                },
                wakeTime.hour,
                wakeTime.minute,
                true
            ).apply {
                setOnDismissListener { isWakeTimePickerShowing = false }
                show()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Registrar Descanso",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                    // Header Subtitle (Matching goals style hierarchy)
                    Text(
                        text = "Registro de Sueño Diario",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
                        textAlign = TextAlign.Center
                    )

                    // Unified Form Card (Matching WeeklyGoalsScreen configuration panel styling)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // 1. DATE SELECTOR FIELD
                            Column {
                                Text(
                                    text = "Fecha del descanso",
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Fecha de registro (Ayer)",
                                        modifier = Modifier.weight(1f),
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = selectedDate.format(dateFormatter).replaceFirstChar { it.uppercase() },
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 15.sp
                                    )
                                }
                            }

                            // 2. BEDTIME SELECTOR FIELD
                            Column {
                                Text(
                                    text = "Hora de acostarse",
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Button(
                                    onClick = { onPickSleepTime() },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Me acosté a las",
                                            modifier = Modifier.weight(1f),
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = sleepTime.format(timeFormatter),
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }

                            // 3. WAKE-UP SELECTOR FIELD
                            Column {
                                Text(
                                    text = "Hora de despertarse",
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Button(
                                    onClick = { onPickWakeTime() },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Me desperté a las",
                                            modifier = Modifier.weight(1f),
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = wakeTime.format(timeFormatter),
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }

                            // 4. SLEEP QUALITY SELECTOR
                            Column {
                                Text(
                                    text = "¿Cómo calificas tu descanso?",
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                val qualities = listOf(
                                    Triple(SleepQuality.VERY_BAD, "😫", "Muy Mal"),
                                    Triple(SleepQuality.BAD, "😟", "Mal"),
                                    Triple(SleepQuality.REGULAR, "😐", "Regular"),
                                    Triple(SleepQuality.GOOD, "🙂", "Bien"),
                                    Triple(SleepQuality.EXCELLENT, "🤩", "Excelente")
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    qualities.forEach { (itemQuality, emoji, name) ->
                                        val isSelected = quality == itemQuality
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                    else MaterialTheme.colorScheme.surface
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clickable { viewModel.updateQuality(itemQuality) }
                                                .padding(vertical = 12.dp, horizontal = 2.dp)
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(text = emoji, fontSize = 20.sp)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = name,
                                                    fontSize = 9.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 5. ESTIMATED DURATION BOX
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)), RoundedCornerShape(8.dp))
                                    .padding(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Duración estimada: $durHours hrs $durMins min",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Error message section
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 16.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }

                    // SAVE BUTTON (Styled matching WeeklyGoalsScreen)
                    Button(
                        onClick = { viewModel.saveRecord() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = "Guardar Registro",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun SleepLogScreenPreview() {
    DormiBienUTheme {
        SleepLogScreen(navController = rememberNavController())
    }
}
