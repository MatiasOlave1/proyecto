package com.camposocampoolavevargas.proyecto.diario.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.camposocampoolavevargas.proyecto.data.local.UserSession
import com.camposocampoolavevargas.proyecto.diario.domain.model.EntradaDiario
import com.camposocampoolavevargas.proyecto.diario.domain.usecase.DiarioUseCases
import com.camposocampoolavevargas.proyecto.diario.domain.usecase.EntradaVaciaException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Estado de la pantalla del diario (SPEC-07).
 */
data class DiarioUiState(
    val entradas: List<EntradaDiario>    = emptyList(),
    val cargando: Boolean                = false,
    val mensajeExito: String?            = null,
    val mensajeError: String?            = null
)

/**
 * ViewModel del módulo Diario de Preocupaciones (SPEC-07).
 *
 * Responsabilidades:
 * - Expone el flujo de entradas activas [entradas] como [StateFlow] inmutable.
 * - Gestiona guardar, eliminar y refrescar entradas.
 * - Toda la lógica de negocio está delegada en [DiarioUseCases].
 * - La sesión de usuario se obtiene de [UserSession] (Offline-First).
 */
@HiltViewModel
class DiarioViewModel @Inject constructor(
    private val useCases: DiarioUseCases,
    private val userSession: UserSession
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiarioUiState())
    val uiState: StateFlow<DiarioUiState> = _uiState.asStateFlow()

    init {
        observarEntradas()
    }

    // ─────────────────────────────────────────────────────────
    // OBSERVACIÓN REACTIVA
    // ─────────────────────────────────────────────────────────

    private fun observarEntradas() {
        val userId = userSession.getActiveUserId() ?: return
        useCases.obtenerHistorial(userId)
            .onEach { lista ->
                _uiState.value = _uiState.value.copy(
                    entradas = lista,
                    cargando = false
                )
            }
            .catch { e ->
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    mensajeError = "Error al cargar el diario: ${e.message}"
                )
            }
            .launchIn(viewModelScope)
    }

    // ─────────────────────────────────────────────────────────
    // GUARDAR ENTRADA
    // ─────────────────────────────────────────────────────────

    /**
     * Guarda una nueva entrada en el diario.
     *
     * @param contenido Texto de la entrada.
     * @param autoEliminar Si true, la entrada se marcará para auto-eliminación matutina.
     */
    fun guardarEntrada(contenido: String, autoEliminar: Boolean) {
        val userId = userSession.getActiveUserId() ?: run {
            _uiState.value = _uiState.value.copy(
                mensajeError = "No hay sesión activa"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)
            try {
                useCases.guardarEntrada(userId, contenido, autoEliminar)
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    mensajeExito = "Tu entrada fue guardada de forma segura"
                )
            } catch (e: EntradaVaciaException) {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    mensajeError = "Escribe algo antes de guardar"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    mensajeError = "Error al guardar: ${e.message}"
                )
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // ELIMINAR ENTRADA (Soft Delete manual)
    // ─────────────────────────────────────────────────────────

    /**
     * Elimina una entrada por su UUID usando Soft Delete.
     * Nunca se realiza DELETE físico de la base de datos.
     */
    fun eliminarEntrada(uuid: String) {
        viewModelScope.launch {
            try {
                useCases.eliminarEntrada(uuid)
                _uiState.value = _uiState.value.copy(
                    mensajeExito = "Entrada eliminada"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    mensajeError = "Error al eliminar: ${e.message}"
                )
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // LIMPIAR MENSAJES (evitar re-renderizados)
    // ─────────────────────────────────────────────────────────

    fun limpiarMensajeExito()  { _uiState.value = _uiState.value.copy(mensajeExito = null) }
    fun limpiarMensajeError()  { _uiState.value = _uiState.value.copy(mensajeError = null) }
}
