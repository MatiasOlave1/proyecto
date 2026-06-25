package com.camposocampoolavevargas.proyecto.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

// --- Paleta de calidad consistente con el resto de la app ---
private fun colorParaCalidad(calidad: SleepQuality): Color = when (calidad) {
    SleepQuality.EXCELLENT -> Color(0xFF3FB950)   // Verde
    SleepQuality.GOOD      -> Color(0xFF58C17A)   // Verde claro
    SleepQuality.REGULAR   -> Color(0xFFE3B341)   // Amarillo
    SleepQuality.BAD       -> Color(0xFFF78166)   // Naranja/Rojo
    SleepQuality.VERY_BAD  -> Color(0xFFDA3633)   // Rojo
}

private fun emojiParaCalidad(calidad: SleepQuality): String = when (calidad) {
    SleepQuality.EXCELLENT -> "🌟"
    SleepQuality.GOOD      -> "😴"
    SleepQuality.REGULAR   -> "😐"
    SleepQuality.BAD       -> "😟"
    SleepQuality.VERY_BAD  -> "😩"
}

private fun mesAbreviado(numeroMes: String): String = when (numeroMes) {
    "01" -> "ENE"; "02" -> "FEB"; "03" -> "MAR"; "04" -> "ABR"
    "05" -> "MAY"; "06" -> "JUN"; "07" -> "JUL"; "08" -> "AGO"
    "09" -> "SEP"; "10" -> "OCT"; "11" -> "NOV"; "12" -> "DIC"
    else -> "---"
}

// ─────────────────────────────────────────────────────────────
// PANTALLA PRINCIPAL — Historial de Sueño (RF03)
// ─────────────────────────────────────────────────────────────

/**
 * Contenido principal del tab Historial — RF03.
 * Incluye gráfico de barras de los últimos 7 días y lista cronológica
 * ordenada DESC, con estética alineada al DormiBienUTheme.
 */
