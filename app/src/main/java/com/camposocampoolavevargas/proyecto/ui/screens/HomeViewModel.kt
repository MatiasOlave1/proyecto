package com.camposocampoolavevargas.proyecto.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.camposocampoolavevargas.proyecto.data.local.UserSession
import com.camposocampoolavevargas.proyecto.data.local.dao.UserDao
import com.camposocampoolavevargas.proyecto.data.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the HomeScreen container.
 * Manages active user session logout queries and preserves the active tab selection state.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userSession: UserSession,
    private val userDao: UserDao,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(HomeTab.Dashboard)
    val selectedTab: StateFlow<HomeTab> = _selectedTab

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    init {
        loadUserEmail()
    }

    private fun loadUserEmail() {
        val userId = userSession.getActiveUserId() ?: return
        viewModelScope.launch {
            userDao.getUserById(userId).collect { user ->
                _userEmail.value = user?.email ?: ""
            }
        }
    }

    fun forceSync() {
        val userId = userSession.getActiveUserId() ?: return
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                syncRepository.syncAll(userId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun selectTab(tab: HomeTab) {
        _selectedTab.value = tab
    }

    /**
     * Clears the persistent user session.
     */
    fun logout() {
        userSession.logout()
    }
}
