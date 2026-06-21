package com.camposocampoolavevargas.proyecto.relajacion.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.camposocampoolavevargas.proyecto.R

class AudioPlayerService : Service(), AudioManager.OnAudioFocusChangeListener {
    
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    private var ducked = false
    private var pausedDueToLoss = false
    
    private val binder = AudioPlayerBinder()
    
    companion object {
        const val ACTION_PLAY = "com.camposocampoolavevargas.proyecto.PLAY"
        const val ACTION_PAUSE = "com.camposocampoolavevargas.proyecto.PAUSE"
        const val ACTION_STOP = "com.camposocampoolavevargas.proyecto.STOP"
        const val EXTRA_AUDIO_FILE = "audio_file"
        const val NOTIFICATION_ID = 1
    }
    
    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val audioFile = intent.getStringExtra(EXTRA_AUDIO_FILE) ?: return START_STICKY
                reproducir(audioFile)
            }
            ACTION_PAUSE -> pausar()
            ACTION_STOP -> detener()
        }
        return START_STICKY
    }
    
    fun reproducir(audioFile: String) {
        try {
            // Solicitar audio focus
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            
            audioFocusRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).apply {
                    setAudioAttributes(audioAttributes)
                    setOnAudioFocusChangeListener(this@AudioPlayerService)
                }.build()
            } else {
                null
            }
            
            val focusResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest != null) {
                audioManager.requestAudioFocus(audioFocusRequest!!)
            } else {
                @Suppress("DEPRECATION")
                audioManager.requestAudioFocus(
                    this,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                )
            }
            
            if (focusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                return
            }
            
            // Inicializar MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(audioFile)
                isLooping = true

                setOnPreparedListener {
                    it.setVolume(0.7f, 0.7f)
                    it.start()
                }

                prepareAsync()
            }
            
            mostrarNotificacion()
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun pausar() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
    }
    
    fun reanudar() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
            }
        }
    }
    
    fun detener() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
        
        // Liberar audio focus
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest != null) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(this)
        }
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    
    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                mediaPlayer?.let {
                    if (!it.isPlaying && pausedDueToLoss) {
                        it.start()
                        pausedDueToLoss = false
                    }
                }
                if (ducked) {
                    mediaPlayer?.setVolume(1f, 1f)
                    ducked = false
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                mediaPlayer?.setVolume(0.3f, 0.3f)
                ducked = true
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        it.pause()
                        pausedDueToLoss = true
                    }
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                detener()
            }
        }
    }
    
    private fun mostrarNotificacion() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(this, "relajacion_channel")
            .setContentTitle("Relajación Analógica")
            .setContentText("Audio reproduciéndose...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        startForeground(NOTIFICATION_ID, notification)
    }
    
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
    
    inner class AudioPlayerBinder : Binder() {
        fun getService(): AudioPlayerService = this@AudioPlayerService
    }
}
