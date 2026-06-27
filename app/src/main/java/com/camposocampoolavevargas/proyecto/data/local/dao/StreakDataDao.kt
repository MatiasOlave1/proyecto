package com.camposocampoolavevargas.proyecto.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.camposocampoolavevargas.proyecto.data.local.entity.StreakDataEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the "streak_data" table.
 */
@Dao
interface StreakDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStreak(streak: StreakDataEntity)

    @Query("SELECT * FROM streak_data WHERE userId = :userId LIMIT 1")
    fun getStreakByUser(userId: String): Flow<StreakDataEntity?>

    @Query("SELECT * FROM streak_data WHERE userId = :userId LIMIT 1")
    suspend fun getStreakByUserDirect(userId: String): StreakDataEntity?
}

