package com.dormibienu.app.relajacion.ui.screens

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.dormibienu.app.relajacion.config.RelajacionConfig
import com.dormibienu.app.relajacion.service.AudioPlayerService
import com.dormibienu.app.relajacion.ui.components.AudioPlayerControls
import com.dormibienu.app.relajacion.ui.components.BreathingAnimationView
import com.dormibienu.app.relajacion.ui.viewmodel.FaseRespiracion
import com.dormibienu.app.relajacion.ui.viewmodel.RelajacionViewModel

/**
 * Pantalla principal del Módulo de Relajación Analógica (SPEC-06).
 * Resuelve de manera nativa la integración offline, el control de brillo de pantalla,
 * el ciclo de vida (onPause/onResume) y el puente con el Foreground Service.
 */
@Composable
fun RelajacionScreen(
    viewModel: RelajacionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()

    // --- INTEGRACIÓN Y ENLACE CON EL FOREGROUND SERVICE ---
    var audioService by remember { mutableStateOf<AudioPlayerService?>(null) }
    var bound by remember { mutableStateOf(false) }

    val connection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(className: ComponentName, service: IBinder) {
                val binder = service as AudioPlayerService.LocalBinder
                val targetService = binder.getService()
                audioService = targetService
                bound = true
                // Sincronizar el estado actual del servicio con el ViewModel en la vinculación
                viewModel.actualizarEstadoAudio(
                    estaReproduciendo = targetService.estaReproduciendo(),
                    subtipo = uiState.audioSeleccionado
                )
            }

            override fun onServiceDisconnected(arg0: ComponentName) {
                audioService = null
                bound = false
                viewModel.actualizarEstadoAudio(estaReproduciendo = false, subtipo = null)
            }
        }
    }

    // Efecto secundario para levantar y enlazar el servicio localmente
    DisposableEffect(context) {
        val intent = Intent(context, AudioPlayerService::class.java)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        onDispose {
            if (bound) {
                context.unbindService(connection)
                bound = false
            }
        }
    }

    // --- CAP-06-A: CICLO DE VIDA DE LA ANIMACIÓN (BACKGROUND / FOREGROUND) ---
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    // Pausa la animación y persiste el timestamp de interrupción
                    viewModel.pausarPorBackground()
                }
                Lifecycle.Event.ON_RESUME -> {
                    // Retoma desde el inicio del ciclo actual de respiración
                    viewModel.reanudarDesdeForeground()
                    
                    // Asegurar refresco del estado del audio si el servicio sigue activo
                    audioService?.let {
                        viewModel.actualizarEstadoAudio(it.estaReproduciendo(), uiState.audioSeleccionado)
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // --- CAP-06-C: MANEJO CONTEXTUAL DEL BRILLO DE PANTALLA ---
    DisposableEffect(uiState.esVentanaNocturnaActiva) {
        val activity = context as? Activity
        val layoutParams = activity?.window?.attributes

        if (uiState.esVentanaNocturnaActiva) {
            // Guarda el brillo original del sistema antes de forzar el mínimo
            val originalBrightness = layoutParams?.screenBrightness ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            
            // Fuerza la reducción del brillo al 20% estipulado
            layoutParams?.screenBrightness = RelajacionConfig.VentanaNocturna.BRILLO_PANTALLA_REDUCIDO
            activity?.window?.attributes = layoutParams

            onDispose {
                // Restaura el brillo original de forma segura al salir de la ventana o del módulo
                layoutParams?.screenBrightness = originalBrightness
                activity?.window?.attributes = layoutParams
            }
        } else {
            onDispose {}
        }
    }

    // Loop periódico para mantener sincronizada la UI con el estado real del reproductor
    LaunchedEffect(uiState.estaReproduciendoAudio) {
        while (uiState.estaReproduciendoAudio) {
            audioService?.let {
                if (uiState.estaReproduciendoAudio != it.estaReproduciendo()) {
                    viewModel.actualizarEstadoAudio(it.estaReproduciendo(), uiState.audioSeleccionado)
                }
            }
            kotlinx.coroutines.delay(1000)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // --- BANNER CONTEXTUAL: CAP-06-C ---
            if (uiState.esVentanaNocturnaActiva) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                ) {
                    Text(
                        text = RelajacionConfig.VentanaNocturna.BANNER_TEXTO,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    )
                }
            }

            // Encabezado del Módulo
            Text(
                text = "Relajación Analógica",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            Text(
                text = "Estrategia de desconexión 100% libre de red",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // --- SECCIÓN CAP-06-A: RESPIREDORES RÍTMICOS ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Método Antiestrés 4-7-8",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    Text(
                        text = if (uiState.esVentanaNocturnaActiva) "★ Opción recomendada para tu ventana nocturna" else "Inhala, retén y suelta el aire rítmicamente.",
                        fontSize = 12.sp,
                        color = if (uiState.esVentanaNocturnaActiva) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (uiState.esVentanaNocturnaActiva) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Área dedicada al canvas interactivo
                    BreathingAnimationView(
                        faseActual = uiState.faseActual,
                        tiempoRestanteMs = uiState.tiempoRestanteFaseMs,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState.faseActual != FaseRespiracion.INACTIVO) {
                        Text(
                            text = "Fase actual: ${uiState.faseActual.name} • Ciclos: ${uiState.ciclosCompletados}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    // Panel de Disparadores de Respiración
                    if (uiState.faseActual == FaseRespiracion.INACTIVO) {
                        Button(
                            onClick = { viewModel.iniciarRespiracion() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Iniciar Sesión de Respiración", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.detenerRespiracion() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(text = "Finalizar y Guardar Progreso", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // --- SECCIÓN CAP-06-B: AUDIO DE FRECUENCIAS ESTABLES ---
            AudioPlayerControls(
                estaReproduciendo = uiState.estaReproduciendoAudio,
                audioSeleccionado = uiState.audioSeleccionado,
                onPlayAudio = { subtipo ->
                    // Inicia el Foreground Service pasándole el archivo a reproducir
                    val intent = Intent(context, AudioPlayerService::class.java)
                    context.startService(intent)
                    audioService?.reproducirAudio(subtipo)
                    viewModel.actualizarEstadoAudio(estaReproduciendo = true, subtipo = subtipo)
                },
                onStopAudio = {
                    audioService?.detenerAudio()
                    viewModel.actualizarEstadoAudio(estaReproduciendo = false, subtipo = null)
                }
            )
        }
    }
}