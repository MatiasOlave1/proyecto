package com.dormibienu.app.relajacion.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class AudioPlayerService : Service() {

    private val binder = LocalBinder()
    private var reproduciendo = false
    private var subtipoActual: String? = null

    inner class LocalBinder : Binder() {
        fun getService(): AudioPlayerService = this@AudioPlayerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    fun reproducirAudio(subtipo: String) {
        subtipoActual = subtipo
        reproduciendo = true
        // TODO: implementar reproducción real
    }

    fun detenerAudio() {
        reproduciendo = false
        subtipoActual = null
        // TODO: detener reproducción real
    }

    fun estaReproduciendo(): Boolean = reproduciendo

    override fun onDestroy() {
        super.onDestroy()
        reproduciendo = false
    }
}