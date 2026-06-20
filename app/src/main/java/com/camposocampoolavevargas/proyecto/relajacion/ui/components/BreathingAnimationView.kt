package com.camposocampoolavevargas.proyecto.relajacion.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
    var ciclosCompletados by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(isRunning) {
        if (!isRunning) return@LaunchedEffect
        
        var elapsedMs = 0
        val phaseDurations = listOf(inhalar, retener, exhalar)
        
        while (true) {
            for (phaseIndex in 0..2) {
                val phaseDuration = phaseDurations[phaseIndex]
                if (phaseDuration == 0) continue
                
                for (ms in 0..phaseDuration step 50) {
                    if (!isRunning) break
                    
                    phase = phaseIndex
                    progress = (ms.toFloat() / phaseDuration * 100).toInt()
                    
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
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val canvasSize = size.minDimension
            val radius = when (phase) {
                0 -> { // Inhalar: expansión
                    val baseRadius = canvasSize / 4
                    baseRadius + (baseRadius * 0.5f * progress / 100f)
                }
                1 -> { // Retener: estático con pulso suave
                    val baseRadius = canvasSize / 4 * 1.5f
                    baseRadius + (kotlin.math.sin(progress * 0.1f) * baseRadius * 0.1f).toFloat()
                }
                else -> { // Exhalar: contracción
                    val maxRadius = canvasSize / 4 * 1.5f
                    maxRadius - (maxRadius * 0.5f * progress / 100f)
                }
            }
            
            val color = when (phase) {
                0 -> Color(0xFF4A90D9) // Azul para inhalar
                1 -> Color(0xFF4A90D9) // Azul para retener
                else -> Color(0xFF7EC8A4) // Verde para exhalar
            }
            
            drawCircle(
                color = color,
                radius = radius,
                center = center,
                alpha = 0.7f
            )
        }
    }
}

@Composable
fun BreathingPhaseIndicator(
    phase: Int = 0,
    progress: Int = 0,
    modifier: Modifier = Modifier
) {
    val phaseNames = listOf("Inhalar", "Retener", "Exhalar")
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val phaseName = if (phase in phaseNames.indices) phaseNames[phase] else "---"
        androidx.compose.material3.Text(
            text = "$phaseName\n$progress%",
            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
        )
    }
}
