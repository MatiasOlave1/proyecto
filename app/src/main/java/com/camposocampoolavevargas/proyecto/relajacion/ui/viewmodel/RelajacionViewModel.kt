package com.camposocampoolavevargas.proyecto.relajacion.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.camposocampoolavevargas.proyecto.relajacion.config.RelajacionConfig
import com.camposocampoolavevargas.proyecto.relajacion.data.repository.RelajacionRepository
import com.camposocampoolavevargas.proyecto.relajacion.domain.model.SesionRelajacion
import com.camposocampoolavevargas.proyecto.relajacion.domain.model.SubtipoRelajacion
import com.camposocampoolavevargas.proyecto.relajacion.domain.usecase.CompletarSesionRelajacionUseCase
import com.camposocampoolavevargas.proyecto.relajacion.domain.usecase.IniciarSesionRelajacionUseCase
import com.camposocampoolavevargas.proyecto.relajacion.domain.usecase.InterrumpirSesionRelajacionUseCase
import com.camposocampoolavevargas.proyecto.relajacion.service.AudioPlayerService
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

    // ─────────────────────────────────────────────────────────────
    // Helpers para obtener la ruta de asset según subtipo
    // ─────────────────────────────────────────────────────────────

    private fun assetPathParaSubtipo(subtipo: SubtipoRelajacion): String? = when (subtipo) {
        SubtipoRelajacion.AUDIO_RUIDO_BLANCO -> RelajacionConfig.ASSET_RUIDO_BLANCO
        SubtipoRelajacion.AUDIO_RUIDO_MARRON -> RelajacionConfig.ASSET_RUIDO_MARRON
        else -> null
    }

    /**
     * Convierte un path de asset relativo en una URI que MediaPlayer puede abrir.
     * Formato: "file:///android_asset/audio/ruido_blanco.mp3"
     */
    private fun assetUri(assetPath: String): String = "file:///android_asset/$assetPath"

    // ─────────────────────────────────────────────────────────────
    // Control del AudioPlayerService
    // ─────────────────────────────────────────────────────────────

    private fun iniciarAudio(subtipo: SubtipoRelajacion) {
        val path = assetPathParaSubtipo(subtipo) ?: return
        val intent = Intent(context, AudioPlayerService::class.java).apply {
            action = AudioPlayerService.ACTION_PLAY
            putExtra(AudioPlayerService.EXTRA_AUDIO_FILE, path)
        }
        context.startForegroundService(intent)
    }

    private fun pausarAudio() {
        val intent = Intent(context, AudioPlayerService::class.java).apply {
            action = AudioPlayerService.ACTION_PAUSE
        }
        context.startService(intent)
    }

    private fun reanudarAudio() {
        val path = assetPathParaSubtipo(_uiState.value.subtipo) ?: return
        val intent = Intent(context, AudioPlayerService::class.java).apply {
            action = AudioPlayerService.ACTION_PLAY
            putExtra(AudioPlayerService.EXTRA_AUDIO_FILE, path)
        }
        context.startForegroundService(intent)
    }

    private fun detenerAudio() {
        val intent = Intent(context, AudioPlayerService::class.java).apply {
            action = AudioPlayerService.ACTION_STOP
        }
        context.startService(intent)
    }

    // ─────────────────────────────────────────────────────────────
    // Acciones públicas
    // ─────────────────────────────────────────────────────────────

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
                    iniciarAudio(subtipo)
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
                if (_uiState.value.subtipo.isAudio) detenerAudio()
                completarUseCase(sesion.uuid, _uiState.value.tiempoTranscurrido)
                val ciclos = _uiState.value.ciclosCompletados
                _uiState.value = _uiState.value.copy(
                    isSessionActive = false,
                    isAnimationRunning = false,
                    isAudioPlaying = false
                )
                _eventEmitter.value = RelajacionEvent.SessionCompleted(sesion.uuid, ciclos)
            } catch (e: Exception) {
                _eventEmitter.value = RelajacionEvent.Error(e.message ?: "Error al completar sesión")
            }
        }
    }

    fun interrumpirSesion() {
        viewModelScope.launch {
            val sesion = _uiState.value.sesionActual ?: return@launch
            try {
                if (_uiState.value.subtipo.isAudio) detenerAudio()
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
            if (currentState.isAudioPlaying) {
                pausarAudio()
                _uiState.value = currentState.copy(isAudioPlaying = false)
            } else {
                reanudarAudio()
                _uiState.value = currentState.copy(isAudioPlaying = true)
            }
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
        _uiState.value = _uiState.value.copy(ciclosCompletados = nuevoCiclo)
        if (nuevoCiclo >= RelajacionConfig.CICLOS_MINIMOS) {
            _eventEmitter.value = RelajacionEvent.CiclosCompletados(nuevoCiclo)
        }
    }

    fun establecerBrillo(brillo: Float) {
        _uiState.value = _uiState.value.copy(brillo = brillo)
    }

    fun activarModoNoche() {
        establecerBrillo(RelajacionConfig.BRILLO_MODO_NOCHE)
    }

    fun desactivarModoNoche() {
        establecerBrillo(RelajacionConfig.BRILLO_NORMAL)
    }

    fun limpiarEvento() {
        _eventEmitter.value = null
    }

    // ─────────────────────────────────────────────────────────────
    // Limpieza al destruir el ViewModel
    // ─────────────────────────────────────────────────────────────

    override fun onCleared() {
        super.onCleared()
        // Si hay audio reproduciéndose al salir, lo detenemos
        if (_uiState.value.isAudioPlaying) {
            detenerAudio()
        }
    }
}

sealed class RelajacionEvent {
    data class SessionCompleted(val sesionId: String, val ciclos: Int) : RelajacionEvent()
    data class CiclosCompletados(val ciclos: Int) : RelajacionEvent()
    data class Error(val message: String) : RelajacionEvent()
}