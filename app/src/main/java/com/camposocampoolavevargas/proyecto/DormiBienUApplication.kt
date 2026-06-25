package com.camposocampoolavevargas.proyecto

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

/**
 * Main Application class for DormiBienU.
 * Initializes Dagger Hilt dependency injection.
 */
@HiltAndroidApp
class DormiBienUApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            // Canal existente
            val disconnectChannel = NotificationChannel(
                "DISCONNECT_CHANNEL",
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_description)
            }

            // Canal nuevo para audio de relajación
            val relajacionChannel = NotificationChannel(
                "relajacion_channel",
                "Relajación",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Audio de relajación en reproducción"
            }

            notificationManager.createNotificationChannel(disconnectChannel)
            notificationManager.createNotificationChannel(relajacionChannel)
        }
    }
}
