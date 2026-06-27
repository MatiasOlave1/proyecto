package com.camposocampoolavevargas.proyecto.service.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.camposocampoolavevargas.proyecto.data.local.UserSession
import com.camposocampoolavevargas.proyecto.util.NotificationHelper
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class AlarmReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AlarmReceiverEntryPoint {
        fun userSession(): UserSession
    }

    companion object {
        // TODO: Reemplazar con el Client ID de tu aplicación en el Spotify Developer Dashboard
        private const val SPOTIFY_CLIENT_ID = "9cc6a9ed47e445039cd34190f8478a86"
        // TODO: Reemplazar con la URI de Redirección configurada en tu aplicación
        private const val SPOTIFY_REDIRECT_URI = "dormibienu://callback"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // Retrieve UserSession using Hilt EntryPoint
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            AlarmReceiverEntryPoint::class.java
        )
        val userSession = entryPoint.userSession()

        val spotifyUri = userSession.getSpotifyAlarmUri()
        val trackName = userSession.getSpotifyAlarmName()

        if (!spotifyUri.isNullOrEmpty()) {
            Log.d("AlarmReceiver", "Waking up Spotify and bringing to foreground for URI: $spotifyUri (Track: $trackName)")
            
            // 1. Traer Spotify al primer plano para despertar el reproductor
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage("com.spotify.music")
                if (launchIntent != null) {
                    launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(launchIntent)
                }
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Failed to wake up Spotify app", e)
            }

            // 2. Esperar 1.5 segundos para que Spotify se inicialice en primer plano, luego conectarse y reproducir
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                val connectionParams = ConnectionParams.Builder(SPOTIFY_CLIENT_ID)
                    .setRedirectUri(SPOTIFY_REDIRECT_URI)
                    .showAuthView(false) // No mostramos ventana de auth en pantalla bloqueada/alarma
                    .build()

                SpotifyAppRemote.connect(context.applicationContext, connectionParams, object : Connector.ConnectionListener {
                    override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                        Log.d("AlarmReceiver", "Connected to Spotify App Remote successfully! Playing track...")
                        spotifyAppRemote.playerApi.play(spotifyUri)
                        
                        // Desconectar después de 5 segundos para liberar el servicio bound
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            SpotifyAppRemote.disconnect(spotifyAppRemote)
                            Log.d("AlarmReceiver", "Disconnected from Spotify App Remote after playback started")
                        }, 5000)
                    }

                    override fun onFailure(throwable: Throwable) {
                        Log.e("AlarmReceiver", "Failed to connect to Spotify App Remote, falling back to Intent redirection", throwable)
                        launchSpotifyViaIntent(context, spotifyUri, trackName)
                    }
                })
            }, 1500)
        } else {
            Log.d("AlarmReceiver", "No Spotify track configured, playing default local sound only")
        }

        // Show native notification (acting as visual fail-safe & local sound generator)
        NotificationHelper.showAchievementNotification(
            context = context,
            title = "⏰ ¡Hora de despertar!",
            message = if (!spotifyUri.isNullOrEmpty()) {
                "Tu alarma ha sonado y se está reproduciendo en Spotify: \"$trackName\"."
            } else {
                "Tu alarma de sueño inteligente ha sonado."
            }
        )
    }

    private fun launchSpotifyViaIntent(context: Context, spotifyUri: String, trackName: String?) {
        try {
            val spotifyIntent = Intent(Intent.ACTION_VIEW, Uri.parse("$spotifyUri:play")).apply {
                setPackage("com.spotify.music")
                putExtra(Intent.EXTRA_REFERRER, Uri.parse("android-app://${context.packageName}"))
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(spotifyIntent)
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Failed to launch Spotify via Intent fallback", e)
        }
    }
}