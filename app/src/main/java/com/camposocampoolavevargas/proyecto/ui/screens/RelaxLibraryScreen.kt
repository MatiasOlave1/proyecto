package com.camposocampoolavevargas.proyecto.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.camposocampoolavevargas.proyecto.navigation.Screen
import com.camposocampoolavevargas.proyecto.ui.theme.DormiBienUTheme
import kotlin.math.cos
import kotlin.math.sin

/**
 * Main content for the Noche (Wellness/Sleep preparation) tab.
 */
@Composable
fun NocheTabContent(
    navController: NavController,
    viewModel: RelaxLibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Reload settings whenever this screen becomes active to reflect changes from DisconnectReminderScreen
    LaunchedEffect(Unit) {
        viewModel.loadSettings()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. CARD: ALARMA INTELIGENTE (Calculadora de Ciclos) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.navigate(Screen.AlarmCalculator.route) }
                .shadow(4.dp, RoundedCornerShape(16.dp)),
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
                Text(
                    text = "Alarma Inteligente",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Clock visualization container
                Box(
                    modifier = Modifier.size(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Custom drawn clock face
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = size.width / 2 - 12.dp.toPx()

                        // Clock ring outline
                        drawCircle(
                            color = Color(0xFF30363D),
                            radius = radius,
                            center = center,
                            style = Stroke(width = 4.dp.toPx())
                        )

                        // Clock markers (12 ticks)
                        for (i in 0 until 12) {
                            val angle = i * 30 * (Math.PI / 180)
                            val startX = center.x + cos(angle).toFloat() * (radius - 8.dp.toPx())
                            val startY = center.y + sin(angle).toFloat() * (radius - 8.dp.toPx())
                            val endX = center.x + cos(angle).toFloat() * radius
                            val endY = center.y + sin(angle).toFloat() * radius

                            drawStrokeLine(
                                color = if (i % 3 == 0) Color(0xFF58A6FF) else Color(0xFF7D8590),
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = if (i % 3 == 0) 3.dp.toPx() else 1.5.dp.toPx()
                            )
                        }

                        // Draw clock hands showing 7:30
                        // 7:30 means Hour hand at 225 degrees (approx), Minute hand at 180 degrees (6 o'clock)
                        // Hour hand
                        val hrAngle = (225 - 90) * (Math.PI / 180)
                        val hrLength = radius * 0.5f
                        drawStrokeLine(
                            color = Color(0xFFE6EDF3),
                            start = center,
                            end = Offset(
                                center.x + cos(hrAngle).toFloat() * hrLength,
                                center.y + sin(hrAngle).toFloat() * hrLength
                            ),
                            strokeWidth = 4.dp.toPx()
                        )

                        // Minute hand
                        val minAngle = (180 - 90) * (Math.PI / 180)
                        val minLength = radius * 0.75f
                        drawStrokeLine(
                            color = Color(0xFF58A6FF),
                            start = center,
                            end = Offset(
                                center.x + cos(minAngle).toFloat() * minLength,
                                center.y + sin(minAngle).toFloat() * minLength
                            ),
                            strokeWidth = 3.dp.toPx()
                        )

                        // Center pin
                        drawCircle(
                            color = Color(0xFF58A6FF),
                            radius = 6.dp.toPx(),
                            center = center
                        )
                    }

                    // Centered Text overlay
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    ) {
                        Text(
                            text = "Hora de Despertar",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "7:30 AM",
                            fontSize = 20.sp,
                            color = Color(0xFF58A6FF),
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Sugerencias de ciclo",
                            fontSize = 8.sp,
                            color = Color(0xFF3FB950),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // --- 2. CARD: INTEGRACIÓN SPOTIFY ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Integración Spotify",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    // Spotify brand color circle with waves representation
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1DB954)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "♫",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom styled Dropdown trigger
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1C2128))
                        .border(1.dp, Color(0xFF30363D), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Playlist...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Desplegar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Playlist activa: \"Relajación Nocturna\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // --- 3. CARD: VENTANA DE DESCONEXIÓN (RF10) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.navigate(Screen.DisconnectReminder.route) },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ventana de Desconexión",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    // Moon representation icon
                    Text(
                        text = "🌙",
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left offset minutes layout
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = uiState.reminderOffsetMinutes.toString(),
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFF78166),
                            lineHeight = 44.sp
                        )
                        Text(
                            text = "MINUTOS ANTES",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Right checklist layout
                    Column(
                        modifier = Modifier.weight(1.5f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "✓", color = Color(0xFF3FB950), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Activar Modo Noche", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "✓", color = Color(0xFF3FB950), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Reducir Brillo Pantalla", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "✓", color = Color(0xFF3FB950), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Silenciar Notificaciones", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        // --- 4. ROW: DIARIO & REGULACIÓN (2 Equal Width Buttons) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left Card: Diario de Preocupaciones
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { navController.navigate(Screen.Journal.route) }
                    .height(130.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Diario de\nPreocupaciones",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    // Thinking sleep emoji/graphics
                    Text(
                        text = "😴💭",
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }

            // Right Card: Biblioteca de Regulación
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { navController.navigate(Screen.RelaxLibrary.route) }
                    .height(130.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Biblioteca de\nRegulación",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    // Book/Meditation emoji/graphics
                    Text(
                        text = "🧘📚",
                        fontSize = 32.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}

/**
 * Custom stroke line helper for custom clock drawing.
 */
fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStrokeLine(
    color: Color,
    start: Offset,
    end: Offset,
    strokeWidth: Float
) {
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = strokeWidth
    )
}

/**
 * Standalone Screen wrapper for compatibility.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelaxLibraryScreen(
    navController: NavController,
    viewModel: RelaxLibraryViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Noche - Bienestar") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NocheTabContent(navController, viewModel)
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
