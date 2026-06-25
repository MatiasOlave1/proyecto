package com.camposocampoolavevargas.proyecto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import com.camposocampoolavevargas.proyecto.navigation.AppNavigation
import com.camposocampoolavevargas.proyecto.ui.theme.DormiBienUTheme
import dagger.hilt.android.AndroidEntryPoint
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.camposocampoolavevargas.proyecto.data.repository.SyncCoordinator
import javax.inject.Inject

/**
 * Main Activity of the DormiBienU application.
 * Configured with Dagger Hilt injection and forces night mode globally.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var syncCoordinator: SyncCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Start monitoring connection and triggering background sync
        syncCoordinator.start()

        // Request Notification Permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != 
                PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // Check if we arrived here from a notification
        val navigateTo = intent.getStringExtra("navigate_to")
        val startRoute = if (navigateTo == "disconnect_reminder") {
            com.camposocampoolavevargas.proyecto.navigation.Screen.DisconnectReminder.route
        } else {
            null
        }

        // Enforce dark mode always
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        setContent {
            DormiBienUTheme {
                AppNavigation(startDestination = startRoute)
            }
        }
    }
}

