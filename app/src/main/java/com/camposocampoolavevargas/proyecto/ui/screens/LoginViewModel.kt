package com.camposocampoolavevargas.proyecto.ui.screens

import androidx.lifecycle.viewModelScope
import com.camposocampoolavevargas.proyecto.data.local.HashUtils
import com.camposocampoolavevargas.proyecto.data.local.UserSession
import com.camposocampoolavevargas.proyecto.data.local.dao.UserDao
import com.camposocampoolavevargas.proyecto.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for user login (RF01).
 * Verifies email/password matches, manages session persistence, and auto-logs-in existing users.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userDao: UserDao,
    private val userSession: UserSession
) : BaseViewModel() {

    private val _loginState = MutableStateFlow<UiState<String>>(UiState.Success(""))
    val loginState: StateFlow<UiState<String>> = _loginState

    /**
     * Checks if there is already an active session logged in.
     */
    fun checkAutoLogin(): String? {
        return if (userSession.isLoggedIn()) {
            userSession.getActiveUserId()
        } else {
            null
        }
    }

    /**
     * Attempts to log in with the provided email and password.
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading

            // 1. Validation
            if (email.isBlank() || password.isBlank()) {
                _loginState.value = UiState.Error("El correo y la contraseña son obligatorios.")
                return@launch
            }

            try {
                // 2. Fetch user
                val user = userDao.getUserByEmail(email.trim().lowercase())
                if (user == null) {
                    _loginState.value = UiState.Error("Credenciales inválidas. Usuario no encontrado.")
                    return@launch
                }

                // 3. Hash input password and compare
                val inputHash = HashUtils.hashPassword(password)
                if (user.passwordHash == inputHash) {
                    // 4. Save session
                    userSession.login(user.userId)
                    _loginState.value = UiState.Success(user.userId)
                } else {
                    _loginState.value = UiState.Error("Credenciales inválidas. Contraseña incorrecta.")
                }
            } catch (e: Exception) {
                _loginState.value = UiState.Error(e.localizedMessage ?: "Ocurrió un error inesperado al iniciar sesión.")
            }
        }
    }
}
