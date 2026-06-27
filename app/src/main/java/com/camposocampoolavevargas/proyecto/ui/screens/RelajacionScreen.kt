package com.camposocampoolavevargas.proyecto.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.camposocampoolavevargas.proyecto.relajacion.domain.model.SubtipoRelajacion
import com.camposocampoolavevargas.proyecto.relajacion.ui.components.AudioPlayerControls
import com.camposocampoolavevargas.proyecto.relajacion.ui.components.BreathingAnimationView
import com.camposocampoolavevargas.proyecto.relajacion.ui.components.SessionTimer
import com.camposocampoolavevargas.proyecto.relajacion.ui.viewmodel.RelajacionUIState
import com.camposocampoolavevargas.proyecto.relajacion.ui.viewmodel.RelajacionViewModel
import com.camposocampoolavevargas.proyecto.ui.theme.DormiBienUTheme

// Acento naranja consistente con el resto de la app
private val AcentoNaranja = Color(0xFFFFA726)
private val AcentoNaranjaClaro = Color(0xFFFFB74D)
private val VerdeExito = Color(0xFF3FB950)

// ─────────────────────────────────────────────────────────────
// PANTALLA RAÍZ
// ─────────────────────────────────────────────────────────────

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

    // Interceptar retroceso físico para interrumpir la sesión activa
    BackHandler(enabled = uiState.isSessionActive) {
        viewModel.interrumpirSesion()
    }

    LaunchedEffect(modoNocturno) {
        if (modoNocturno) viewModel.activarModoNoche()
        else viewModel.desactivarModoNoche()
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
            .background(MaterialTheme.colorScheme.background)
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
                onIncrementarCiclo = { viewModel.incrementarCiclo() },
                onBackClick = { viewModel.interrumpirSesion() }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// PANTALLA HOME — Biblioteca de herramientas
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RelajacionHomeScreen(
    userId: String,
    onIniciarSesion: (SubtipoRelajacion, Boolean) -> Unit = { _, _ -> },
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Biblioteca de Herramientas",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Banner hero ───────────────────────────────────
            BannerHero()

            // ── Sección respiración ───────────────────────────
            SeccionLabel(emoji = "🌬️", titulo = "Métodos de Respiración", color = AcentoNaranja)

            RelajacionToolCard(
                titulo = "Técnica 4-7-8",
                descripcion = "Ideal para conciliar el sueño rápido y reducir la ansiedad nocturna.",
                detalles = "Inhala 4s • Retén 7s • Exhala 8s",
                emoji = "🌬️",
                colorTheme = AcentoNaranja,
                onClick = { onIniciarSesion(SubtipoRelajacion.RESPIRACION_4_7_8, false) }
            )

            RelajacionToolCard(
                titulo = "Box Breathing",
                descripcion = "Calma la mente, reduce el estrés y recupera el enfoque.",
                detalles = "Inhala 4s • Retén 4s • Exhala 4s",
                emoji = "📦",
                colorTheme = AcentoNaranja,
                onClick = { onIniciarSesion(SubtipoRelajacion.RESPIRACION_BOX, false) }
            )

            RelajacionToolCard(
                titulo = "Respiración Coherente",
                descripcion = "Sincroniza tu ritmo cardíaco y equilibra el sistema nervioso.",
                detalles = "Inhala 4s • Exhala 6s",
                emoji = "⚖️",
                colorTheme = AcentoNaranja,
                onClick = { onIniciarSesion(SubtipoRelajacion.RESPIRACION_COHERENTE, false) }
            )

            // ── Sección audios ────────────────────────────────
            SeccionLabel(emoji = "🎵", titulo = "Audios Relajantes", color = VerdeExito)

            RelajacionToolCard(
                titulo = "Ruido Blanco",
                descripcion = "Bloquea ruidos externos con un sonido de frecuencia constante.",
                detalles = "Audio continuo • Bloqueo de distracciones",
                emoji = "⚪",
                colorTheme = VerdeExito,
                onClick = { onIniciarSesion(SubtipoRelajacion.AUDIO_RUIDO_BLANCO, true) }
            )

            RelajacionToolCard(
                titulo = "Ruido Marrón",
                descripcion = "Frecuencias bajas y profundas que favorecen la calma mental.",
                detalles = "Audio continuo • Relajación profunda",
                emoji = "🟫",
                colorTheme = VerdeExito,
                onClick = { onIniciarSesion(SubtipoRelajacion.AUDIO_RUIDO_MARRON, true) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────
// BANNER HERO
// ─────────────────────────────────────────────────────────────

@Composable
private fun BannerHero() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF1A1A2E), Color(0xFF16213E))
                )
            )
            .border(1.dp, AcentoNaranja.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "🧘", fontSize = 26.sp)
                Text(
                    text = "Herramientas de Calma",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = "Sesiones locales sin conexión para inducir el sueño profundo",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// ETIQUETA DE SECCIÓN
// ─────────────────────────────────────────────────────────────

@Composable
private fun SeccionLabel(emoji: String, titulo: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = "$emoji  $titulo",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

// ─────────────────────────────────────────────────────────────
// TARJETA DE HERRAMIENTA
// ─────────────────────────────────────────────────────────────

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
            .shadow(6.dp, RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, colorTheme.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji badge
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colorTheme.copy(alpha = 0.12f))
                    .border(1.dp, colorTheme.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Pill de detalles
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colorTheme.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = detalles,
                        style = MaterialTheme.typography.labelSmall,
                        color = colorTheme,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Flecha indicadora
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = colorTheme.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(18.dp)
                    .padding(start = 4.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// PANTALLA SESIÓN ACTIVA
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RelajacionActivoScreen(
    uiState: RelajacionUIState,
    onCompletarSesion: () -> Unit = {},
    onInterrumpirSesion: () -> Unit = {},
    onPausarReanudar: () -> Unit = {},
    onIncrementarTiempo: () -> Unit = {},
    onIncrementarCiclo: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    LaunchedEffect(uiState.isSessionActive, uiState.isAnimationRunning, uiState.isAudioPlaying) {
        if (uiState.isSessionActive && (uiState.isAnimationRunning || uiState.isAudioPlaying)) {
            while (true) {
                kotlinx.coroutines.delay(1000)
                onIncrementarTiempo()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.subtipo.displayName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Indicador de progreso animado
            val progreso by animateFloatAsState(
                targetValue = if (uiState.isAnimationRunning || uiState.isAudioPlaying) 1f else 0f,
                animationSpec = tween(600),
                label = "progreso"
            )
            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = AcentoNaranja,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )

            // Visualización principal
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
                // Panel de audio
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF1A1A2E), Color(0xFF16213E))
                            )
                        )
                        .border(1.dp, AcentoNaranja.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (uiState.isAudioPlaying) "🔊" else "⏸",
                            fontSize = 40.sp
                        )
                        Text(
                            text = if (uiState.isAudioPlaying) "Audio reproduciéndose" else "Audio pausado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Timer
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SessionTimer(
                        segundosTranscurridos = uiState.tiempoTranscurrido,
                        ciclosCompletados = uiState.ciclosCompletados
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Controles
            if (uiState.subtipo.isAudio) {
                AudioPlayerControls(
                    isPlaying = uiState.isAudioPlaying,
                    onPlayPause = onPausarReanudar,
                    onStop = onInterrumpirSesion
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onCompletarSesion,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = "✓  Completar sesión",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onPausarReanudar,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Text(
                                text = if (uiState.isAnimationRunning) "⏸  Pausar" else "▶  Reanudar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        OutlinedButton(
                            onClick = onInterrumpirSesion,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                            )
                        ) {
                            Text(
                                text = "✕  Interrumpir",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────
// PREVIEW
// ─────────────────────────────────────────────────────────────

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = 0xFF1E293B)
@Composable
private fun RelajacionToolCardPreview() {
    DormiBienUTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            RelajacionToolCard(
                titulo = "Técnica 4-7-8",
                descripcion = "Ideal para conciliar el sueño rápido.",
                detalles = "Inhala 4s • Retén 7s • Exhala 8s",
                emoji = "🌬️",
                colorTheme = AcentoNaranja,
                onClick = {}
            )
        }
    }
}