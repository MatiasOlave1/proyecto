package com.camposocampoolavevargas.proyecto.relajacion.data.repository

import com.camposocampoolavevargas.proyecto.relajacion.data.local.dao.SesionRelajacionDao
import com.camposocampoolavevargas.proyecto.relajacion.data.local.entity.SesionRelajacionEntity
import com.camposocampoolavevargas.proyecto.relajacion.domain.model.SesionRelajacion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

import javax.inject.Inject

class RelajacionRepository @Inject constructor(private val dao: SesionRelajacionDao) {
    
    suspend fun crearSesion(sesion: SesionRelajacion) {
        val entity = sesion.toEntity()
        dao.insertar(entity)
    }
    
    suspend fun actualizarSesion(sesion: SesionRelajacion) {
        val entity = sesion.toEntity()
        dao.actualizar(entity)
    }
    
    suspend fun obtenerSesion(uuid: String): SesionRelajacion? {
        return dao.obtenerPorUuid(uuid)?.toDomain()
    }
    
    fun obtenerSesionesPorUsuario(userId: String): Flow<List<SesionRelajacion>> {
        return dao.obtenerSesionesPorUsuario(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    fun obtenerSesionesCompletadas(userId: String): Flow<List<SesionRelajacion>> {
        return dao.obtenerSesionesCompletadas(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    fun obtenerUltimas(userId: String, tipo: String, limit: Int = 10): Flow<List<SesionRelajacion>> {
        return dao.obtenerUltimas(userId, tipo, limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    suspend fun contarSesiones(userId: String, tipo: String): Int {
        return dao.contar(userId, tipo)
    }
    
    suspend fun borrarSesionesPorUsuario(userId: String) {
        dao.borrarPorUsuario(userId)
    }
    
    private fun SesionRelajacion.toEntity(): SesionRelajacionEntity {
        return SesionRelajacionEntity(
            uuid = uuid,
            userId = userId,
            tipo = tipo.name,
            subtipo = subtipo,
            duracionSegundos = duracionSegundos,
            completada = completada,
            audioActivo = audioActivo,
            iniciadoEn = iniciadoEn.toString(),
            finalizadoEn = finalizadoEn?.toString()
        )
    }
    
    private fun SesionRelajacionEntity.toDomain(): SesionRelajacion {
        return SesionRelajacion(
            uuid = uuid,
            userId = userId,
            tipo = enumValueOf(tipo),
            subtipo = subtipo,
            duracionSegundos = duracionSegundos,
            completada = completada,
            audioActivo = audioActivo,
            iniciadoEn = Instant.parse(iniciadoEn),
            finalizadoEn = finalizadoEn?.let { Instant.parse(it) }
        )
    }
}
