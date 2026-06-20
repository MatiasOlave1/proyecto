package com.dormibienu.app.relajacion.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.dormibienu.app.relajacion.config.RelajacionConfig

/**
 * Gestor centralizado de notificaciones para el Foreground Service multimedia (CAP-06-B).
 * Implementa canales con prioridad baja para cumplir con los requerimientos visuales no intrusivos.
 */
object RelajacionNotificationManager {

    /**
     * Crea o actualiza la notificación persistente del Foreground Service mientras se reproduce audio.
     */
    fun crearNotificacionServicio(context: Context, subtipo: String): Notification {
        registrarCanalNotificaciones(context)

        // Intent para abrir la UI al presionar la notificación
        val intentLaunch = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntentLaunch = PendingIntent.getActivity(
            context,
            0,
            intentLaunch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val textoSubtipo = if (subtipo.contains("BLANCO")) "Ruido Blanco" else "Ruido Marrón"

        return NotificationCompat.Builder(context, RelajacionConfig.Notification.CHANNEL_ID)
            .setContentTitle("DormiBienU — Relajación Analógica")
            .setContentText("Reproduciendo frecuencias estables: $textoSubtipo")
            // Reemplazar por R.drawable.ic_sleep_relax o similar según assets finales de res/
            .setSmallIcon(android.R.drawable.ic_media_play) 
            .setPriority(NotificationCompat.PRIORITY_LOW) // Requerimiento: PRIORITY_LOW
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntentLaunch)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    /**
     * Crea la notificación alternativa de aviso cuando la interrupción de AudioFocus supera los 60 segundos.
     */
    fun crearNotificacionPausaExcedida(context: Context): Notification {
        registrarCanalNotificaciones(context)

        val intentLaunch = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntentLaunch = PendingIntent.getActivity(
            context,
            1,
            intentLaunch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Criterio de Aceptación: Texto explícito "Audio pausado — toca para continuar"
        return NotificationCompat.Builder(context, RelajacionConfig.Notification.CHANNEL_ID)
            .setContentTitle("DormiBienU")
            .setContentText("Audio pausado — toca para continuar")
            .setSmallIcon(android.R.drawable.ic_media_pause)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntentLaunch)
            .setAutoCancel(false)
            .setOngoing(true) // Sigue en foreground para mantener vivo el proceso
            .build()
    }

    private fun registrarCanalNotificaciones(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Verificamos si ya existe para evitar sobreescribir configuraciones del usuario
            if (manager.getNotificationChannel(RelajacionConfig.Notification.CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    RelajacionConfig.Notification.CHANNEL_ID,
                    RelajacionConfig.Notification.CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW // Garantiza prioridad baja nativa
                ).apply {
                    description = "Canal local para reproducción multimedia analógica en background."
                    enableLights(false)
                    enableVibration(false)
                    setShowBadge(false)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }
}