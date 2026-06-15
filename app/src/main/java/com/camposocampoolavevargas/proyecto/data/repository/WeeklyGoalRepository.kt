package com.camposocampoolavevargas.proyecto.data.repository

import com.camposocampoolavevargas.proyecto.data.local.dao.WeeklyGoalDao
import com.camposocampoolavevargas.proyecto.data.local.entity.WeeklyGoalEntity
import com.camposocampoolavevargas.proyecto.service.notification.DisconnectAlarmScheduler
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing weekly goals.
 * Coordinates between local database and background services like notifications.
 */
interface WeeklyGoalRepository {
    suspend fun saveGoal(goal: WeeklyGoalEntity)
    fun getCurrentGoal(userId: String, isoWeek: Int, isoYear: Int): Flow<WeeklyGoalEntity?>
}

@Singleton
class WeeklyGoalRepositoryImpl @Inject constructor(
    private val weeklyGoalDao: WeeklyGoalDao,
    private val alarmScheduler: DisconnectAlarmScheduler
) : WeeklyGoalRepository {

    override suspend fun saveGoal(goal: WeeklyGoalEntity) {
        weeklyGoalDao.insertGoal(goal)
        // Whenever a goal is saved, we schedule the disconnect reminder automatically.
        alarmScheduler.scheduleReminder(goal.bedtimeLimitMillis)
    }

    override fun getCurrentGoal(userId: String, isoWeek: Int, isoYear: Int): Flow<WeeklyGoalEntity?> {
        return weeklyGoalDao.getCurrentGoal(userId, isoWeek, isoYear)
    }
}
