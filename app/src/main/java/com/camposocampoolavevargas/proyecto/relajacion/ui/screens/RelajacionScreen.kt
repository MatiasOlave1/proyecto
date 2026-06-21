package com.camposocampoolavevargas.proyecto.relajacion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.camposocampoolavevargas.proyecto.relajacion.domain.model.SubtipoRelajacion
import com.camposocampoolavevargas.proyecto.relajacion.ui.components.AudioPlayerControls
import com.camposocampoolavevargas.proyecto.relajacion.ui.components.BreathingAnimationView
import com.camposocampoolavevargas.proyecto.relajacion.ui.components.BreathingMethodSelector
import com.camposocampoolavevargas.proyecto.relajacion.ui.components.SessionTimer
import com.camposocampoolavevargas.proyecto.relajacion.ui.viewmodel.RelajacionUIState
import com.camposocampoolavevargas.proyecto.relajacion.ui.viewmodel.RelajacionViewModel

@Composable
fun RelajacionScreen(
    viewModel: RelajacionViewModel,
    userId: String,
    modoNocturno: Boolean = false,
    onSessionCompleted: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val event by viewModel.eventEmitter.collectAsState()
    
    LaunchedEffect(modoNocturno) {
        if (modoNocturno) {
            viewModel.activarModoNoche()
        } else {
            viewModel.desactivarModoNoche()
        }
    }
    
    LaunchedEffect(event) {
        if (event != null) {
            val currentEvent = event
            when (currentEvent) {
                is com.camposocampoolavevargas.proyecto.relajacion.ui.viewmodel.RelajacionEvent.SessionCompleted -> {
                    onSessionCompleted(currentEvent.sesionId)
                }
                else -> {}
            }
            viewModel.limpiarEvento()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = Color.Black.copy(alpha = 1 - (1 - uiState.brillo))
            )
    ) {
        if (!uiState.isSessionActive) {
            RelajacionHomeScreen(
                userId = userId,
                onIniciarSesion = { subtipo, audioActivo ->
                    viewModel.iniciarSesion(userId, subtipo, audioActivo)
                }
            )
        } else {
            RelajacionActivoScreen(
                uiState = uiState,
                onCompletarSesion = { viewModel.completarSesion() },
                onInterrumpirSesion = { viewModel.interrumpirSesion() },
                onPausarReanudar = { viewModel.pausarReanudar() },
                onIncrementarTiempo = { viewModel.incrementarTiempo() },
                onIncrementarCiclo = { viewModel.incrementarCiclo() }
            )
        }
    }
}

@Composable
private fun RelajacionHomeScreen(
    userId: String,
    onIniciarSesion: (SubtipoRelajacion, Boolean) -> Unit = { _, _ -> }
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Relajación Analógica",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Offline • Local • Sin interrupciones",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Métodos de Respiración",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 12.dp)
        )
        
        val metodosRespiracion = listOf(
            SubtipoRelajacion.RESPIRACION_4_7_8 to "Técnica 4-7-8",
            SubtipoRelajacion.RESPIRACION_BOX to "Box Breathing",
            SubtipoRelajacion.RESPIRACION_COHERENTE to "Respiración Coherente"
        )
        
        metodosRespiracion.forEach { (subtipo, nombre) ->
            Button(
                onClick = { onIniciarSesion(subtipo, false) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(nombre)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Audio Local",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 12.dp)
        )
        
        val metodosAudio = listOf(
            SubtipoRelajacion.AUDIO_RUIDO_BLANCO to "Ruido Blanco",
            SubtipoRelajacion.AUDIO_RUIDO_MARRON to "Ruido Marrón"
        )
        
        metodosAudio.forEach { (subtipo, nombre) ->
            Button(
                onClick = { onIniciarSesion(subtipo, false) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(nombre)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun RelajacionActivoScreen(
    uiState: RelajacionUIState,
    onCompletarSesion: () -> Unit = {},
    onInterrumpirSesion: () -> Unit = {},
    onPausarReanudar: () -> Unit = {},
    onIncrementarTiempo: () -> Unit = {},
    onIncrementarCiclo: () -> Unit = {}
) {
    LaunchedEffect(uiState.isSessionActive, uiState.isAnimationRunning, uiState.isAudioPlaying) {
        if (uiState.isSessionActive && (uiState.isAnimationRunning || uiState.isAudioPlaying)) {
            while (true) {
                kotlinx.coroutines.delay(1000)
                onIncrementarTiempo()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = uiState.subtipo.displayName,
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (uiState.subtipo.isRespiracion) {
            BreathingAnimationView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                subtipo = uiState.subtipo,
                isRunning = uiState.isAnimationRunning,
                onCicloCompleted = { onIncrementarCiclo() }
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isAudioPlaying) {
                    Text("♪ Audio reproduciéndose")
                } else {
                    Text("♪ Audio pausado")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        SessionTimer(
            segundosTranscurridos = uiState.tiempoTranscurrido,
            ciclosCompletados = uiState.ciclosCompletados
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (uiState.subtipo.isAudio) {
            AudioPlayerControls(
                isPlaying = uiState.isAudioPlaying,
                onPlayPause = onPausarReanudar,
                onStop = onInterrumpirSesion
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onPausarReanudar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Text(if (uiState.isAnimationRunning) "Pausar" else "Reanudar")
                }
                
                Button(
                    onClick = onCompletarSesion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("Completar sesión")
                }
                
                Button(
                    onClick = onInterrumpirSesion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Interrumpir")
                }
            }
        }
    }
}
