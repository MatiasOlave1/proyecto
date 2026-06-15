package com.dormibienu.app.relajacion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dormibienu.app.relajacion.data.local.entity.SesionRelajacionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para realizar operaciones de persistencia local
 * sobre la tabla de sesiones de relajación.
 */
@Dao
interface SesionRelajacionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSesion(sesion: SesionRelajacionEntity)

    @Update
    suspend fun updateSesion(sesion: SesionRelajacionEntity)

    @Query("SELECT * FROM sesiones_relajacion WHERE uuid = :uuid LIMIT 1")
    suspend fun getSesionByUuid(uuid: String): SesionRelajacionEntity?

    @Query("SELECT * FROM sesiones_relajacion WHERE user_id = :userId ORDER BY iniciado_en DESC")
    fun getSesionesByUserId(userId: String): Flow<List<SesionRelajacionEntity>>

    @Query("SELECT COUNT(*) FROM sesiones_relajacion WHERE user_id = :userId AND completada = 1")
    fun getContadorSesionesCompletadas(userId: String): Flow<Int>
}