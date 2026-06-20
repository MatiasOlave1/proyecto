package com.camposocampoolavevargas.proyecto.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.camposocampoolavevargas.proyecto.data.local.entity.WeeklyGoalEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the "weekly_goals" table.
 */
@Dao
interface WeeklyGoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: WeeklyGoalEntity)

    @Query("SELECT * FROM weekly_goals WHERE userId = :userId AND isoWeek = :isoWeek AND isoYear = :isoYear LIMIT 1")
    fun getCurrentGoal(userId: String, isoWeek: Int, isoYear: Int): Flow<WeeklyGoalEntity?>

    @Query("SELECT * FROM weekly_goals WHERE userId = :userId AND isoWeek = :isoWeek AND isoYear = :isoYear LIMIT 1")
    suspend fun getCurrentGoalDirect(userId: String, isoWeek: Int, isoYear: Int): WeeklyGoalEntity?

    @Query("SELECT * FROM weekly_goals WHERE userId = :userId ORDER BY isoYear DESC, isoWeek DESC")
    fun getGoalsByUser(userId: String): Flow<List<WeeklyGoalEntity>>

    @Query("SELECT * FROM weekly_goals WHERE userId = :userId ORDER BY isoYear DESC, isoWeek DESC")
    suspend fun getGoalsByUserIdDirect(userId: String): List<WeeklyGoalEntity>
}

