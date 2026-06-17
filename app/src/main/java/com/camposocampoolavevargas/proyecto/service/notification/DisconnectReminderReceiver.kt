package com.camposocampoolavevargas.proyecto.service.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.camposocampoolavevargas.proyecto.MainActivity

/**
 * BroadcastReceiver responsible for triggering the Disconnect Window notification.
 * This is called by AlarmManager at the user-configured time before bedtime.
 */
class DisconnectReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("DisconnectAlarm", "Broadcast received! Building notification...")
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Intent to open MainActivity and potentially navigate to the disconnect screen
        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "disconnect_reminder")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Use a system icon as placeholder
            .setContentTitle(context.getString(com.camposocampoolavevargas.proyecto.R.string.notification_disconnect_title))
            .setContentText(context.getString(com.camposocampoolavevargas.proyecto.R.string.notification_disconnect_body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(0xFFFF9800.toInt()) // Sunset Orange color for low luminance
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_ID = "DISCONNECT_CHANNEL"
        const val NOTIFICATION_ID = 1001
    }
}
