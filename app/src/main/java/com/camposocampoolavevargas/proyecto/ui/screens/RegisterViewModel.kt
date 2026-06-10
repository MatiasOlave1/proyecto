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

/**
 * ViewModel for user registration (RF01).
 * Validates inputs, hashes passwords, checks for duplicates, and seeds the DB.
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userDao: UserDao,
    private val userSession: UserSession
) : BaseViewModel() {

    private val _registerState = MutableStateFlow<UiState<String>>(UiState.Success(""))
    val registerState: StateFlow<UiState<String>> = _registerState

    // Standard RFC 5322 email regex pattern
    private val emailPattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$")

    /**
     * Attempts to register a new user in the database.
     */
    fun register(
        name: String,
        birthDate: Long,
        region: String,
        commune: String,
        university: String,
        career: String,
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            _registerState.value = UiState.Loading

            // 1. Check for empty fields
            if (name.isBlank() || region.isBlank() || commune.isBlank() ||
                university.isBlank() || career.isBlank() || email.isBlank() || password.isBlank()
            ) {
                _registerState.value = UiState.Error("Todos los campos obligatorios deben ser completados.")
                return@launch
            }

            // 2. Validate email format
            if (!emailPattern.matcher(email).matches()) {
                _registerState.value = UiState.Error("El formato del correo electrónico es inválido.")
                return@launch
            }

            try {
                // 3. Verify email uniqueness
                val existingUser = userDao.getUserByEmail(email.trim().lowercase())
                if (existingUser != null) {
                    _registerState.value = UiState.Error("Este correo electrónico ya está registrado.")
                    return@launch
                }

                // 4. Hash password and save new UserEntity
                val passwordHash = HashUtils.hashPassword(password)
                val userId = UUID.randomUUID().toString()
                val newUser = UserEntity(
                    userId = userId,
                    name = name.trim(),
                    birthDate = birthDate,
                    region = region.trim(),
                    commune = commune.trim(),
                    university = university.trim(),
                    career = career.trim(),
                    email = email.trim().lowercase(),
                    passwordHash = passwordHash
                )

                userDao.insertUser(newUser)
                
                // 5. Automatically log in the user
                userSession.login(userId)
                
                _registerState.value = UiState.Success(userId)
            } catch (e: Exception) {
                _registerState.value = UiState.Error(e.localizedMessage ?: "Ocurrió un error inesperado al registrar el usuario.")
            }
        }
    }
}
