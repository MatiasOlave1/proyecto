package com.camposocampoolavevargas.proyecto.ui.screens

import androidx.lifecycle.viewModelScope
import com.camposocampoolavevargas.proyecto.data.local.UserSession
import com.camposocampoolavevargas.proyecto.data.local.dao.CircadianAlertDao
import com.camposocampoolavevargas.proyecto.data.local.entity.CircadianAlertEntity
import com.camposocampoolavevargas.proyecto.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Circadian Alerts screen.
 * Handles fetching, listing, and dismissing active alerts.
 */
@HiltViewModel
class CircadianAlertViewModel @Inject constructor(
    private val circadianAlertDao: CircadianAlertDao,
    private val userSession: UserSession
) : BaseViewModel() {

    private val _alerts = MutableStateFlow<List<CircadianAlertEntity>>(emptyList())
    val alerts: StateFlow<List<CircadianAlertEntity>> = _alerts

    init {
        loadAlerts()
    }

    fun loadAlerts() {
        val userId = userSession.getActiveUserId() ?: return
        viewModelScope.launch {
            circadianAlertDao.getActiveAlerts(userId).collect { list ->
                _alerts.value = list
            }
        }
    }

    fun dismissAlert(alertId: String) {
        viewModelScope.launch {
            circadianAlertDao.dismissAlert(alertId)
        }
    }
}
