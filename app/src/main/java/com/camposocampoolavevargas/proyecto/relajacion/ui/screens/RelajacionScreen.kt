package com.camposocampoolavevargas.proyecto.relajacion.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onSessionCompleted: (String) -> Unit = {},
    onBackClick: () -> Unit = {}
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
                color = Color.Black.copy(alpha = 1f - uiState.brillo)
            )
    ) {
        if (!uiState.isSessionActive) {
            RelajacionHomeScreen(
                userId = userId,
                onIniciarSesion = { subtipo, audioActivo ->
                    viewModel.iniciarSesion(userId, subtipo, audioActivo)
                },
                onBackClick = onBackClick
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
fun RelajacionToolCard(
    titulo: String,
    descripcion: String,
    detalles: String,
    emoji: String,
    colorTheme: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, colorTheme.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji badge with colored background
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(colorTheme.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 24.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = detalles,
                    style = MaterialTheme.typography.labelSmall,
                    color = colorTheme,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun RelajacionHomeScreen(
    userId: String,
    onIniciarSesion: (SubtipoRelajacion, Boolean) -> Unit = { _, _ -> },
    onBackClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Regulación de Respiración",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Herramientas de Calma",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.Start)
        )
        
        Text(
            text = "Sesiones locales sin conexión para inducir el sueño profundo",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(top = 4.dp, bottom = 24.dp)
        )
        
        Text(
            text = "Métodos de Respiración",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 12.dp)
        )

        RelajacionToolCard(
            titulo = "Técnica 4-7-8",
            descripcion = "Ideal para conciliar el sueño rápido y reducir la ansiedad nocturna.",
            detalles = "Ciclo: Inhala 4s • Retén 7s • Exhala 8s",
            emoji = "🌬️",
            colorTheme = MaterialTheme.colorScheme.primary,
            onClick = { onIniciarSesion(SubtipoRelajacion.RESPIRACION_4_7_8, false) }
        )

        RelajacionToolCard(
            titulo = "Box Breathing",
            descripcion = "Utilizada para calmar la mente, reducir el estrés y recuperar el enfoque.",
            detalles = "Ciclo: Inhala 4s • Retén 4s • Exhala 4s",
            emoji = "📦",
            colorTheme = MaterialTheme.colorScheme.primary,
            onClick = { onIniciarSesion(SubtipoRelajacion.RESPIRACION_BOX, false) }
        )

        RelajacionToolCard(
            titulo = "Respiración Coherente",
            descripcion = "Sincroniza tu ritmo cardíaco y equilibra el sistema nervioso.",
            detalles = "Ciclo: Inhala 4s • Exhala 6s",
            emoji = "⚖️",
            colorTheme = MaterialTheme.colorScheme.primary,
            onClick = { onIniciarSesion(SubtipoRelajacion.RESPIRACION_COHERENTE, false) }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Audios Relajantes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 12.dp)
        )

        RelajacionToolCard(
            titulo = "Ruido Blanco",
            descripcion = "Bloquea ruidos externos molestos mediante un sonido de frecuencia constante.",
            detalles = "Audio continuo • Bloqueo de distracciones",
            emoji = "⚪",
            colorTheme = MaterialTheme.colorScheme.secondary,
            onClick = { onIniciarSesion(SubtipoRelajacion.AUDIO_RUIDO_BLANCO, false) }
        )

        RelajacionToolCard(
            titulo = "Ruido Marrón",
            descripcion = "Frecuencias bajas y profundas que favorecen la calma mental profunda.",
            detalles = "Audio continuo • Relajación profunda",
            emoji = "🟫",
            colorTheme = MaterialTheme.colorScheme.secondary,
            onClick = { onIniciarSesion(SubtipoRelajacion.AUDIO_RUIDO_MARRON, false) }
        )
        
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
