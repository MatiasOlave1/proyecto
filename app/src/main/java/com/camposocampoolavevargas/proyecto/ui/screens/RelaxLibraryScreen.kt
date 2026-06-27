package com.camposocampoolavevargas.proyecto.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.camposocampoolavevargas.proyecto.navigation.Screen
import com.camposocampoolavevargas.proyecto.ui.theme.DormiBienUTheme
import kotlin.math.cos
import kotlin.math.sin
import java.util.Calendar
import androidx.compose.ui.graphics.drawscope.rotate

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
            ),
            border = if (uiState.alarmSet) {
                androidx.compose.foundation.BorderStroke(
                    2.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            } else null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Alarma Inteligente",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (uiState.alarmSet) {
                        Text(
                            text = "Activa ⏰",
                            color = Color(0xFF3FB950),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "Sin Alarma",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Clock visualization container
                Box(
                    modifier = Modifier.size(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val activeColor = MaterialTheme.colorScheme.primary
                    val outlineColor = MaterialTheme.colorScheme.outline
                    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
                    val secondaryColor = MaterialTheme.colorScheme.secondary

                    // Custom drawn clock face
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val radius = size.width / 2 - 12.dp.toPx()

                        // Clock ring outline
                        drawCircle(
                            color = if (uiState.alarmSet) activeColor else outlineColor.copy(alpha = 0.4f),
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
                                color = if (i % 3 == 0) {
                                    if (uiState.alarmSet) activeColor else outlineColor
                                } else {
                                    outlineColor.copy(alpha = 0.5f)
                                },
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = if (i % 3 == 0) 3.dp.toPx() else 1.5.dp.toPx()
                            )
                        }

                        // Determine target hours and minutes to draw hands
                        val h = if (uiState.alarmSet) uiState.alarmHour else Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                        val m = if (uiState.alarmSet) uiState.alarmMinute else Calendar.getInstance().get(Calendar.MINUTE)

                        // Hour hand (H * 30 + M * 0.5) - 90 degrees
                        val hrAngle = ((h % 12) * 30 + m * 0.5 - 90) * (Math.PI / 180)
                        val hrLength = radius * 0.5f
                        drawStrokeLine(
                            color = if (uiState.alarmSet) activeColor else onSurfaceColor.copy(alpha = 0.7f),
                            start = center,
                            end = Offset(
                                center.x + cos(hrAngle).toFloat() * hrLength,
                                center.y + sin(hrAngle).toFloat() * hrLength
                            ),
                            strokeWidth = 4.dp.toPx()
                        )

                        // Minute hand (M * 6) - 90 degrees
                        val minAngle = (m * 6 - 90) * (Math.PI / 180)
                        val minLength = radius * 0.75f
                        drawStrokeLine(
                            color = if (uiState.alarmSet) secondaryColor else activeColor.copy(alpha = 0.6f),
                            start = center,
                            end = Offset(
                                center.x + cos(minAngle).toFloat() * minLength,
                                center.y + sin(minAngle).toFloat() * minLength
                            ),
                            strokeWidth = 3.dp.toPx()
                        )

                        // Center pin
                        drawCircle(
                            color = if (uiState.alarmSet) activeColor else outlineColor,
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
                            text = if (uiState.alarmSet) "Alarma Activa" else "Sin Alarma",
                            fontSize = 10.sp,
                            color = if (uiState.alarmSet) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val clockTimeText = if (uiState.alarmSet) {
                            val alarmAmPm = if (uiState.alarmHour < 12) "AM" else "PM"
                            val alarmHour12 = when {
                                uiState.alarmHour == 0 -> 12
                                uiState.alarmHour > 12 -> uiState.alarmHour - 12
                                else -> uiState.alarmHour
                            }
                            String.format("%d:%02d %s", alarmHour12, uiState.alarmMinute, alarmAmPm)
                        } else {
                            "--:--"
                        }
                        Text(
                            text = clockTimeText,
                            fontSize = 18.sp,
                            color = if (uiState.alarmSet) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (uiState.alarmSet) "Activa ⏰" else "Configurar",
                            fontSize = 8.sp,
                            color = if (uiState.alarmSet) Color(0xFF3FB950) else MaterialTheme.colorScheme.onSurfaceVariant,
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
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (uiState.spotifyTrackUri != null && uiState.spotifyTrackUri != "") {
                    Color(0xFF1DB954).copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                }
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
                        text = "Spotify",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    // Custom Canvas drawn official-style Spotify Logo
                    Canvas(modifier = Modifier.size(28.dp)) {
                        val r = size.width / 2
                        drawCircle(color = Color(0xFF1DB954), radius = r, center = center)

                        val strokeWidth = 2.5.dp.toPx()
                        
                        rotate(degrees = -10f, pivot = center) {
                            // Top wave
                            drawArc(
                                color = Color(0xFF191414),
                                startAngle = -140f,
                                sweepAngle = 100f,
                                useCenter = false,
                                topLeft = Offset(center.x - r * 0.7f, center.y - r * 0.5f),
                                size = androidx.compose.ui.geometry.Size(r * 1.4f, r * 1.0f),
                                style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                            )
                            
                            // Middle wave
                            drawArc(
                                color = Color(0xFF191414),
                                startAngle = -140f,
                                sweepAngle = 100f,
                                useCenter = false,
                                topLeft = Offset(center.x - r * 0.55f, center.y - r * 0.25f),
                                size = androidx.compose.ui.geometry.Size(r * 1.1f, r * 0.8f),
                                style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                            )
                            
                            // Bottom wave
                            drawArc(
                                color = Color(0xFF191414),
                                startAngle = -140f,
                                sweepAngle = 100f,
                                useCenter = false,
                                topLeft = Offset(center.x - r * 0.4f, center.y - r * 0.0f),
                                size = androidx.compose.ui.geometry.Size(r * 0.8f, r * 0.6f),
                                style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                var showDropdown by remember { mutableStateOf(false) }
                val activeTrackName = uiState.spotifyTrackName ?: "Ninguna (Alarma Local)"
                val activeTrack = viewModel.curatedTracks.find { it.uri == uiState.spotifyTrackUri }
                val displayArtist = when {
                    uiState.spotifyTrackUri == null || uiState.spotifyTrackUri == "" -> "Alarma Local"
                    activeTrack != null -> activeTrack.artist
                    else -> "Enlace de Spotify"
                }

                Box {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showDropdown = true },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Album Art Mockup with Spotify Brand Gradient
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            androidx.compose.ui.graphics.Brush.linearGradient(
                                                colors = listOf(Color(0xFF1DB954), Color(0xFF191414))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "🎵",
                                        color = Color.White,
                                        fontSize = 18.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Track Info
                                Column {
                                    Text(
                                        text = activeTrackName,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = displayArtist,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }
                            }

                            // Change Button / Dropdown Icon
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Cambiar",
                                    color = Color(0xFF1DB954),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Desplegar",
                                    tint = Color(0xFF1DB954)
                                )
                            }
                        }
                    }

                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false }
                    ) {
                        viewModel.curatedTracks.forEach { track ->
                            DropdownMenuItem(
                                text = { 
                                    Column {
                                        Text(track.name, fontWeight = FontWeight.SemiBold)
                                        if (track.artist.isNotEmpty()) {
                                            Text(
                                                text = track.artist,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    viewModel.selectSpotifyTrack(
                                        if (track.uri.isEmpty()) null else track.uri,
                                        if (track.uri.isEmpty()) null else track.name
                                    )
                                    showDropdown = false
                                }
                            )
                        }
                    }
                }

                val context = LocalContext.current
                val activity = remember(context) {
                    var currentContext = context
                    while (currentContext is android.content.ContextWrapper) {
                        if (currentContext is android.app.Activity) {
                            break
                        }
                        currentContext = currentContext.baseContext
                    }
                    currentContext as? android.app.Activity
                }

                val isCustomSelected = uiState.spotifyTrackUri == "custom" || 
                    (uiState.spotifyTrackUri != null && uiState.spotifyTrackUri != "" && 
                     viewModel.curatedTracks.none { it.uri == uiState.spotifyTrackUri && it.uri != "custom" })

                var customUrl by remember { mutableStateOf(if (isCustomSelected && uiState.spotifyTrackUri != "custom") uiState.spotifyTrackUri ?: "" else "") }

                if (isCustomSelected) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Configurar Canción Personalizada",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            androidx.compose.material3.OutlinedTextField(
                                value = customUrl,
                                onValueChange = { customUrl = it },
                                label = { Text("Enlace o URI de Spotify") },
                                placeholder = { Text("https://open.spotify.com/track/...") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedBorderColor = Color(0xFF1DB954),
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                androidx.compose.material3.OutlinedButton(
                                    onClick = {
                                        try {
                                            val searchIntent = Intent(Intent.ACTION_VIEW, Uri.parse("spotify:search")).apply {
                                                setPackage("com.spotify.music")
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            context.startActivity(searchIntent)
                                        } catch (e: Exception) {
                                            val webSearchIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com")).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            context.startActivity(webSearchIntent)
                                        }
                                    },
                                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFF1DB954)
                                    ),
                                    modifier = Modifier.weight(1.5f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1DB954)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("🔍 Buscar", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                                androidx.compose.material3.Button(
                                    onClick = {
                                        if (customUrl.isNotEmpty()) {
                                            viewModel.saveCustomSpotifyTrack(customUrl)
                                            android.widget.Toast.makeText(context, "Canción guardada con éxito", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1DB954)
                                    ),
                                    modifier = Modifier.weight(1.5f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Guardar", color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    androidx.compose.material3.Button(
                        onClick = {
                            if (activity != null) {
                                // 1. Traer Spotify al primer plano primero para evitar el bloqueo de inicio de actividad en segundo plano (BAL) de Android 14+
                                try {
                                    val launchIntent = context.packageManager.getLaunchIntentForPackage("com.spotify.music")
                                    if (launchIntent != null) {
                                        launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        context.startActivity(launchIntent)
                                    }
                                } catch (e: Exception) {
                                    Log.e("RelaxLibraryScreen", "No se pudo abrir la app de Spotify", e)
                                }

                                // 2. Esperar 1.5 segundos para dar tiempo a Spotify a estar en primer plano y luego conectar
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    val connectionParams = ConnectionParams.Builder("9cc6a9ed47e445039cd34190f8478a86")
                                        .setRedirectUri("dormibienu://callback")
                                        .showAuthView(true)
                                        .build()

                                    SpotifyAppRemote.connect(activity, connectionParams, object : Connector.ConnectionListener {
                                        override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                                            Log.d("RelaxLibraryScreen", "Spotify App Remote Auth Connected!")
                                            SpotifyAppRemote.disconnect(spotifyAppRemote)
                                            android.widget.Toast.makeText(context, "¡Spotify vinculado con éxito!", android.widget.Toast.LENGTH_SHORT).show()
                                        }

                                        override fun onFailure(throwable: Throwable) {
                                            Log.e("RelaxLibraryScreen", "Spotify Auth failed", throwable)
                                            android.widget.Toast.makeText(context, "Error al vincular: ${throwable.message}", android.widget.Toast.LENGTH_LONG).show()
                                        }
                                    })
                                }, 1500)
                            } else {
                                android.widget.Toast.makeText(context, "Error: No se pudo obtener el contexto", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1DB954)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("🔗 Vincular Cuenta de Spotify", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
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
