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

@Composable
fun DormiBienUTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DormiBienUScheme
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