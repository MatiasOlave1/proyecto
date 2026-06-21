package com.camposocampoolavevargas.proyecto.relajacion.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.camposocampoolavevargas.proyecto.relajacion.data.repository.RelajacionRepository
import com.camposocampoolavevargas.proyecto.relajacion.domain.model.SesionRelajacion
import com.camposocampoolavevargas.proyecto.relajacion.domain.model.SubtipoRelajacion
import com.camposocampoolavevargas.proyecto.relajacion.domain.usecase.CompletarSesionRelajacionUseCase
import com.camposocampoolavevargas.proyecto.relajacion.domain.usecase.IniciarSesionRelajacionUseCase
import com.camposocampoolavevargas.proyecto.relajacion.domain.usecase.InterrumpirSesionRelajacionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext

data class RelajacionUIState(
    val isSessionActive: Boolean = false,
    val sesionActual: SesionRelajacion? = null,
    val tiempoTranscurrido: Int = 0,
    val ciclosCompletados: Int = 0,
    val isAnimationRunning: Boolean = false,
    val isAudioPlaying: Boolean = false,
    val brillo: Float = 1f,
    val subtipo: SubtipoRelajacion = SubtipoRelajacion.RESPIRACION_4_7_8
)

@HiltViewModel
class RelajacionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: RelajacionRepository,
    private val iniciarUseCase: IniciarSesionRelajacionUseCase,
    private val completarUseCase: CompletarSesionRelajacionUseCase,
    private val interrumpirUseCase: InterrumpirSesionRelajacionUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RelajacionUIState())
    val uiState: StateFlow<RelajacionUIState> = _uiState.asStateFlow()
    
    private val _eventEmitter = MutableStateFlow<RelajacionEvent?>(null)
    val eventEmitter: StateFlow<RelajacionEvent?> = _eventEmitter.asStateFlow()
    
    fun iniciarSesion(userId: String, subtipo: SubtipoRelajacion, audioActivo: Boolean = false) {
        viewModelScope.launch {
            try {
                val sesion = iniciarUseCase(userId, subtipo, audioActivo)
                _uiState.value = _uiState.value.copy(
                    isSessionActive = true,
                    sesionActual = sesion,
                    subtipo = subtipo,
                    tiempoTranscurrido = 0,
                    ciclosCompletados = 0
                )
                if (subtipo.isAudio) {
                    _uiState.value = _uiState.value.copy(isAudioPlaying = true)
                } else {
                    _uiState.value = _uiState.value.copy(isAnimationRunning = true)
                }
            } catch (e: Exception) {
                _eventEmitter.value = RelajacionEvent.Error(e.message ?: "Error al iniciar sesión")
            }
        }
    }
    
    fun completarSesion() {
        viewModelScope.launch {
            val sesion = _uiState.value.sesionActual ?: return@launch
            try {
                completarUseCase(sesion.uuid, _uiState.value.tiempoTranscurrido)
                _uiState.value = _uiState.value.copy(
                    isSessionActive = false,
                    isAnimationRunning = false,
                    isAudioPlaying = false
                )
                _eventEmitter.value = RelajacionEvent.SessionCompleted(
                    sesion.uuid,
                    _uiState.value.ciclosCompletados
                )
            } catch (e: Exception) {
                _eventEmitter.value = RelajacionEvent.Error(e.message ?: "Error al completar sesión")
            }
        }
    }
    
    fun interrumpirSesion() {
        viewModelScope.launch {
            val sesion = _uiState.value.sesionActual ?: return@launch
            try {
                interrumpirUseCase(sesion.uuid, _uiState.value.tiempoTranscurrido)
                _uiState.value = _uiState.value.copy(
                    isSessionActive = false,
                    isAnimationRunning = false,
                    isAudioPlaying = false
                )
            } catch (e: Exception) {
                _eventEmitter.value = RelajacionEvent.Error(e.message ?: "Error al interrumpir sesión")
            }
        }
    }
    
    fun pausarReanudar() {
        val currentState = _uiState.value
        if (currentState.subtipo.isAudio) {
            _uiState.value = currentState.copy(
                isAudioPlaying = !currentState.isAudioPlaying
            )
        } else {
            _uiState.value = currentState.copy(
                isAnimationRunning = !currentState.isAnimationRunning
            )
        }
    }
    
    fun incrementarTiempo() {
        _uiState.value = _uiState.value.copy(
            tiempoTranscurrido = _uiState.value.tiempoTranscurrido + 1
        )
    }
    
    fun incrementarCiclo() {
        val nuevoCiclo = _uiState.value.ciclosCompletados + 1
        _uiState.value = _uiState.value.copy(
            ciclosCompletados = nuevoCiclo
        )
        
        // Marcar como completada si alcanzó 3 ciclos
        if (nuevoCiclo >= 3) {
            _eventEmitter.value = RelajacionEvent.CiclosCompletados(nuevoCiclo)
        }
    }
    
    fun establecerBrillo(brillo: Float) {
        _uiState.value = _uiState.value.copy(brillo = brillo)
    }
    
    fun activarModoNoche() {
        establecerBrillo(0.2f)
    }
    
    fun desactivarModoNoche() {
        establecerBrillo(1f)
    }
    
    fun limpiarEvento() {
        _eventEmitter.value = null
    }
}

sealed class RelajacionEvent {
    data class SessionCompleted(val sesionId: String, val ciclos: Int) : RelajacionEvent()
    data class CiclosCompletados(val ciclos: Int) : RelajacionEvent()
    data class Error(val message: String) : RelajacionEvent()
}
