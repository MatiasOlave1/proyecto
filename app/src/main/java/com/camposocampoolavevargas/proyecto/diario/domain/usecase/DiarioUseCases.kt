package com.camposocampoolavevargas.proyecto.diario.domain.usecase

import com.camposocampoolavevargas.proyecto.diario.data.repository.DiarioRepository
import com.camposocampoolavevargas.proyecto.diario.domain.model.EntradaDiario
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.util.UUID

/**
 * Contenedor que agrupa todos los casos de uso del mÃ³dulo Diario de Preocupaciones.
 * Facilita la inyecciÃ³n Ãºnica en la capa de UI/ViewModel.
 */
class DiarioUseCases @Inject constructor(
    val guardarEntrada: GuardarEntradaUseCase,
    val obtenerHistorial: ObtenerHistorialDiarioUseCase,
    val eliminarEntrada: EliminarEntradaUseCase,
    val autoEliminar: AutoEliminarEntradasUseCase
)

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// CAP-07-A: Registrar entrada de texto
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

/**
 * Caso de uso para guardar una entrada en el diario.
 *
 * Reglas de negocio:
 * - El contenido NO puede estar vacÃ­o (lanza [EntradaVaciaException]).
 * - El UUID se genera en cliente con [java.util.UUID.randomUUID].
 * - Los timestamps se registran en ISO 8601 UTC con [java.time.Instant].
 */
class GuardarEntradaUseCase(private val repository: DiarioRepository) {

    @Throws(EntradaVaciaException::class)
    suspend operator fun invoke(
        userId: String,
        contenido: String,
        autoEliminar: Boolean
    ): EntradaDiario {
        if (contenido.isBlank()) throw EntradaVaciaException()

        val ahora = Instant.now().toString()
        val nuevaEntrada = EntradaDiario(
            uuid         = UUID.randomUUID().toString(),
            userId       = userId,
            contenido    = contenido.trim(),
            fechaEntrada = ahora,
            autoEliminar = autoEliminar,
            eliminada    = false,
            creadoEn     = ahora,
            eliminadoEn  = null
        )
        repository.guardarEntrada(nuevaEntrada)
        return nuevaEntrada
    }
}

/** ExcepciÃ³n de dominio cuando el usuario intenta guardar texto vacÃ­o. */
class EntradaVaciaException : Exception("El contenido del diario no puede estar vacÃ­o.")

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// CAP-07-B: Consultar historial
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

/**
 * Caso de uso para obtener el flujo de entradas activas del diario.
 * Retorna solo entradas donde [EntradaDiario.eliminada] = false, ordenadas DESC.
 */
class ObtenerHistorialDiarioUseCase(private val repository: DiarioRepository) {
    operator fun invoke(userId: String): Flow<List<EntradaDiario>> {
        return repository.obtenerEntradasActivas(userId)
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// CAP-07-B: Eliminar entrada manualmente (Soft Delete)
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

/**
 * Caso de uso para eliminar manualmente una entrada del diario.
 * Implementa Soft Delete: [EntradaDiario.eliminada] = true, nunca DELETE fÃ­sico.
 */
class EliminarEntradaUseCase(private val repository: DiarioRepository) {
    suspend operator fun invoke(uuid: String) {
        val eliminadoEn = Instant.now().toString()
        repository.softDeleteManual(uuid, eliminadoEn)
    }
}

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// CAP-07-C: Auto-eliminaciÃ³n matutina (invocado por WorkManager)
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

/**
 * Caso de uso ejecutado por [AutoEliminarWorker] a las 06:00 hora local.
 * Marca como eliminadas todas las entradas con [EntradaDiario.autoEliminar] = true.
 *
 * @return NÃºmero de entradas marcadas como eliminadas (para decisiÃ³n de notificaciÃ³n).
 */
class AutoEliminarEntradasUseCase(private val repository: DiarioRepository) {
    suspend operator fun invoke(): Int {
        val eliminadoEn = Instant.now().toString()
        return repository.autoEliminarEntradas(eliminadoEn)
    }
}
