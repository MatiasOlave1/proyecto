package com.camposocampoolavevargas.proyecto.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val isOnline: Flow<Boolean> = callbackFlow {
        var isChecking = true

        val checkServerStatus = suspend {
            val hasInternet = isCurrentlyConnected()
            val isReachable = if (hasInternet) isServerReachable() else false
            trySend(isReachable)
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // Check immediately when network becomes available
                CoroutineScope(Dispatchers.IO).launch {
                    checkServerStatus()
                }
            }

            override fun onLost(network: Network) {
                trySend(false)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(request, callback)

        // Periodic polling check every 8 seconds
        val job = CoroutineScope(Dispatchers.IO).launch {
            while (isChecking) {
                checkServerStatus()
                delay(8000)
            }
        }

        awaitClose {
            isChecking = false
            job.cancel()
            try {
                connectivityManager.unregisterNetworkCallback(callback)
            } catch (e: Exception) {
                // Ignore if already unregistered
            }
        }
    }.distinctUntilChanged()

    private fun isCurrentlyConnected(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun isServerReachable(): Boolean {
        return try {
            // Check reachability of the database backend API server
            val url = java.net.URL("http://192.168.1.5:8000/api/")
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.connectTimeout = 2000 // 2 seconds timeout
            connection.readTimeout = 2000
            connection.requestMethod = "GET"
            
            // We just want to check if the server responds at all.
            // Any response code (including 200, 404, 401, 403, 500) indicates server is running.
            connection.responseCode
            true
        } catch (e: Exception) {
            false
        }
    }
}
