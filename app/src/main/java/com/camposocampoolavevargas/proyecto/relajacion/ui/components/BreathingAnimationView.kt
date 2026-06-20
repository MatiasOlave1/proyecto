package com.dormibienu.app.relajacion.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dormibienu.app.relajacion.config.RelajacionConfig
import com.dormibienu.app.relajacion.ui.viewmodel.FaseRespiracion
import kotlin.math.ceil

/**
 * Componente interactivo 100% Jetpack Compose encargado de renderizar la animación rítmica.
 * Corre enteramente en el hilo de UI sin llamadas a red, optimizando el rendimiento.
 */
@Composable
fun BreathingAnimationView(
    faseActual: FaseRespiracion,
    tiempoRestanteMs: Long,
    modifier: Modifier = Modifier
) {
    // Escala del círculo de respiración controlado por un contenedor Animatable nativo
    val escalaCirculo = remember { Animatable(1.0f) }
    
    // Escala independiente para el efecto de pulso suave durante la fase de RETENER
    val escalaPulsoRetener = remember { Animatable(1.0f) }

    // Criterio de Aceptación: Cambios de color reactivos estipulados por especificación
    val colorFaseActual by animateColorAsState(
        targetValue = when (faseActual) {
            FaseRespiracion.INHALAR -> Color(RelajacionConfig.Respiracion478.COLOR_INHALAR_HEX)
            FaseRespiracion.EXHALAR -> Color(RelajacionConfig.Respiracion478.COLOR_EXHALAR_HEX)
            FaseRespiracion.RETENER -> Color(RelajacionConfig.Respiracion478.COLOR_RETENER_HEX)
            FaseRespiracion.INACTIVO -> Color.LightGray
        },
        animationSpec = tween(durationMillis = 500),
        label = "ColorBreathingPhase"
    )

    // Coordinación estricta de las animaciones físicas según la fase actual de la especificación
    LaunchedEffect(faseActual) {
        when (faseActual) {
            FaseRespiracion.INHALAR -> {
                escalaPulsoRetener.stop()
                escalaPulsoRetener.snapTo(1.0f)
                // Expansión del círculo en 4 segundos exactos
                escalaCirculo.animateTo(
                    targetValue = 2.2f,
                    animationSpec = tween(
                        durationMillis = RelajacionConfig.Respiracion478.TIEMPO_INHALAR_MS.toInt(),
                        easing = LinearEasing
                    )
                )
            }
            FaseRespiracion.RETENER -> {
                // Círculo estático base con un pulso suave infinito e intermitente
                escalaPulsoRetener.animateTo(
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 875, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
            }
            FaseRespiracion.EXHALAR -> {
                escalaPulsoRetener.stop()
                escalaPulsoRetener.snapTo(1.0f)
                // Contracción del círculo en 8 segundos exactos hacia su tamaño base
                escalaCirculo.animateTo(
                    targetValue = 1.0f,
                    animationSpec = tween(
                        durationMillis = RelajacionConfig.Respiracion478.TIEMPO_EXHALAR_MS.toInt(),
                        easing = LinearEasing
                    )
                )
            }
            FaseRespiracion.INACTIVO -> {
                escalaPulsoRetener.stop()
                escalaCirculo.snapTo(1.0f)
                escalaPulsoRetener.snapTo(1.0f)
            }
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Renderizado del elemento gráfico geométrico sin sobredibujo (Canvas limpio)
        Canvas(
            modifier = Modifier
                .size(140.dp)
                .graphicsLayer(
                    scaleX = escalaCirculo.value * escalaPulsoRetener.value,
                    scaleY = escalaCirculo.value * escalaPulsoRetener.value
                )
        ) {
            drawCircle(color = colorFaseActual)
        }

        // Indicador textual dinámico en el núcleo del Canvas de la guía visual
        if (faseActual != FaseRespiracion.INACTIVO) {
            val segundosRestantesText = ceil(tiempoRestanteMs / 1000.0).toInt().toString()
            Text(
                text = segundosRestantesText,
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            Text(
                text = "Zzz",
                color = Color.Gray,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}