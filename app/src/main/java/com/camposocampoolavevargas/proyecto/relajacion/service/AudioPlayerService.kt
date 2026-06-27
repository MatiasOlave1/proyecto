package com.camposocampoolavevargas.proyecto.relajacion.service

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
    private var currentAssetPath: String? = null

    private val binder = AudioPlayerBinder()

    companion object {
        const val ACTION_PLAY  = "com.camposocampoolavevargas.proyecto.PLAY"
        const val ACTION_PAUSE = "com.camposocampoolavevargas.proyecto.PAUSE"
        const val ACTION_STOP  = "com.camposocampoolavevargas.proyecto.STOP"
        const val EXTRA_AUDIO_FILE = "audio_file"
        const val NOTIFICATION_ID  = 1
    }

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // ── Llamar startForeground INMEDIATAMENTE para cumplir el límite de 5s ──
        mostrarNotificacion()

        when (intent?.action) {
            ACTION_PLAY -> {
                val audioFile = intent.getStringExtra(EXTRA_AUDIO_FILE)
                if (audioFile != null && audioFile != currentAssetPath) {
                    liberarMediaPlayer()
                    currentAssetPath = audioFile
                    reproducir(audioFile)
                } else if (audioFile != null) {
                    // Misma pista — reanudar
                    reanudar()
                } else {
                    reanudar()
                }
            }
            ACTION_PAUSE -> pausar()
            ACTION_STOP  -> detener()
        }
        return START_STICKY
    }

    private fun reproducir(assetPath: String) {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            // Solicitar audio focus
            val focusResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(audioAttributes)
                    .setOnAudioFocusChangeListener(this)
                    .build()
                audioManager.requestAudioFocus(audioFocusRequest!!)
            } else {
                @Suppress("DEPRECATION")
                audioManager.requestAudioFocus(
                    this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN
                )
            }

            if (focusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return

            // Abrir asset con AssetFileDescriptor
            val afd = assets.openFd(assetPath)

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(audioAttributes)
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                isLooping = true
                setOnPreparedListener { mp ->
                    mp.setVolume(0.7f, 0.7f)
                    mp.start()
                }
                setOnErrorListener { _, _, _ -> false }
                prepareAsync()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pausar() {
        mediaPlayer?.let { if (it.isPlaying) it.pause() }
    }

    fun reanudar() {
        try {
            mediaPlayer?.let {
                if (!it.isPlaying) {
                    it.start()
                }
            } ?: run {
                // Si el mediaPlayer fue liberado, volver a cargar
                currentAssetPath?.let { reproducir(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Intentar recargar si hay error de estado
            currentAssetPath?.let {
                liberarMediaPlayer()
                reproducir(it)
            }
        }
    }

    fun detener() {
        liberarMediaPlayer()
        currentAssetPath = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest != null) {
            audioManager.abandonAudioFocusRequest(audioFocusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(this)
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun liberarMediaPlayer() {
        mediaPlayer?.let {
            try { if (it.isPlaying) it.stop() } catch (_: Exception) {}
            it.release()
        }
        mediaPlayer = null
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (ducked) {
                    mediaPlayer?.setVolume(0.7f, 0.7f)
                    ducked = false
                }
                if (pausedDueToLoss) {
                    mediaPlayer?.let { if (!it.isPlaying) it.start() }
                    pausedDueToLoss = false
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
            AudioManager.AUDIOFOCUS_LOSS -> detener()
        }
    }

    private fun mostrarNotificacion() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, "relajacion_channel")
            .setContentTitle("Relajación")
            .setContentText("Audio reproduciéndose...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder = binder

    inner class AudioPlayerBinder : Binder() {
        fun getService(): AudioPlayerService = this@AudioPlayerService
    }
}