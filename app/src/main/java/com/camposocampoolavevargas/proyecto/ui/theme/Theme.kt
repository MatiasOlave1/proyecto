package com.camposocampoolavevargas.proyecto.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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
fun ProyectoTheme(
    content: @Composable () -> Unit
) {

    MaterialTheme(
        colorScheme = DormiBienUScheme,
        typography = Typography,
        content = content
    )
}