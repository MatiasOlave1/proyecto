package com.camposocampoolavevargas.proyecto.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.camposocampoolavevargas.proyecto.data.local.entity.CircadianAlertEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the "circadian_alerts" table.
 */
@Dao
interface CircadianAlertDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: CircadianAlertEntity)

    @Query("UPDATE circadian_alerts SET dismissed = 1 WHERE alertId = :alertId")
    suspend fun dismissAlert(alertId: String)

    @Query("SELECT * FROM circadian_alerts WHERE userId = :userId AND dismissed = 0 ORDER BY generatedAt DESC")
    fun getActiveAlerts(userId: String): Flow<List<CircadianAlertEntity>>

    @Query("SELECT * FROM circadian_alerts WHERE userId = :userId ORDER BY generatedAt DESC LIMIT :limit")
    suspend fun getRecentAlerts(userId: String, limit: Int): List<CircadianAlertEntity>
}

