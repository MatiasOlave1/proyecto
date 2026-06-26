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
import java.util.regex.Pattern
import javax.inject.Inject

import com.camposocampoolavevargas.proyecto.data.repository.SyncRepository
import com.camposocampoolavevargas.proyecto.UserManager

/**
 * ViewModel for user registration (RF01 - simplified).
 * Requires only email, password and optionally phone. Supports Google account persistence.
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userDao: UserDao,
    private val userSession: UserSession,
    private val syncRepository: SyncRepository
) : BaseViewModel() {

    private val _registerState = MutableStateFlow<UiState<String>>(UiState.Success(""))
    val registerState: StateFlow<UiState<String>> = _registerState

    private val emailPattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")

    /**
     * Registers a new user locally using email, password, and optionally phone.
     */
    fun register(
        email: String,
        password: String,
        phone: String? = null
    ) {
        viewModelScope.launch {
            _registerState.value = UiState.Loading

            // 1. Validate mandatory fields
            if (email.isBlank() || password.isBlank()) {
                _registerState.value = UiState.Error("El correo y la contraseña son obligatorios.")
                return@launch
            }

            // 2. Validate email format
            if (!emailPattern.matcher(email).matches()) {
                _registerState.value = UiState.Error("El formato del correo electrónico es inválido.")
                return@launch
            }

            try {
                val cleanEmail = email.trim().lowercase()
                val cleanPhone = phone?.trim()?.takeIf { it.isNotEmpty() }

                // 3. Verify user uniqueness
                val existingUser = userDao.getUserByEmail(cleanEmail)
                if (existingUser != null) {
                    _registerState.value = UiState.Error("Este correo electrónico ya está registrado.")
                    return@launch
                }

                if (cleanPhone != null) {
                    val existingPhone = userDao.getUserByPhone(cleanPhone)
                    if (existingPhone != null) {
                        _registerState.value = UiState.Error("Este número de teléfono ya está registrado.")
                        return@launch
                    }
                }

                // 4. Hash password and register
                val passwordHash = HashUtils.hashPassword(password)
                val userId = UUID.randomUUID().toString()

                val apiResult = syncRepository.register(
                    userId = userId,
                    email = cleanEmail,
                    passwordHash = passwordHash,
                    phone = cleanPhone
                )

                if (apiResult.isSuccess) {
                    val serverUserId = apiResult.getOrThrow()
                    _registerState.value = UiState.Success(serverUserId)
                    return@launch
                }

                // If API call failed, check if it's a network issue to attempt offline registration
                val exception = apiResult.exceptionOrNull()
                val isNetworkError = exception is java.io.IOException || exception?.cause is java.io.IOException

                if (isNetworkError) {
                    // Save credentials temporarily in memory for background sync
                    UserManager.correoRegistrado = cleanEmail
                    UserManager.passwordRegistrada = password

                    val newUser = UserEntity(
                        userId = userId,
                        email = cleanEmail,
                        passwordHash = passwordHash,
                        phone = cleanPhone
                    )
                    userDao.insertUser(newUser)
                    userSession.login(userId)
                    _registerState.value = UiState.Success(userId)
                    return@launch
                }

                _registerState.value = UiState.Error(
                    exception?.localizedMessage ?: "Ocurrió un error al registrarse en el servidor."
                )
            } catch (e: android.database.sqlite.SQLiteConstraintException) {
                val msg = e.message ?: ""
                if (msg.contains("email")) {
                    _registerState.value = UiState.Error("Este correo electrónico ya está registrado.")
                } else if (msg.contains("phone")) {
                    _registerState.value = UiState.Error("Este número de teléfono ya está registrado.")
                } else {
                    _registerState.value = UiState.Error("Este correo o teléfono ya están registrados.")
                }
            } catch (e: Exception) {
                _registerState.value = UiState.Error(e.localizedMessage ?: "Ocurrió un error inesperado al registrarse.")
            }
        }
    }

    /**
     * Handles Google login/register success.
     * Persists the Google account details locally if they don't already exist.
     */
    fun registerOrLoginWithGoogle(email: String, name: String) {
        viewModelScope.launch {
            _registerState.value = UiState.Loading
            try {
                val cleanEmail = email.trim().lowercase()
                var user = userDao.getUserByEmail(cleanEmail)
                
                if (user == null) {
                    // Create new local representation for Google user
                    val userId = UUID.randomUUID().toString()
                    user = UserEntity(
                        userId = userId,
                        email = cleanEmail,
                        passwordHash = "GOOGLE_AUTH_ACCOUNT", // Special tag for non-password users
                        name = name.trim()
                    )
                    userDao.insertUser(user)
                }
                
                userSession.login(user.userId)
                _registerState.value = UiState.Success(user.userId)
            } catch (e: Exception) {
                _registerState.value = UiState.Error(e.localizedMessage ?: "Ocurrió un error al iniciar sesión con Google.")
            }
        }
    }
}
