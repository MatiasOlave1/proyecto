package com.camposocampoolavevargas.proyecto.relajacion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.camposocampoolavevargas.proyecto.relajacion.data.local.entity.SesionRelajacionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SesionRelajacionDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(sesion: SesionRelajacionEntity): Long
    
    @Update
    suspend fun actualizar(sesion: SesionRelajacionEntity)
    
    @Query("SELECT * FROM sesion_relajacion WHERE uuid = :uuid")
    suspend fun obtenerPorUuid(uuid: String): SesionRelajacionEntity?
    
    @Query("SELECT * FROM sesion_relajacion WHERE userId = :userId ORDER BY iniciadoEn DESC")
    fun obtenerSesionesPorUsuario(userId: String): Flow<List<SesionRelajacionEntity>>
    
    @Query("SELECT * FROM sesion_relajacion WHERE userId = :userId AND completada = 1 ORDER BY iniciadoEn DESC")
    fun obtenerSesionesCompletadas(userId: String): Flow<List<SesionRelajacionEntity>>
    
    @Query("""
        SELECT * FROM sesion_relajacion 
        WHERE userId = :userId 
        AND tipo = :tipo 
        ORDER BY iniciadoEn DESC 
        LIMIT :limit
    """)
    fun obtenerUltimas(userId: String, tipo: String, limit: Int = 10): Flow<List<SesionRelajacionEntity>>
    
    @Query("""
        SELECT COUNT(*) FROM sesion_relajacion 
        WHERE userId = :userId AND completada = 1 AND tipo = :tipo
    """)
    suspend fun contar(userId: String, tipo: String): Int
    
    @Query("DELETE FROM sesion_relajacion WHERE userId = :userId")
    suspend fun borrarPorUsuario(userId: String)
}
