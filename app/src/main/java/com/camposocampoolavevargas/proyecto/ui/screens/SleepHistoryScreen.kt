package com.camposocampoolavevargas.proyecto.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.camposocampoolavevargas.proyecto.ui.theme.DormiBienUTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.camposocampoolavevargas.proyecto.data.local.model.SleepQuality

/**
 * Main content Composable for the Historial tab.
 */
@Composable
fun HistorialTabContent(
    navController: NavController,
    viewModel: SleepHistoryViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val registros by viewModel.records.collectAsState()

    val ultimosRegistros = registros
        .sortedBy { it.date }
        .takeLast(7)

    val barData = ultimosRegistros.map {
        it.durationMinutes / 60f
    }

    val labels = ultimosRegistros.map {
        it.date.takeLast(2)
    }

    val promedioHoras =
        if (barData.isNotEmpty())
            barData.average()
        else
            0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. CARD: HISTORIAL MENSUAL (GRAFICO DE BARRAS) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                // Header of the card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Historial Mensual",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "X̄ = %.1f hrs".format(promedioHoras),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF58A6FF)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Custom Bar Chart using Canvas

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasHeight = size.height
                        val canvasWidth = size.width
                        val paddingRight = 10.dp.toPx()
                        val spacing = 12.dp.toPx()
                        val barWidth = (canvasWidth - paddingRight - (spacing * (barData.size - 1))) / barData.size
                        val maxVal = 10f // Max sleep hours scale

                        barData.forEachIndexed { index, value ->
                            val barHeight = (value / maxVal) * (canvasHeight - 20.dp.toPx())
                            val left = index * (barWidth + spacing)
                            val top = canvasHeight - 20.dp.toPx() - barHeight

                            // Pick color based on sleep quality (value >= 7h is good/green, otherwise warning/yellow)
                            val calidad = ultimosRegistros[index].quality

                            val barColor = when (calidad) {
                                SleepQuality.EXCELLENT -> Color(0xFF3FB950)
                                SleepQuality.GOOD -> Color(0xFF3FB950)
                                SleepQuality.REGULAR -> Color(0xFFE3B341)
                                SleepQuality.BAD -> Color.Red
                                SleepQuality.VERY_BAD -> Color.Red
                            }
                            // Draw rounded bar
                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(left, top),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                        }

                        // Draw base guideline
                        drawLine(
                            color = Color(0xFF30363D),
                            start = Offset(0f, canvasHeight - 20.dp.toPx()),
                            end = Offset(canvasWidth, canvasHeight - 20.dp.toPx()),
                            strokeWidth = 2f
                        )
                    }

                    // Render bottom label tags positioned under the bars
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomStart)
                            .padding(bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        labels.forEach { label ->
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(26.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // --- 2. CARD: FILTRO SELECTOR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, Color(0xFF30363D), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filtra: Oct 24 - Oct 26",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Desplegar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // --- 3. SLEEP LOG LIST ---
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            registros.forEach { registro ->

                val horasDormidas = registro.durationMinutes / 60.0

                val colorCalidad = when (registro.quality) {
                    SleepQuality.EXCELLENT -> Color(0xFF3FB950)
                    SleepQuality.GOOD -> Color(0xFF3FB950)
                    SleepQuality.REGULAR -> Color(0xFFE3B341)
                    SleepQuality.BAD -> Color.Red
                    SleepQuality.VERY_BAD -> Color.Red
                }

                val fechaPartes = registro.date.split("-")

                val dia = fechaPartes[2]

                val mes = when (fechaPartes[1]) {
                    "01" -> "ENE"
                    "02" -> "FEB"
                    "03" -> "MAR"
                    "04" -> "ABR"
                    "05" -> "MAY"
                    "06" -> "JUN"
                    "07" -> "JUL"
                    "08" -> "AGO"
                    "09" -> "SEP"
                    "10" -> "OCT"
                    "11" -> "NOV"
                    "12" -> "DIC"
                    else -> "--"
                }

                HistoryItemRow(
                    month = mes,
                    day = dia,
                    hours = String.format("%.1fh", horasDormidas),
                    quality = registro.quality.displayName,
                    qualityColor = colorCalidad,
                    hasStreak = horasDormidas >= 7
                )
            }
        }
    }
}

/**
 * Helper row representation for a single Sleep Log Item.
 */
@Composable
fun HistoryItemRow(
    month: String,
    day: String,
    hours: String,
    quality: String,
    qualityColor: Color,
    hasStreak: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Date Layout
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(vertical = 6.dp)
                ) {
                    Text(
                        text = month.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = day,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Middle Text Info
                Column {
                    Text(
                        text = hours,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = quality,
                        fontSize = 12.sp,
                        color = qualityColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Right Icons (Checkmark and flame)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (hasStreak) {
                    Text(
                        text = "🔥",
                        fontSize = 16.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(qualityColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        color = qualityColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
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
fun SleepHistoryScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Sueño") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HistorialTabContent(navController)
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun SleepHistoryScreenPreview() {
    DormiBienUTheme {
        SleepHistoryScreen(navController = rememberNavController())
    }
}
