package com.camposocampoolavevargas.proyecto.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.camposocampoolavevargas.proyecto.data.local.entity.AchievementEntity
import com.camposocampoolavevargas.proyecto.data.local.model.AchievementType
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the "achievements" table.
 */
@Dao
interface AchievementDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievement(achievement: AchievementEntity)

    @Query("UPDATE achievements SET unlocked = 1, unlockedAt = :unlockedAt, points = :points WHERE userId = :userId AND type = :type")
    suspend fun unlockAchievement(userId: String, type: AchievementType, unlockedAt: Long, points: Int)

    @Query("SELECT * FROM achievements WHERE userId = :userId")
    fun getAchievementsByUser(userId: String): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements WHERE userId = :userId AND unlocked = 1")
    fun getUnlockedAchievements(userId: String): Flow<List<AchievementEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM achievements WHERE userId = :userId AND type = :type AND unlocked = 1)")
    suspend fun isAchievementUnlocked(userId: String, type: AchievementType): Boolean
}

