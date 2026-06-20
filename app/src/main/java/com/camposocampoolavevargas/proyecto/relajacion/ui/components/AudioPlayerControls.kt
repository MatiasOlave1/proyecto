package com.dormibienu.app.relajacion.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dormibienu.app.relajacion.domain.model.SesionRelajacion

/**
 * Panel de control táctil 100% en Jetpack Compose para gestionar la reproducción
 * de frecuencias estables en local sin consumo de red.
 */
@Composable
fun AudioPlayerControls(
    estaReproduciendo: Boolean,
    audioSeleccionado: String?,
    onPlayAudio: (String) -> Unit,
    onStopAudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Frecuencias de Relajación Analógica",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Loops continuos sin costuras (< 5 MB) • Offline-First",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // --- BOTÓN RUIDO BLANCO ---
                val esBlancoActivo = estaReproduciendo && audioSeleccionado == SesionRelajacion.SUBTIPO_AUDIO_BLANCO
                Button(
                    onClick = { onPlayAudio(SesionRelajacion.SUBTIPO_AUDIO_BLANCO) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (esBlancoActivo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(
                        text = if (esBlancoActivo) "🟢 Ruido Blanco" else "Ruido Blanco",
                        fontWeight = FontWeight.Medium
                    )
                }

                // --- BOTÓN RUIDO MARRÓN ---
                val esMarronActivo = estaReproduciendo && audioSeleccionado == SesionRelajacion.SUBTIPO_AUDIO_MARRON
                Button(
                    onClick = { onPlayAudio(SesionRelajacion.SUBTIPO_AUDIO_MARRON) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (esMarronActivo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(
                        text = if (esMarronActivo) "🟢 Ruido Marrón" else "Ruido Marrón",
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // --- BOTÓN DETENER REPRODUCCIÓN ---
            if (estaReproduciendo) {
                Button(
                    onClick = onStopAudio,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(text = "Detener Audio", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}