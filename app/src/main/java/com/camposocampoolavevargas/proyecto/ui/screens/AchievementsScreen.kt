package com.camposocampoolavevargas.proyecto.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.camposocampoolavevargas.proyecto.data.local.entity.AchievementEntity
import com.camposocampoolavevargas.proyecto.data.local.model.AchievementType
import com.camposocampoolavevargas.proyecto.ui.theme.DormiBienUTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class AchievementDisplay(
    val title: String,
    val description: String,
    val icon: String,
    val color: Color,
    val progress: Float,
    val progressText: String
)

private fun getAchievementDisplay(
    type: AchievementType,
    currentStreak: Int,
    unlocked: Boolean,
    points: Int
): AchievementDisplay {
    return when (type) {
        AchievementType.FIRST_RECORD -> AchievementDisplay(
            title = "Primer Paso",
            description = "1er registro",
            icon = "⭐",
            color = Color(0xFFF78166), // Coral / Bronze
            progress = if (unlocked) 1.0f else 0.0f,
            progressText = if (unlocked) "1/1" else "0/1"
        )
        AchievementType.DISCIPLINE_5_DAYS -> AchievementDisplay(
            title = "Disciplina",
            description = "5 d mín.",
            icon = "🛡️",
            color = Color(0xFF3FB950), // Green
            progress = (currentStreak / 5.0f).coerceAtMost(1.0f),
            progressText = "${currentStreak.coerceAtMost(5)}/5 d"
        )
        AchievementType.PERFECT_WEEK -> AchievementDisplay(
            title = "Perfecto",
            description = "7 d cont.",
            icon = "👑",
            color = Color(0xFFE3B341), // Gold
            progress = (currentStreak / 7.0f).coerceAtMost(1.0f),
            progressText = "${currentStreak.coerceAtMost(7)}/7 d"
        )
        AchievementType.STREAK_10 -> AchievementDisplay(
            title = "Constancia",
            description = "10 d cont.",
            icon = "📅",
            color = Color(0xFF58A6FF), // Blue
            progress = (currentStreak / 10.0f).coerceAtMost(1.0f),
            progressText = "${currentStreak.coerceAtMost(10)}/10 d"
        )
        AchievementType.STREAK_30 -> AchievementDisplay(
            title = "Búho Sinc.",
            description = "30 d cont.",
            icon = "🦉",
            color = Color(0xFFBC8CFF), // Purple
            progress = (currentStreak / 30.0f).coerceAtMost(1.0f),
            progressText = "${currentStreak.coerceAtMost(30)}/30 d"
        )
        AchievementType.EASTER_EGG -> AchievementDisplay(
            title = "Prueba 🥚",
            description = "Reclamado: ${points / 10} veces",
            icon = "🥚",
            color = Color(0xFFFF79C6), // Pink
            progress = if (unlocked) 1.0f else 0.0f,
            progressText = "Reclamado: ${points / 10} veces"
        )
    }
}

private fun getAchievementInstructions(type: AchievementType): String {
    return when (type) {
        AchievementType.FIRST_RECORD -> "Registra tu primera noche de descanso en la pestaña de Registro de Sueño para desbloquear esta medalla."
        AchievementType.DISCIPLINE_5_DAYS -> "Mantén una racha activa de registro de sueño de al menos 5 días consecutivos para conseguir esta medalla."
        AchievementType.PERFECT_WEEK -> "Registra tu sueño durante 7 días consecutivos. ¡Una semana completa de hábitos saludables!"
        AchievementType.STREAK_10 -> "Alcanza una racha de 10 días seguidos registrando tu descanso para demostrar tu constancia."
        AchievementType.STREAK_30 -> "Alcanza la racha definitiva de 30 días consecutivos de registro de sueño."
        AchievementType.EASTER_EGG -> "¡Easter Egg secreto de pruebas! Presiona el botón al final de la pantalla 5 veces consecutivas dentro de un lapso de 10 segundos. ¡Se puede reclamar infinitas veces!"
    }
}

private fun formatDate(timestamp: Long?): String {
    if (timestamp == null) return "Fecha desconocida"
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        "Fecha desconocida"
    }
}

/**
 * Main content for the Logros tab, dynamically connected to the Hilt AchievementsViewModel.
 */
@Composable
fun LogrosTabContent(
    navController: NavController,
    viewModel: AchievementsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.isLoading) {
            androidx.compose.material3.CircularProgressIndicator()
        } else {
            val (unlocked, locked) = uiState.achievements.partition { it.unlocked }
            LogrosTabContentImpl(
                navController = navController,
                unlockedAchievements = unlocked,
                lockedAchievements = locked,
                currentStreak = uiState.currentStreak,
                onTestClick = { viewModel.onTestButtonClick() }
            )
        }
    }
}

