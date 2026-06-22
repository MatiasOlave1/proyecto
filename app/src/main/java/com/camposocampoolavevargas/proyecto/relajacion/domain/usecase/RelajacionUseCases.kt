package com.camposocampoolavevargas.proyecto.relajacion.domain.usecase

import com.camposocampoolavevargas.proyecto.relajacion.data.repository.RelajacionRepository
import com.camposocampoolavevargas.proyecto.relajacion.domain.model.SesionRelajacion
import com.camposocampoolavevargas.proyecto.relajacion.domain.model.SubtipoRelajacion
import com.camposocampoolavevargas.proyecto.relajacion.domain.model.TipoRelajacion
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

class IniciarSesionRelajacionUseCase @Inject constructor(private val repository: RelajacionRepository) {

    suspend operator fun invoke(
        userId: String,
        subtipo: SubtipoRelajacion,
        audioActivo: Boolean = false
    ): SesionRelajacion {
        val sesion = SesionRelajacion(
            uuid = UUID.randomUUID().toString(),
            userId = userId,
            tipo = if (subtipo.isAudio) TipoRelajacion.AUDIO else TipoRelajacion.RESPIRACION,
            subtipo = subtipo.name,
            duracionSegundos = 0,
            completada = false,
            audioActivo = audioActivo,
            iniciadoEn = Instant.now()
        )
        repository.crearSesion(sesion)
        return sesion
    }
}

class CompletarSesionRelajacionUseCase @Inject constructor(private val repository: RelajacionRepository) {

    suspend operator fun invoke(
        uuid: String,
        duracionSegundos: Int
    ) {
        val sesion = repository.obtenerSesion(uuid) ?: return
        val sesionActualizada = sesion.copy(
            duracionSegundos = duracionSegundos,
            completada = true,
            finalizadoEn = Instant.now()
        )
        repository.actualizarSesion(sesionActualizada)
    }
}

class ObtenerHistorialSesionesUseCase @Inject constructor(private val repository: RelajacionRepository) {
    operator fun invoke(userId: String): Flow<List<SesionRelajacion>> {
        return repository.obtenerSesionesPorUsuario(userId)
    }
}

class InterrumpirSesionRelajacionUseCase @Inject constructor(private val repository: RelajacionRepository) {

    suspend operator fun invoke(
        uuid: String,
        duracionSegundos: Int
    ) {
        val sesion = repository.obtenerSesion(uuid) ?: return
        val sesionActualizada = sesion.copy(
            duracionSegundos = duracionSegundos,
            completada = false,
            finalizadoEn = Instant.now()
        )
        repository.actualizarSesion(sesionActualizada)
    }
}