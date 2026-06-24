package com.dormibienu.app.diario.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dormibienu.app.diario.data.local.entity.EntradaDiarioEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para realizar operaciones de persistencia local
 * sobre la tabla de entradas del diario de preocupaciones, con soporte para soft delete.
 */
@Dao
interface EntradaDiarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntrada(entrada: EntradaDiarioEntity)

    @Update
    suspend fun updateEntrada(entrada: EntradaDiarioEntity)

    @Query("SELECT * FROM entradas_diario WHERE uuid = :uuid LIMIT 1")
    suspend fun getEntradaByUuid(uuid: String): EntradaDiarioEntity?

    @Query("SELECT * FROM entradas_diario WHERE user_id = :userId AND eliminada = 0 ORDER BY fecha_entrada DESC")
    fun getEntradasByUserId(userId: String): Flow<List<EntradaDiarioEntity>>

    @Query("UPDATE entradas_diario SET eliminada = 1, eliminado_en = :eliminadoEn WHERE uuid = :uuid")
    suspend fun softDeleteManual(uuid: String, eliminadoEn: String)

    @Query("UPDATE entradas_diario SET eliminada = 1, eliminado_en = :eliminadoEn WHERE auto_eliminar = 1 AND eliminada = 0")
    suspend fun autoEliminarEntradas(eliminadoEn: String): Int
}
