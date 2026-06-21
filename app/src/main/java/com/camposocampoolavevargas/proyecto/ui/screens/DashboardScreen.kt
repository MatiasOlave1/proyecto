package com.camposocampoolavevargas.proyecto.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.camposocampoolavevargas.proyecto.navigation.Screen
import com.camposocampoolavevargas.proyecto.ui.theme.DormiBienUTheme

/**
 * Main scrollable content for the Dashboard tab.
 */
@Composable
fun DashboardTabContent(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val currentGoal by viewModel.currentGoal.collectAsState()
    val streakData by viewModel.streakData.collectAsState()
    val streakDays = streakData?.currentStreak ?: 0
    val streakProgress = (streakDays / 30f).coerceIn(0f, 1f)
    val averageHours by viewModel.averageHours.collectAsState()
    val mostCommonQuality by viewModel.mostCommonQuality.collectAsState()
    val monthlyGoalsCompleted by viewModel.monthlyGoalsCompleted.collectAsState()


    // Reload active goal and streak data every time this screen becomes active/visible
    val circadianAlerts by viewModel.circadianAlerts.collectAsState()
    val recentSleepRecords by viewModel.recentSleepRecords.collectAsState()
    
    var showCircadianDialog by remember { mutableStateOf(false) }
 
    // Reload active goal, streak data, circadian alerts and sleep records every time this screen becomes active/visible
    LaunchedEffect(Unit) {
        viewModel.loadCurrentGoal()
        viewModel.loadStreakData()
        viewModel.loadCircadianAlerts()
        viewModel.loadRecentSleepRecords()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. CARD: STREAK COUNTER ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.navigate(Screen.Streaks.route) }
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header of card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Contador de Rachas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3FB950)) // Green indicator
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "En línea/Sincronizado",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF3FB950)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Circular fire racha representation
                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer glow/background ring
                    CircularProgressIndicator(
                        progress = { streakProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFFF78166).copy(alpha = 0.2f),
                        strokeWidth = 12.dp
                    )

                    // Active fire gradient progress circle
                    CircularProgressIndicator(
                        progress = { streakProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFFF78166), // Coral/Orange
                        strokeWidth = 12.dp
                    )

                    // Inner contents (flame and number)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🔥",
                            fontSize = 32.sp
                        )
                        Text(
                            text = "$streakDays",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFF78166),
                            lineHeight = 48.sp
                        )
                        Text(
                            text = "días",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Racha de Sueño Diaria",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // --- 2. CARD: LAST NIGHT'S SLEEP ENTRY ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = "Registro de Sueño de Anoche",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Registra las horas reales y la calidad de tu descanso de ayer para calcular tus métricas diarias.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate(Screen.SleepLog.route) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF78166), // Orange
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Registrar Sueño de Anoche",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Flecha"
                        )
                    }
                }
            }
        }

        // --- 3. CARD: WEEKLY GOALS CONFIGURATION ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (currentGoal != null) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            border = if (currentGoal != null) {
                androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF78166).copy(alpha = 0.4f))
            } else null
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = if (currentGoal != null) "Meta de Sueño Activa" else "Metas Semanales de Sueño",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (currentGoal != null) Color(0xFFF78166) else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                if (currentGoal != null) {
                    val goalVal = currentGoal!!
                    val bedtimeHour = (goalVal.bedtimeLimitMillis / (1000 * 60 * 60)).toInt()
                    val bedtimeMinute = ((goalVal.bedtimeLimitMillis % (1000 * 60 * 60)) / (1000 * 60)).toInt()
                    val bedtimeLabel = String.format(Locale.getDefault(), "%02d:%02d", bedtimeHour, bedtimeMinute)
                    
                    Text(
                        text = "Objetivo semanal configurado para mejorar tu ritmo circadiano:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("Dormir Mínimo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${String.format(Locale.getDefault(), "%.1f", goalVal.minHours)} horas", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column {
                            Text("Días Requeridos", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${goalVal.requiredDays} días", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column {
                            Text("Hora Límite", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(bedtimeLabel, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                } else {
                    Text(
                        text = "Configura tus objetivos de sueño (horas mínimas, días consecutivos y hora límite) para activar la gamificación.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { navController.navigate(Screen.WeeklyGoals.route) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF78166), // Use custom coral/orange theme color
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (currentGoal != null) "Editar Meta de la Semana" else "Configurar Metas de la Semana",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Flecha"
                        )
                    }
                }
            }
        }

        // --- 4. CARD: DASHBOARD MONTHLY ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "Dashboard Mensual",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Promedio mensual de sueño",
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = String.format("%.2f horas", averageHours),
                    fontSize = 22.sp,
                    color = Color(0xFFF78166)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Calidad predominante",
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = mostCommonQuality
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Metas cumplidas",
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "$monthlyGoalsCompleted noches con 7 o más horas"
                )
            }
        }

        // --- 5. CARD: JET LAG SOCIAL ALERT ---
        // --- 4. CARD: ESTADO RITMO CIRCADIANO ---
        val weekdaysCount = recentSleepRecords.count {
            val day = try { LocalDate.parse(it.date).dayOfWeek } catch (e: Exception) { null }
            day != null && day != java.time.DayOfWeek.SATURDAY && day != java.time.DayOfWeek.SUNDAY
        }
        val weekendsCount = recentSleepRecords.count {
            val day = try { LocalDate.parse(it.date).dayOfWeek } catch (e: Exception) { null }
            day != null && (day == java.time.DayOfWeek.SATURDAY || day == java.time.DayOfWeek.SUNDAY)
        }
        val hasEnoughRecords = weekdaysCount >= 1 && weekendsCount >= 1
        
        val socialJetLagAlert = circadianAlerts.firstOrNull()
        val isAligned = socialJetLagAlert == null

        val cardColor = when {
            !hasEnoughRecords -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
            isAligned -> Color(0xFF4CAF50).copy(alpha = 0.1f)
            else -> Color(0xFFF78166).copy(alpha = 0.1f)
        }
        val borderColor = when {
            !hasEnoughRecords -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
            isAligned -> Color(0xFF4CAF50).copy(alpha = 0.5f)
            else -> Color(0xFFF78166).copy(alpha = 0.5f)
        }
        val tintColor = when {
            !hasEnoughRecords -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            isAligned -> Color(0xFF4CAF50)
            else -> Color(0xFFF78166)
        }
        val titleText = when {
            !hasEnoughRecords -> "Ciclo Circadiano: Registros Insuficientes"
            isAligned -> "Ciclo Circadiano Sincronizado"
            else -> "Ciclo Circadiano Desalineado"
        }
        val descText = when {
            !hasEnoughRecords -> "Faltan días de registro para calcular el ritmo. Pulsa para ver."
            isAligned -> "¡Buen trabajo! Mantienes un patrón regular. Pulsa para ver detalles."
            else -> "Alerta: Desfase de ${String.format(Locale.getDefault(), "%.1f", socialJetLagAlert?.deltaHours)}h entre semana y finde. Pulsa para ver."
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showCircadianDialog = true }
                .border(
                    width = 1.dp, 
                    color = borderColor, 
                    shape = RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardColor
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Estado Circadiano",
                    tint = tintColor,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = tintColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = descText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // --- 5. DIALOG: DETALLES Y MEDIDAS CIRCADIANAS ---
        if (showCircadianDialog) {
            AlertDialog(
                onDismissRequest = { showCircadianDialog = false },
                title = {
                    Text(
                        text = when {
                            !hasEnoughRecords -> "Registros Insuficientes 🔘"
                            isAligned -> "Ritmo Circadiano Sincronizado ✅"
                            else -> "Ciclo Circadiano Desalineado ⚠️"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Calendario interno de horas dormidas
                        Text(
                            text = "Horas dormidas en los últimos 7 días:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        val last7Days = (6 downTo 0).map { LocalDate.now().minusDays(it.toLong()) }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            last7Days.forEach { day ->
                                val record = recentSleepRecords.find { it.date == day.toString() }
                                val hours = record?.durationMinutes?.let { it / 60f } ?: 0f
                                val dayName = day.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale("es", "ES"))
                                
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = if (hours > 0) "${String.format(Locale.getDefault(), "%.1f", hours)}h" else "-",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    val barHeight = (hours * 7).coerceIn(8f, 70f).dp
                                    Box(
                                        modifier = Modifier
                                            .width(16.dp)
                                            .height(barHeight)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (hours == 0f) {
                                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                                                } else if (hours >= 7f && hours <= 9f) {
                                                    Color(0xFF4CAF50) // Green for healthy duration
                                                } else {
                                                    Color(0xFFF78166) // Orange/coral for deviation
                                                }
                                            )
                                    )
                                    
                                    Text(
                                        text = dayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                        )

                        Text(
                            text = when {
                                !hasEnoughRecords -> {
                                    "¿Por qué está así?\nNecesitamos al menos 1 registro de día de semana (Lunes a Viernes) y 1 de fin de semana (Sábado o Domingo) en los últimos 7 días para evaluar y calcular tu ritmo biológico."
                                }
                                isAligned -> {
                                    "¿Por qué está así?\nTu diferencia promedio de despertar entre días de semana y fines de semana es menor a 2 horas. Esto indica estabilidad biológica."
                                }
                                else -> {
                                    "¿Por qué está así?\nLa diferencia de tu hora promedio de despertar entre días hábiles y el fin de semana supera las 2 horas (tienes ${String.format(Locale.getDefault(), "%.1f", socialJetLagAlert?.deltaHours)}h de desfase). Esto se conoce como Jet Lag Social."
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Text(
                            text = "Medidas y Recomendaciones:\n" +
                            when {
                                !hasEnoughRecords -> {
                                    "• Registra tu descanso todos los días para obtener un análisis preciso de tu ciclo.\n• Intenta mantener la constancia en tus registros diarios de sueño.\n• Al registrar suficientes días, verás tu estado aquí automáticamente."
                                }
                                isAligned -> {
                                    "• Sigue despertándote en el mismo rango de 1 hora todos los días.\n• Evita prolongar demasiado el descanso los fines de semana.\n• Toma luz solar por las mañanas para fijar tu reloj biológico."
                                }
                                else -> {
                                    "• Intenta despertar y acostarte a horas similares todos los días (varía máximo 1 hora).\n• Evita recuperar sueño retrasando tu despertar; prefiere siestas cortas en la tarde (20-30 min).\n• Toma luz solar en los primeros 30 minutos al despertar para fijar tu reloj interno."
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCircadianDialog = false }) {
                        Text("Entendido")
                    }
                }
            )
        }
    }
}

/**
 * Standalone Screen wrapper for compatibility.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            DashboardTabContent(navController)
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun DashboardScreenPreview() {
    DormiBienUTheme {
        DashboardScreen(navController = rememberNavController())
    }
}
