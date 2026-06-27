package com.camposocampoolavevargas.proyecto.relajacion.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.camposocampoolavevargas.proyecto.relajacion.domain.model.SubtipoRelajacion
import kotlinx.coroutines.delay

@Composable
fun BreathingAnimationView(
    modifier: Modifier = Modifier,
    subtipo: SubtipoRelajacion,
    isRunning: Boolean = true,
    onCicloCompleted: () -> Unit = {}
) {
    val (inhalar, retener, exhalar) = when (subtipo) {
        SubtipoRelajacion.RESPIRACION_4_7_8 -> Triple(4000, 7000, 8000)
        SubtipoRelajacion.RESPIRACION_BOX -> Triple(4000, 4000, 4000)
        SubtipoRelajacion.RESPIRACION_COHERENTE -> Triple(4000, 0, 6000)
        else -> Triple(4000, 7000, 8000)
    }
    
    var phase by remember { mutableIntStateOf(0) } // 0=inhalar, 1=retener, 2=exhalar
    var progress by remember { mutableIntStateOf(0) } // 0-100
    var secondsRemaining by remember { mutableIntStateOf(0) }
    var ciclosCompletados by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(isRunning) {
        if (!isRunning) return@LaunchedEffect
        
        val phaseDurations = listOf(inhalar, retener, exhalar)
        
        while (true) {
            for (phaseIndex in 0..2) {
                val phaseDuration = phaseDurations[phaseIndex]
                if (phaseDuration == 0) continue
                
                for (ms in 0..phaseDuration step 50) {
                    if (!isRunning) break
                    
                    phase = phaseIndex
                    progress = (ms.toFloat() / phaseDuration * 100).toInt()
                    
                    val remainingMs = phaseDuration - ms
                    secondsRemaining = kotlin.math.max(1, kotlin.math.ceil(remainingMs.toDouble() / 1000.0).toInt())
                    
                    delay(50)
                }
                
                if (phaseIndex == 2) {
                    ciclosCompletados++
                    onCicloCompleted()
                }
            }
            
            progress = 100
            delay(100)
            progress = 0
        }
    }
    
    val phaseName = when (phase) {
        0 -> "Inhala"
        1 -> "Retén"
        else -> "Exhala"
    }
    
    val phaseColor = when (phase) {
        0 -> Color(0xFF2196F3) // Azul (Inhalar)
        1 -> Color(0xFFFFA726) // Naranja (Retener)
        else -> Color(0xFF4CAF50) // Verde (Exhalar)
    }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(240.dp)) {
            val canvasSize = size.minDimension
            val baseRadius = canvasSize / 3.8f
            val maxRadius = canvasSize / 2 - 16.dp.toPx()
            
            val radius = when (phase) {
                0 -> { // Inhalar: expansión
                    baseRadius + (maxRadius - baseRadius) * (progress / 100f)
                }
                1 -> { // Retener: mantener grande
                    maxRadius
                }
                else -> { // Exhalar: contracción
                    maxRadius - (maxRadius - baseRadius) * (progress / 100f)
                }
            }
            
            // Draw background track ring
            drawCircle(
                color = Color.Gray.copy(alpha = 0.15f),
                radius = maxRadius + 8.dp.toPx(),
                center = center,
                style = Stroke(width = 4.dp.toPx())
            )
            
            // Draw active progress arc
            drawArc(
                color = phaseColor,
                startAngle = -90f,
                sweepAngle = progress * 3.6f,
                useCenter = false,
                topLeft = Offset(center.x - (maxRadius + 8.dp.toPx()), center.y - (maxRadius + 8.dp.toPx())),
                size = androidx.compose.ui.geometry.Size((maxRadius + 8.dp.toPx()) * 2, (maxRadius + 8.dp.toPx()) * 2),
                style = Stroke(width = 4.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
            
            // Draw inner breathing bubble
            drawCircle(
                color = phaseColor,
                radius = radius,
                center = center,
                alpha = 0.22f
            )
            
            // Draw solid inner core for depth
            drawCircle(
                color = phaseColor,
                radius = radius * 0.82f,
                center = center,
                alpha = 0.4f
            )
        }
        
        // Centered instructions and countdown
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = phaseName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${secondsRemaining}s",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = phaseColor
            )
        }
    }
}
