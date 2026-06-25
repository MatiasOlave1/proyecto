package com.camposocampoolavevargas.proyecto.data.repository

import android.util.Log
import com.camposocampoolavevargas.proyecto.data.local.UserSession
import com.camposocampoolavevargas.proyecto.util.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Application-level coordinator that monitors network state transitions
 * and automatically triggers background data synchronization when connection is restored.
 */
@Singleton
class SyncCoordinator @Inject constructor(
    private val syncRepository: SyncRepository,
    private val networkMonitor: NetworkMonitor,
    private val userSession: UserSession
) {
    private val tag = "SyncCoordinator"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    /**
     * Starts monitoring connectivity changes.
     */
    @OptIn(FlowPreview::class)
    fun start() {
        scope.launch {
            networkMonitor.isOnline
                .distinctUntilChanged()
                .debounce(1000) // Filter transient network fluctuations
                .filter { it }  // Only trigger sync when connectivity is stable and true (online)
                .collectLatest {
                    val userId = userSession.getActiveUserId()
                    if (userId != null) {
                        Log.d(tag, "Network restored. Triggering syncAll for user: $userId")
                        try {
                            syncRepository.syncAll(userId)
                        } catch (e: Exception) {
                            Log.e(tag, "Error in background syncAll", e)
                        }
                    }
                }
        }
    }
}
