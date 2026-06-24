package com.camposocampoolavevargas.proyecto.diario.data.repository

import com.camposocampoolavevargas.proyecto.diario.data.local.dao.EntradaDiarioDao
import com.camposocampoolavevargas.proyecto.diario.data.local.entity.toDomain
import com.camposocampoolavevargas.proyecto.diario.data.local.entity.toEntity
import com.camposocampoolavevargas.proyecto.diario.domain.model.EntradaDiario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Interfaz del Repositorio que define las operaciones de negocio
 * para el mÃ³dulo Diario de Preocupaciones (SPEC-07).
 */
interface DiarioRepository {
    suspend fun guardarEntrada(entrada: EntradaDiario)
    fun obtenerEntradasActivas(userId: String): Flow<List<EntradaDiario>>
    suspend fun softDeleteManual(uuid: String, eliminadoEn: String)
    suspend fun autoEliminarEntradas(eliminadoEn: String): Int
}

/**
 * ImplementaciÃ³n Offline-First del repositorio del diario.
 * Toda operaciÃ³n de I/O se ejecuta en Dispatchers.IO.
 */
class DiarioRepositoryImpl(
    private val dao: EntradaDiarioDao
) : DiarioRepository {

    override suspend fun guardarEntrada(entrada: EntradaDiario) = withContext(Dispatchers.IO) {
        dao.insertEntrada(entrada.toEntity())
    }

    override fun obtenerEntradasActivas(userId: String): Flow<List<EntradaDiario>> {
        return dao.getEntradasByUserId(userId).map { lista ->
            lista.map { entity -> entity.toDomain() }
        }
    }

    override suspend fun softDeleteManual(uuid: String, eliminadoEn: String) =
        withContext(Dispatchers.IO) {
            dao.softDeleteManual(uuid, eliminadoEn)
        }

    override suspend fun autoEliminarEntradas(eliminadoEn: String): Int =
        withContext(Dispatchers.IO) {
            dao.autoEliminarEntradas(eliminadoEn)
        }
}
