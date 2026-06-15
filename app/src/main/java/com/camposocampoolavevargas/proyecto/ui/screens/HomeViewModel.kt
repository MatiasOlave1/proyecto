package com.camposocampoolavevargas.proyecto.ui.screens

import androidx.lifecycle.ViewModel
import com.camposocampoolavevargas.proyecto.data.local.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for the HomeScreen container.
 * Manages active user session logout queries.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userSession: UserSession
) : ViewModel() {

    /**
     * Clears the persistent user session.
     */
    fun logout() {
        userSession.logout()
    }
}
