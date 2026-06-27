package com.camposocampoolavevargas.proyecto.relajacion.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.camposocampoolavevargas.proyecto.R

class RelajacionNotificationManager(private val context: Context) {
    
    companion object {
        const val CHANNEL_RELAJACION = "relajacion_channel"
        const val CHANNEL_AUDIO = "audio_channel"
        const val NOTIFICATION_SESION_COMPLETADA = 100
        const val NOTIFICATION_AUDIO_PAUSADO = 101
    }
    
    init {
        crearCanales()
    }
    
    private fun crearCanales() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = 
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Canal para sesiones de relajación
            val canalRelajacion = NotificationChannel(
                CHANNEL_RELAJACION,
                "Sesiones de Relajación",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificaciones de sesiones de relajación"
                enableVibration(false)
            }
            
            // Canal para audio
            val canalAudio = NotificationChannel(
                CHANNEL_AUDIO,
                "Audio de Relajación",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Reproductor de audio de relajación"
                enableVibration(false)
            }
            
            notificationManager.createNotificationChannel(canalRelajacion)
            notificationManager.createNotificationChannel(canalAudio)
        }
    }
    
    fun mostrarSesionCompletada(titulo: String, mensaje: String) {
        val notificationManager = 
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val notification = NotificationCompat.Builder(context, CHANNEL_RELAJACION)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_SESION_COMPLETADA, notification)
    }
    
    fun mostrarAudioPausado() {
        val notificationManager = 
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val notification = NotificationCompat.Builder(context, CHANNEL_AUDIO)
            .setContentTitle("Audio pausado")
            .setContentText("Toca para continuar reproduciendo")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_AUDIO_PAUSADO, notification)
    }
}
