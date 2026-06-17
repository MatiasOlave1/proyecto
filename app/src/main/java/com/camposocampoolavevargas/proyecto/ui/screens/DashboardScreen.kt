package com.camposocampoolavevargas.proyecto.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
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

    // Reload active goal every time this screen becomes active/visible
    LaunchedEffect(Unit) {
        viewModel.loadCurrentGoal()
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
                        progress = { 0.8f },
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFFF78166).copy(alpha = 0.2f),
                        strokeWidth = 12.dp
                    )

                    // Active fire gradient progress circle
                    CircularProgressIndicator(
                        progress = { 0.8f },
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
                            text = "12",
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

        // --- 4. CARD: JET LAG SOCIAL ALERT ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFF78166).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF78166).copy(alpha = 0.1f) // 10% opacity coral
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Alerta",
                    tint = Color(0xFFF78166),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Jet Lag Social Detectado",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF78166)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Alerta de Jet Lag Social: Desfase de 2.5h este finde vs semana. ¡Ajusta tu rutina!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
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
