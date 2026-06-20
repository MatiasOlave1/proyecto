package com.dormibienu.app.relajacion.data.repository

import com.dormibienu.app.relajacion.data.local.dao.SesionRelajacionDao
import com.dormibienu.app.relajacion.data.local.entity.toDomain
import com.dormibienu.app.relajacion.data.local.entity.toEntity
import com.dormibienu.app.relajacion.domain.model.SesionRelajacion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Interfaz del Repositorio que define las operaciones de negocio admitidas
 * para el módulo de relajación analógica.
 */
interface RelajacionRepository {
    suspend fun registrarSesion(sesion: SesionRelajacion)
    suspend fun actualizarSesion(sesion: SesionRelajacion)
    suspend fun obtenerSesionPorUuid(uuid: String): SesionRelajacion?
    fun obtenerSesionesPorUsuario(userId: String): Flow<List<SesionRelajacion>>
    fun obtenerContadorSesionesCompletadas(userId: String): Flow<Int>
}

/**
 * Implementación del repositorio enfocada en una estrategia Offline-First rigurosa,
 * utilizando exclusivamente el DAO de Room y garantizando la ejecución en el hilo de I/O.
 */
class RelajacionRepositoryImpl(
    private val dao: SesionRelajacionDao
) : RelajacionRepository {

    override suspend fun registrarSesion(sesion: SesionRelajacion) = withContext(Dispatchers.IO) {
        dao.insertSesion(sesion.toEntity())
    }

    override suspend fun actualizarSesion(sesion: SesionRelajacion) = withContext(Dispatchers.IO) {
        dao.updateSesion(sesion.toEntity())
    }

    override suspend fun obtenerSesionPorUuid(uuid: String): SesionRelajacion? = withContext(Dispatchers.IO) {
        dao.getSesionByUuid(uuid)?.toDomain()
    }

    override fun obtenerSesionesPorUsuario(userId: String): Flow<List<SesionRelajacion>> {
        return dao.getSesionesByUserId(userId).map { listaEntidades ->
            listaEntidades.map { entidad -> entidad.toDomain() }
        }
    }

    override fun obtenerContadorSesionesCompletadas(userId: String): Flow<Int> {
        return dao.getContadorSesionesCompletadas(userId)
    }
}