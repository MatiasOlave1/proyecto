package com.camposocampoolavevargas.proyecto.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * Utility helper to manage Android notification channels and show local push notifications
 * for unlocked achievements.
 */
object NotificationHelper {
    private const val CHANNEL_ID = "achievements_channel"
    private const val CHANNEL_NAME = "Logros y Medallas"
    private const val CHANNEL_DESC = "Notificaciones para logros y recompensas desbloqueadas"

    /**
     * Initializes the notification channel on Android Oreo (API 26) and above.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Triggers a local push notification showing the unlocked achievement.
     * Catches [SecurityException] on Android 13+ if notification permission is not granted.
     */
    fun showAchievementNotification(context: Context, title: String, message: String) {
        createNotificationChannel(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.btn_star) // Safe standard star icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(System.currentTimeMillis().toInt(), builder.build())
            }
        } catch (e: Exception) {
            // Gracefully handle any permission or system errors
            e.printStackTrace()
        }
    }
}
