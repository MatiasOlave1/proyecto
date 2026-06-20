package com.camposocampoolavevargas.proyecto.relajacion.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val PauseIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Pause",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            moveTo(6f, 19f)
            lineTo(10f, 19f)
            lineTo(10f, 5f)
            lineTo(6f, 5f)
            close()
            moveTo(14f, 5f)
            lineTo(14f, 19f)
            lineTo(18f, 19f)
            lineTo(18f, 5f)
            close()
        }
    }.build()

@Composable
fun AudioPlayerControls(
    isPlaying: Boolean = false,
    onPlayPause: () -> Unit = {},
    onStop: () -> Unit = {},
    volume: Float = 0.7f,
    onVolumeChange: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onPlayPause,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isPlaying) PauseIcon else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pausar" else "Reproducir"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isPlaying) "Pausar" else "Reproducir")
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = onStop,
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp)
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Detener",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Volumen",
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            text = "${(volume * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
fun BreathingMethodSelector(
    methods: List<Pair<String, String>>,
    selectedMethod: String = "",
    onMethodSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = "Selecciona tu método",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        methods.forEach { (id, name) ->
            Button(
                onClick = { onMethodSelected(id) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedMethod == id) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Text(name)
            }
        }
    }
}

@Composable
fun SessionTimer(
    segundosTranscurridos: Int = 0,
    ciclosCompletados: Int = 0,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Tiempo",
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = formatTime(segundosTranscurridos),
                style = MaterialTheme.typography.headlineMedium
            )
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Ciclos",
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = ciclosCompletados.toString(),
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

private fun formatTime(segundos: Int): String {
    val minutos = segundos / 60
    val seg = segundos % 60
    return String.format("%02d:%02d", minutos, seg)
}
