package com.camposocampoolavevargas.proyecto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import com.camposocampoolavevargas.proyecto.navigation.AppNavigation
import com.camposocampoolavevargas.proyecto.ui.theme.DormiBienUTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity of the DormiBienU application.
 * Configured with Dagger Hilt injection and forces night mode globally.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enforce dark mode always
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        setContent {
            DormiBienUTheme {
                AppNavigation()
            }
        }
    }
}

