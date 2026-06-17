package com.camposocampoolavevargas.proyecto.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ==========================================
// 1. Color Scheme for ccampos / dev screens
// ==========================================
private val DarkColorScheme = darkColorScheme(
    background = Background,
    surface = Surface,
    surfaceVariant = SurfaceVariant,
    primary = Primary,
    primaryContainer = PrimaryContainer,
    onPrimary = OnPrimary,
    secondary = Secondary,
    onSecondary = OnSecondary,
    tertiary = Tertiary,
    onBackground = OnBackground,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline
)

// ==========================================
// 2. Color Scheme for molave (Classmate) screens
// ==========================================
private val DormiBienUScheme = darkColorScheme(
    primary = PrimaryOrange,
    secondary = SecondaryOrange,
    background = DarkBackground,
    surface = CardBackground,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextWhite,
    onSurface = TextWhite
)

/**
 * Custom Material Design 3 theme for DormiBienU.
 * Forces dark mode colors always, ignoring system settings.
 */
@Composable
fun DormiBienUTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val context = view.context
            if (context is Activity) {
                context.window.statusBarColor = colorScheme.background.toArgb()
                context.window.navigationBarColor = colorScheme.background.toArgb()
                val windowInsetsController = WindowCompat.getInsetsController(context.window, view)
                windowInsetsController.isAppearanceLightStatusBars = false
                windowInsetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * Classmate's Theme wrapper using their specific color palette.
 */
@Composable
fun ProyectoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DormiBienUScheme,
        typography = Typography,
        content = content
    )
}