@Composable
fun HistorialTabContent(
    navController: NavController,
    viewModel: SleepHistoryViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val registros by viewModel.filteredRecords.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    var filterExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.reload() }

    val ultimos7 = registros.sortedBy { it.date }.takeLast(7)
    val barData   = ultimos7.map { it.durationMinutes / 60f }
    val labels    = ultimos7.map { it.date.takeLast(2) }
    val calidades = ultimos7.map { it.quality }
    val promedio  = if (barData.isNotEmpty()) barData.average() else 0.0

    val dateRangeText = if (registros.isNotEmpty()) {
        val latestRecord = registros.first()
        val earliestRecord = registros.last()
        val formattedStart = formatShortDate(earliestRecord.date)
        val formattedEnd = formatShortDate(latestRecord.date)
        if (formattedStart == formattedEnd) formattedStart else "$formattedStart - $formattedEnd"
    } else {
        "Sin registros"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ── ENCABEZADO ────────────────────────────────────────
        Text(
            text = "Historial de Sueño",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Tus registros ordenados cronológicamente",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // ── TARJETA: GRÁFICO DE BARRAS (últimos 7 días) ──────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Últimos 7 días",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Horas dormidas por noche",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Badge promedio
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF78166).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFFF78166).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "X̄ = %.1fh".format(promedio),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF78166)
                            )
                            Text(
                                text = "promedio",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Gráfico de barras con Canvas
                if (barData.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val h          = size.height
                            val w          = size.width
                            val bottomPad  = 24.dp.toPx()
                            val spacing    = 10.dp.toPx()
                            val barW       = (w - spacing * (barData.size - 1)) / barData.size
                            val maxVal     = 10f
                            val chartH     = h - bottomPad

                            // Línea de referencia 8h (meta recomendada)
                            val ref8h = chartH - (8f / maxVal) * chartH
                            drawLine(
                                color = Color(0xFFF78166).copy(alpha = 0.3f),
                                start = Offset(0f, ref8h),
                                end = Offset(w, ref8h),
                                strokeWidth = 1.5f,
                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                    floatArrayOf(8f, 6f)
                                )
                            )

                            barData.forEachIndexed { i, value ->
                                val barH    = (value / maxVal) * chartH
                                val left    = i * (barW + spacing)
                                val top     = chartH - barH
                                val calidad = calidades[i]
                                val barColor = colorParaCalidad(calidad)

                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(barColor, barColor.copy(alpha = 0.6f)),
                                        startY = top,
                                        endY = chartH
                                    ),
                                    topLeft = Offset(left, top),
                                    size = Size(barW, barH),
                                    cornerRadius = CornerRadius(6.dp.toPx())
                                )
                            }

                            // Línea base
                            drawLine(
                                color = Color(0xFF30363D),
                                start = Offset(0f, chartH),
                                end = Offset(w, chartH),
                                strokeWidth = 1.5f
                            )
                        }

                        // Etiquetas de días
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomStart),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            labels.forEach { label ->
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Leyenda de referencia
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF78166).copy(alpha = 0.4f))
                                .align(Alignment.CenterVertically)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Meta: 8h",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    EmptyStateChart()
                }
            }
        }

        // ── ENCABEZADO SECCIÓN LISTA ──────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Registros Recientes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${registros.size} entradas",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ── LISTA CRONOLÓGICA ─────────────────────────────────
        if (registros.isEmpty()) {
            EmptyStateList()
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                registros.forEach { registro ->
                    val partes     = registro.date.split("-")
                    val dia        = partes.getOrNull(2) ?: "--"
                    val mes        = mesAbreviado(partes.getOrNull(1) ?: "")
                    val anio       = partes.getOrNull(0) ?: ""
                    val horas      = registro.durationMinutes / 60.0
                    val color      = colorParaCalidad(registro.quality)
                    val emoji      = emojiParaCalidad(registro.quality)
                    val cumpleMeta = horas >= 7

                    SleepHistoryItemCard(
                        dia = dia,
                        mes = mes,
                        anio = anio,
                        horas = "%.1fh".format(horas),
                        calidad = registro.quality.displayName,
                        colorCalidad = color,
                        emoji = emoji,
                        cumpleMeta = cumpleMeta
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ─────────────────────────────────────────────────────────────
// COMPONENTE: Tarjeta de item de historial expandible
// ─────────────────────────────────────────────────────────────

/**
 * Tarjeta individual de registro de sueño con expansión animada.
 * Muestra fecha, horas dormidas, calidad y detalles al expandir.
 */
@Composable
fun SleepHistoryItemCard(
    dia: String,
    mes: String,
    anio: String,
    horas: String,
    calidad: String,
    colorCalidad: Color,
    emoji: String,
    cumpleMeta: Boolean
) {
    var expandida by remember { mutableStateOf(false) }
    val borderAlpha by animateFloatAsState(
        targetValue = if (expandida) 0.6f else 0.0f,
        animationSpec = tween(250),
        label = "borderAlpha"
    )
    val bgColor by animateColorAsState(
        targetValue = if (expandida)
            colorCalidad.copy(alpha = 0.05f)
        else
            MaterialTheme.colorScheme.surface,
        animationSpec = tween(250),
        label = "bgColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(if (expandida) 6.dp else 2.dp, RoundedCornerShape(16.dp))
            .then(
                if (expandida) Modifier.border(1.dp, colorCalidad.copy(alpha = borderAlpha), RoundedCornerShape(16.dp))
                else Modifier
            )
            .clickable { expandida = !expandida },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Fila principal ──────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    // Bloque de fecha
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = mes,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = dia,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = horas,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = emoji, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = calidad,
                            fontSize = 13.sp,
                            color = colorCalidad,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Iconos derecha
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (cumpleMeta) {
                        Text(text = "🔥", fontSize = 16.sp)
                    }
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(colorCalidad.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (cumpleMeta) "✓" else "–",
                            color = colorCalidad,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Icon(
                        imageVector = if (expandida) Icons.Default.KeyboardArrowUp
                                      else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expandida) "Contraer" else "Expandir",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // ── Sección expandida ───────────────────────────
            AnimatedVisibility(
                visible = expandida,
                enter = expandVertically(animationSpec = tween(250)),
                exit  = shrinkVertically(animationSpec = tween(200))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DetailChip(
                            label = "Calidad",
                            value = calidad,
                            color = colorCalidad,
                            modifier = Modifier.weight(1f)
                        )
                        DetailChip(
                            label = "Duración",
                            value = horas,
                            color = if (cumpleMeta) Color(0xFF3FB950) else Color(0xFFE3B341),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (cumpleMeta) "✅ Cumpliste la meta de 7h esta noche."
                               else "⚠️ No alcanzaste las 7h recomendadas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (cumpleMeta) Color(0xFF3FB950) else Color(0xFFE3B341),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Chip de detalle reutilizable para la sección expandida.
 */
@Composable
private fun DetailChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * Estado vacío cuando no hay datos para el gráfico.
 */
@Composable
private fun EmptyStateChart() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "📊", fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sin datos aún",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Estado vacío cuando no hay registros de sueño.
 */
@Composable
private fun EmptyStateList() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🌙", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Sin registros de sueño",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Comienza registrando el sueño de anoche para ver tu historial aquí.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// WRAPPER — Pantalla standalone con TopAppBar
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepHistoryScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Historial de Sueño",
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
        ) {
            HistorialTabContent(navController)
        }
    }
}

// ─────────────────────────────────────────────────────────────
// PREVIEW
// ─────────────────────────────────────────────────────────────

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF1E293B)
@Composable
fun SleepHistoryScreenPreview() {
    DormiBienUTheme {
        SleepHistoryScreen(navController = rememberNavController())
    }
}