/**
 * Presentational implementation of the achievements list.
 */
@Composable
fun LogrosTabContentImpl(
    navController: NavController,
    unlockedAchievements: List<AchievementEntity>,
    lockedAchievements: List<AchievementEntity>,
    currentStreak: Int,
    onTestClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    var selectedAchievementForDetails by remember { mutableStateOf<AchievementEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Main Container Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Tus Medallas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // --- SECTION: UNLOCKED MEDALS ---
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Logros Desbloqueados (${unlockedAchievements.size})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (unlockedAchievements.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "¡Registra tu sueño para empezar a desbloquear medallas! ⭐",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        unlockedAchievements.chunked(3).forEach { rowList ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowList.forEach { ach ->
                                    val display = getAchievementDisplay(ach.type, currentStreak, true, ach.points)
                                    BadgeItem(
                                        title = display.title,
                                        subtitle = display.description,
                                        iconText = display.icon,
                                        badgeColor = display.color,
                                        onClick = { selectedAchievementForDetails = ach },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                repeat(3 - rowList.size) {
                                    Box(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // --- SECTION: IN PROGRESS ---
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Logros en Progreso (${lockedAchievements.size})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (lockedAchievements.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "¡Felicidades! Has desbloqueado todos los logros. 🎉",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        lockedAchievements.chunked(3).forEach { rowList ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowList.forEach { ach ->
                                    val display = getAchievementDisplay(ach.type, currentStreak, false, ach.points)
                                    ProgressBadgeItem(
                                        title = display.title,
                                        iconText = display.icon,
                                        currentProgress = display.progress,
                                        progressText = display.progressText,
                                        onClick = { selectedAchievementForDetails = ach },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                repeat(3 - rowList.size) {
                                    Box(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Test / Easter Egg Button
                androidx.compose.material3.Button(
                    onClick = onTestClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Easter Egg 🥚 (Presiona 5 veces en 10s)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }

    // Detail dialog when an achievement is clicked
    selectedAchievementForDetails?.let { ach ->
        val display = getAchievementDisplay(ach.type, currentStreak, ach.unlocked, ach.points)
        val instructions = getAchievementInstructions(ach.type)

        AlertDialog(
            onDismissRequest = { selectedAchievementForDetails = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(display.color.copy(alpha = 0.15f))
                            .border(1.dp, display.color, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = display.icon, fontSize = 18.sp)
                    }
                    Text(
                        text = display.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Status
                    Row {
                        Text(
                            text = "Estado: ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (ach.unlocked) {
                                "Desbloqueado (${formatDate(ach.unlockedAt)})"
                            } else {
                                "Bloqueado (${display.progressText})"
                            },
                            fontSize = 14.sp,
                            color = if (ach.unlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }

                    // Puntos
                    Row {
                        Text(
                            text = "Recompensa: ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${ach.points} Puntos",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Instructions
                    Text(
                        text = "¿Cómo conseguirlo?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = instructions,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedAchievementForDetails = null }) {
                    Text("Cerrar")
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

/**
 * Custom Unlocked Badge Item representation.
 */
@Composable
fun BadgeItem(
    title: String,
    subtitle: String,
    iconText: String,
    badgeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.5.dp, badgeColor.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(badgeColor.copy(alpha = 0.15f))
                .border(1.dp, badgeColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = iconText, fontSize = 22.sp)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/**
 * Custom Locked/In-Progress Badge representation.
 */
@Composable
fun ProgressBadgeItem(
    title: String,
    iconText: String,
    currentProgress: Float,
    progressText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
                .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = iconText,
                fontSize = 20.sp,
                modifier = Modifier.graphicsLayer(alpha = 0.4f)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { currentProgress },
            modifier = Modifier
                .width(54.dp)
                .height(4.dp)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = progressText,
            fontSize = 8.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Screen entry point containing Hilt dependency injection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    navController: NavController,
    viewModel: AchievementsViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Logros y Medallas") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            LogrosTabContent(navController, viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun AchievementsScreenPreview() {
    DormiBienUTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Logros y Medallas") }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                LogrosTabContentImpl(
                    navController = rememberNavController(),
                    unlockedAchievements = listOf(
                        AchievementEntity(userId = "preview", type = AchievementType.FIRST_RECORD, unlocked = true, points = 50)
                    ),
                    lockedAchievements = listOf(
                        AchievementEntity(userId = "preview", type = AchievementType.DISCIPLINE_5_DAYS, unlocked = false, points = 100),
                        AchievementEntity(userId = "preview", type = AchievementType.PERFECT_WEEK, unlocked = false, points = 150)
                    ),
                    currentStreak = 3,
                    onTestClick = {}
                )
            }
        }
    }
}
