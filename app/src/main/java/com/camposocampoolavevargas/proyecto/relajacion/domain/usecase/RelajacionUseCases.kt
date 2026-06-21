package com.dormibienu.app.relajacion.domain.usecase

import com.dormibienu.app.relajacion.domain.model.SesionRelajacion
import com.dormibienu.app.relajacion.domain.model.TipoSesion
import com.dormibienu.app.relajacion.data.repository.RelajacionRepository
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.util.UUID

/**
 * Interfaz para la emisión de eventos internos del sistema (Decoupled Event Bus).
 * Permite que CAP-06 notifique a CAP-04 (Gamificación) sin acoplamiento directo.
 */
interface RelajacionEventBus {
    suspend fun emitirEvento(evento: String)
    
    companion object {
        const val EVENTO_SESION_COMPLETADA = "SESION_RELAJACION_COMPLETADA"
    }
}

/**
 * Contenedor que agrupa todos los casos de uso del módulo de relajación analógica
 * para facilitar su inyección en la capa de UI.
 */
data class RelajacionUseCases(
    val iniciarSesion: IniciarSesionUseCase,
    val finalizarSesion: FinalizarSesionUseCase,
    val obtenerHistorialSesiones: ObtenerHistorialSesionesUseCase
)

/**
 * Caso de Uso para registrar el inicio de una sesión de relajación (Respiración o Audio).
 * Genera el UUID v4 en el cliente y captura el timestamp exacto en ISO 8601 UTC.
 */
class IniciarSesionUseCase(private val repository: RelajacionRepository) {
    suspend operator fun invoke(
        userId: String,
        tipo: TipoSesion,
        subtipo: String,
        audioActivo: Boolean
    ): SesionRelajacion {
        val nuevaSesion = SesionRelajacion(
            uuid = UUID.randomUUID().toString(),
            userId = userId,
            tipo = tipo,
            subtipo = subtipo,
            duracionSegundos = 0,
            completada = false,
            audioActivo = audioActivo,
            iniciadoEn = Instant.now().toString(), // ISO 8601 UTC Estricto
            finalizadoEn = null
        )
        repository.registrarSesion(nuevaSesion)
        return nuevaSesion
    }
}

/**
 * Caso de Uso para finalizar una sesión de relajación.
 * Determina si la sesión cumple las condiciones de completitud (ej. >= 3 ciclos completos)
 * y dispara el evento interno "SESION_RELAJACION_COMPLETADA" si corresponde.
 */
class FinalizarSesionUseCase(
    private val repository: RelajacionRepository,
    private val eventBus: RelajacionEventBus
) {
    suspend operator fun invoke(
        uuid: String,
        duracionSegundos: Int,
        completadaPorCiclos: Boolean
    ): SesionRelajacion? {
        val sesionActual = repository.obtenerSesionPorUuid(uuid) ?: return null
        
        val sesionFinalizada = sesionActual.copy(
            duracionSegundos = duracionSegundos,
            completada = completadaPorCiclos,
            finalizadoEn = Instant.now().toString() // ISO 8601 UTC Estricto
        )
        
        repository.actualizarSesion(sesionFinalizada)
        
        // Criterio de Aceptación: Emitir evento si la sesión fue exitosa y completada
        if (completadaPorCiclos) {
            eventBus.emitirEvento(RelajacionEventBus.EVENTO_SESION_COMPLETADA)
        }
        
        return sesionFinalizada
    }
}

/**
 * Caso de Uso para obtener el flujo de sesiones históricas del usuario localmente.
 */
class ObtenerHistorialSesionesUseCase(private val repository: RelajacionRepository) {
    operator fun invoke(userId: String): Flow<List<SesionRelajacion>> {
        return repository.obtenerSesionesPorUsuario(userId)
    }
}