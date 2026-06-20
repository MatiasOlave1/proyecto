package com.dormibienu.app.relajacion.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dormibienu.app.relajacion.config.RelajacionConfig
import com.dormibienu.app.relajacion.domain.model.SesionRelajacion
import com.dormibienu.app.relajacion.domain.model.TipoSesion
import com.dormibienu.app.relajacion.domain.usecase.RelajacionUseCases
import com.dormibienu.app.relajacion.util.RelajacionUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estados posibles para cada una de las fases de la animación interactiva 4-7-8.
 */
enum class FaseRespiracion {
    INHALAR,
    RETENER,
    EXHALAR,
    INACTIVO
}

/**
 * Representa el estado actual de la interfaz de usuario del módulo SPEC-06.
 */
data class RelajacionUiState(
    val userId: String = "",
    val esVentanaNocturnaActiva: Boolean = false,
    val faseActual: FaseRespiracion = FaseRespiracion.INACTIVO,
    val tiempoRestanteFaseMs: Long = 0L,
    val ciclosCompletados: Int = 0,
    val audioSeleccionado: String? = null,
    val estaReproduciendoAudio: Boolean = false,
    val sesionActivaUuid: String? = null,
    val timestampInterrupcion: String? = null
)

class RelajacionViewModel(
    private val useCases: RelajacionUseCases,
    private val userId: String,
    private val esVentanaNocturnaInicial: Boolean
) : ViewModel() {

    private val _uiState = MutableStateFlow(RelajacionUiState(userId = userId, esVentanaNocturnaActiva = esVentanaNocturnaInicial))
    val uiState: StateFlow<RelajacionUiState> = _uiState.asStateFlow()

    private var cronometroJob: Job? = null
    private var segundosTranscurridosTotales = 0
    private var timestampInicioSesion: String? = null

    /**
     * CAP-06-A: Inicia el ciclo rítmico de respiración 4-7-8.
     */
    fun iniciarRespiracion() {
        if (_uiState.value.faseActual != FaseRespiracion.INACTIVO) return

        viewModelScope.launch {
            // Registrar inicio de sesión en persistencia local (Offline-First)
            val nuevaSesion = useCases.iniciarSesion(
                userId = _uiState.value.userId,
                tipo = TipoSesion.RESPIRACION,
                subtipo = SesionRelajacion.SUBTIPO_RESPIRACION_478,
                audioActivo = _uiState.value.estaReproduciendoAudio
            )
            
            timestampInicioSesion = nuevaSesion.iniciadoEn
            segundosTranscurridosTotales = 0

            _uiState.update { 
                it.copy(
                    sesionActivaUuid = nuevaSesion.uuid,
                    ciclosCompletados = 0,
                    timestampInterrupcion = null
                ) 
            }
            ejecutarCicloRespiracion()
        }
    }

    private fun ejecutarCicloRespiracion() {
        cronometroJob?.cancel()
        cronometroJob = viewModelScope.launch {
            while (true) {
                // FASE 1: INHALAR (4 segundos)
                ejecutarFase(FaseRespiracion.INHALAR, RelajacionConfig.Respiracion478.TIEMPO_INHALAR_MS)
                
                // FASE 2: RETENER (7 segundos)
                ejecutarFase(FaseRespiracion.RETENER, RelajacionConfig.Respiracion478.TIEMPO_RETENER_MS)
                
                // FASE 3: EXHALAR (8 segundos)
                ejecutarFase(FaseRespiracion.EXHALAR, RelajacionConfig.Respiracion478.TIEMPO_EXHALAR_MS)

                // Fin del ciclo exacto de 19 segundos. Actualizar contador.
                _uiState.update { it.copy(ciclosCompletados = it.ciclosCompletados + 1) }
            }
        }
    }

    private suspend fun ejecutarFase(fase: FaseRespiracion, duracionMs: Long) {
        _uiState.update { it.copy(faseActual = fase, tiempoRestanteFaseMs = duracionMs) }
        var tiempoRestante = duracionMs
        val intervaloTick = 100L // Ticks frecuentes para suavidad de UI sin sobrecargar el hilo

        while (tiempoRestante > 0) {
            delay(intervaloTick)
            tiempoRestante -= intervaloTick
            segundosTranscurridosTotales = RelajacionUtils.calcularDuracionHastaAhora(timestampInicioSesion)
            _uiState.update { it.copy(tiempoRestanteFaseMs = tiempoRestante.coerceAtLeast(0L)) }
        }
    }

    /**
     * GIVEN la app pasa a background (onPause)
     */
    fun pausarPorBackground() {
        if (_uiState.value.faseActual == FaseRespiracion.INACTIVO) return
        
        cronometroJob?.cancel()
        _uiState.update {
            it.copy(
                faseActual = FaseRespiracion.INACTIVO,
                timestampInterrupcion = RelajacionUtils.obtenerTimestampActual()
            )
        }
    }

    /**
     * GIVEN al volver a foreground (onResume) la animación retoma desde el inicio del ciclo actual
     */
    fun reanudarDesdeForeground() {
        if (_uiState.value.timestampInterrupcion != null && _uiState.value.sesionActivaUuid != null) {
            ejecutarCicloRespiracion()
        }
    }

    /**
     * Detiene la sesión de respiración y evalúa criterios de completitud (>= 3 ciclos).
     */
    fun detenerRespiracion() {
        cronometroJob?.cancel()
        val uuidSesion = _uiState.value.sesionActivaUuid
        val ciclos = _uiState.value.ciclosCompletados
        // Criterio de Aceptación: completada=true si completó >= 3 ciclos completos
        val completada = ciclos >= RelajacionConfig.Respiracion478.MIN_CICLOS_COMPLETADOS

        if (uuidSesion != null) {
            viewModelScope.launch {
                useCases.finalizarSesion(
                    uuid = uuidSesion,
                    duracionSegundos = segundosTranscurridosTotales,
                    completadaPorCiclos = completada
                )
            }
        }

        _uiState.update {
            it.copy(
                faseActual = FaseRespiracion.INACTIVO,
                tiempoRestanteFaseMs = 0L,
                sesionActivaUuid = null,
                timestampInterrupcion = null
            )
        }
    }

    /**
     * CAP-06-B: Vinculación de estados con las acciones del Foreground Service.
     */
    fun actualizarEstadoAudio(estaReproduciendo: Boolean, subtipo: String?) {
        _uiState.update {
            it.copy(
                estaReproduciendoAudio = estaReproduciendo,
                audioSeleccionado = subtipo
            )
        }
    }

    /**
     * CAP-06-C: Desconexión nocturna activa el banner y reduce brillo temporalmente.
     */
    fun setVentanaNocturnaActiva(activa: Boolean) {
        _uiState.update { it.copy(esVentanaNocturnaActiva = activa) }
    }
}