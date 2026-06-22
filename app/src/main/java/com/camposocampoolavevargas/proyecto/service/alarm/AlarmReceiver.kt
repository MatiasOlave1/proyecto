package com.camposocampoolavevargas.proyecto.service.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.camposocampoolavevargas.proyecto.util.NotificationHelper

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationHelper.showAchievementNotification(
            context = context,
            title = "⏰ ¡Hora de despertar!",
            message = "Tu alarma de sueño inteligente ha sonado."
        )
    }
}