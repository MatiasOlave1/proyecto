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
    private val alarmScheduler: DisconnectAlarmScheduler,
    private val settingsRepository: DisconnectSettingsRepository
) : WeeklyGoalRepository {

    override suspend fun saveGoal(goal: WeeklyGoalEntity) {
        weeklyGoalDao.insertGoal(goal)
        
        // If the mode is manual, we don't automatically reschedule here 
        // as the manual configuration screen handles its own scheduling.
        // But if it's WEEKLY_STREAK, we should update the alarm based on the new goal.
        if (settingsRepository.getDisconnectMode() == DisconnectMode.WEEKLY_STREAK) {
            val offset = settingsRepository.getReminderOffsetMinutes()
            alarmScheduler.scheduleReminder(goal.bedtimeLimitMillis, offset)
        }
    }

    override fun getCurrentGoal(userId: String, isoWeek: Int, isoYear: Int): Flow<WeeklyGoalEntity?> {
        return weeklyGoalDao.getCurrentGoal(userId, isoWeek, isoYear)
    }
}
