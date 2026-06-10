package com.camposocampoolavevargas.proyecto.ui.screens

import androidx.lifecycle.viewModelScope
import com.camposocampoolavevargas.proyecto.data.local.HashUtils
import com.camposocampoolavevargas.proyecto.data.local.UserSession
import com.camposocampoolavevargas.proyecto.data.local.dao.UserDao
import com.camposocampoolavevargas.proyecto.data.local.entity.UserEntity
import com.camposocampoolavevargas.proyecto.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for user login (RF01 - simplified).
 * Verifies email/phone and password credentials, and handles Google login redirects.
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
     * Attempts to log in with the provided identifier (email or phone) and password.
     */
    fun login(emailOrPhone: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading

            // 1. Validation
            if (emailOrPhone.isBlank() || password.isBlank()) {
                _loginState.value = UiState.Error("El correo/teléfono y la contraseña son obligatorios.")
                return@launch
            }

            try {
                // 2. Fetch user by email or phone
                val identifier = emailOrPhone.trim().lowercase()
                val user = userDao.getUserByEmailOrPhone(identifier)
                if (user == null) {
                    _loginState.value = UiState.Error("Credenciales inválidas. Usuario no encontrado.")
                    return@launch
                }

                // 3. Verify password
                val inputHash = HashUtils.hashPassword(password)
                if (user.passwordHash == inputHash) {
                    // 4. Save session
                    userSession.login(user.userId)
                    _loginState.value = UiState.Success(user.userId)
                } else if (user.passwordHash == "GOOGLE_AUTH_ACCOUNT") {
                    _loginState.value = UiState.Error("Esta cuenta se registró con Google. Por favor, usa Iniciar Sesión con Google.")
                } else {
                    _loginState.value = UiState.Error("Credenciales inválidas. Contraseña incorrecta.")
                }
            } catch (e: Exception) {
                _loginState.value = UiState.Error(e.localizedMessage ?: "Ocurrió un error inesperado al iniciar sesión.")
            }
        }
    }

    /**
     * Logs in or creates a user account when authenticating through Google on the login screen.
     */
    fun loginWithGoogle(email: String, name: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            try {
                val cleanEmail = email.trim().lowercase()
                var user = userDao.getUserByEmailOrPhone(cleanEmail)
                
                if (user == null) {
                    val userId = UUID.randomUUID().toString()
                    user = UserEntity(
                        userId = userId,
                        email = cleanEmail,
                        passwordHash = "GOOGLE_AUTH_ACCOUNT",
                        name = name.trim()
                    )
                    userDao.insertUser(user)
                }
                
                userSession.login(user.userId)
                _loginState.value = UiState.Success(user.userId)
            } catch (e: Exception) {
                _loginState.value = UiState.Error(e.localizedMessage ?: "Ocurrió un error al iniciar sesión con Google.")
            }
        }
    }
}
