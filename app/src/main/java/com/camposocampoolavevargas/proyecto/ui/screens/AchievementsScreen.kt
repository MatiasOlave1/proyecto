package com.camposocampoolavevargas.proyecto.ui.screens

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.camposocampoolavevargas.proyecto.ui.theme.DormiBienUTheme

/**
 * Main content for the Logros tab.
 */
@Composable
fun LogrosTabContent(navController: NavController) {
    val scrollState = rememberScrollState()

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
                        text = "Logros Desbloqueados",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BadgeItem(
                            title = "Primer Paso",
                            subtitle = "1 día",
                            iconText = "⭐",
                            badgeColor = Color(0xFFF78166), // Bronze/Coral
                            modifier = Modifier.weight(1f)
                        )
                        BadgeItem(
                            title = "Perfecto",
                            subtitle = "7 días cont.",
                            iconText = "👑",
                            badgeColor = Color(0xFFE3B341), // Gold
                            modifier = Modifier.weight(1f)
                        )
                        BadgeItem(
                            title = "Disciplina",
                            subtitle = "5 días mín.",
                            iconText = "🛡️",
                            badgeColor = Color(0xFF3FB950), // Green
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // --- SECTION: IN PROGRESS ---
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Logros en Progreso",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProgressBadgeItem(
                            title = "Constancia",
                            iconText = "📅",
                            currentProgress = 0.5f,
                            progressText = "5/10 días",
                            modifier = Modifier.weight(1f)
                        )
                        ProgressBadgeItem(
                            title = "Madrugador",
                            iconText = "⏰",
                            currentProgress = 0.42f,
                            progressText = "3/7 días",
                            modifier = Modifier.weight(1f)
                        )
                        ProgressBadgeItem(
                            title = "Búho",
                            iconText = "🦉",
                            currentProgress = 0.8f,
                            progressText = "4/5 noches",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProgressBadgeItem(
                            title = "Zen",
                            iconText = "🧘",
                            currentProgress = 0.33f,
                            progressText = "1/3 sesiones",
                            modifier = Modifier.weight(1f)
                        )
                        ProgressBadgeItem(
                            title = "Sincronizado",
                            iconText = "🔄",
                            currentProgress = 0.4f,
                            progressText = "2/5 syncs",
                            modifier = Modifier.weight(1f)
                        )
                        // Empty spacer card for alignment
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(1.dp)
                        )
                    }
                }
            }
        }
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
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.5.dp, badgeColor.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
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
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
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
        // Progress Bar indicator
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
 * Standalone Screen wrapper for compatibility.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(navController: NavController) {
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
                .padding(paddingValues)
        ) {
            LogrosTabContent(navController)
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun AchievementsScreenPreview() {
    DormiBienUTheme {
        AchievementsScreen(navController = rememberNavController())
    }
}
