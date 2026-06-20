package com.camposocampoolavevargas.proyecto.ui.screens

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.camposocampoolavevargas.proyecto.data.local.UserSession
import com.camposocampoolavevargas.proyecto.data.local.dao.SleepRecordDao
import com.camposocampoolavevargas.proyecto.data.local.dao.StreakDataDao
import com.camposocampoolavevargas.proyecto.data.local.dao.AchievementDao
import com.camposocampoolavevargas.proyecto.data.local.dao.CircadianAlertDao
import com.camposocampoolavevargas.proyecto.data.local.entity.SleepRecordEntity
import com.camposocampoolavevargas.proyecto.data.local.entity.StreakDataEntity
import com.camposocampoolavevargas.proyecto.data.local.entity.AchievementEntity
import com.camposocampoolavevargas.proyecto.data.local.entity.CircadianAlertEntity
import com.camposocampoolavevargas.proyecto.data.local.model.SleepQuality
import com.camposocampoolavevargas.proyecto.data.local.model.SyncStatus
import com.camposocampoolavevargas.proyecto.data.local.model.AchievementType
import com.camposocampoolavevargas.proyecto.ui.BaseViewModel
import com.camposocampoolavevargas.proyecto.util.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID
import com.camposocampoolavevargas.proyecto.data.repository.SyncRepository
import javax.inject.Inject

/**
 * ViewModel for the Sleep Log Screen (RF02 — Registro diario de sueño).
 * Manages the daily sleep input form state and persists entries in Room DB.
 */
@HiltViewModel
class SleepLogViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sleepRecordDao: SleepRecordDao,
    private val streakDataDao: StreakDataDao,
    private val achievementDao: AchievementDao,
    private val circadianAlertDao: CircadianAlertDao,
    private val userSession: UserSession,
    private val syncRepository: SyncRepository
) : BaseViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now().minusDays(1))
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    private val _sleepTime = MutableStateFlow(LocalTime.of(22, 0))
    val sleepTime: StateFlow<LocalTime> = _sleepTime

    private val _wakeTime = MutableStateFlow(LocalTime.of(6, 0))
    val wakeTime: StateFlow<LocalTime> = _wakeTime

    private val _quality = MutableStateFlow(SleepQuality.GOOD)
    val quality: StateFlow<SleepQuality> = _quality

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isSaveSuccessful = MutableStateFlow(false)
    val isSaveSuccessful: StateFlow<Boolean> = _isSaveSuccessful

    fun updateDate(date: LocalDate) {
        _selectedDate.value = date
        _errorMessage.value = null
    }

    fun updateSleepTime(time: LocalTime) {
        _sleepTime.value = time
        _errorMessage.value = null
    }

    fun updateWakeTime(time: LocalTime) {
        _wakeTime.value = time
        _errorMessage.value = null
    }

    fun updateQuality(quality: SleepQuality) {
        _quality.value = quality
    }

    /**
     * Validates input, calculates sleep duration (handling midnight crossing), 
     * and saves the sleep record to Room Database.
     */
    fun saveRecord() {
        val userId = userSession.getActiveUserId()
        if (userId == null) {
            _errorMessage.value = "Error: Sesión de usuario no válida"
            return
        }

        val dateVal = _selectedDate.value
        val sleepTimeVal = _sleepTime.value
        val wakeTimeVal = _wakeTime.value

        // Resolve sleep datetime vs wake datetime
        // E.g., Went to sleep at 23:00, woke up at 07:00. 
        // Chronologically, the bedtime belongs to the previous day if sleep time is after wake time in a 24h cycle.
        val sleepDateTime = if (sleepTimeVal.isAfter(wakeTimeVal)) {
            LocalDateTime.of(dateVal.minusDays(1), sleepTimeVal)
        } else {
            LocalDateTime.of(dateVal, sleepTimeVal)
        }
        val wakeDateTime = LocalDateTime.of(dateVal, wakeTimeVal)

        val zoneId = ZoneId.systemDefault()
        val sleepMillis = sleepDateTime.atZone(zoneId).toInstant().toEpochMilli()
        val wakeMillis = wakeDateTime.atZone(zoneId).toInstant().toEpochMilli()

        if (sleepMillis >= wakeMillis) {
            _errorMessage.value = "La hora de despertarse debe ser posterior a la hora de acostarse"
            return
        }

        val durationMinutes = ((wakeMillis - sleepMillis) / (1000 * 60)).toInt()
        if (durationMinutes > 24 * 60) {
            _errorMessage.value = "La duración de sueño no puede exceder las 24 horas"
            return
        }

        viewModelScope.launch {
            try {
                // Check if a sleep record already exists for this date
                val existingRecord = sleepRecordDao.getRecordByDateDirect(userId, dateVal.toString())
                if (existingRecord != null) {
                    _errorMessage.value = "Ya has registrado tu descanso para este día"
                    return@launch
                }

                val record = SleepRecordEntity(
                    recordId = UUID.randomUUID().toString(),
                    userId = userId,
                    sleepTime = sleepMillis,
                    wakeTime = wakeMillis,
                    quality = _quality.value,
                    date = dateVal.toString(), // YYYY-MM-DD
                    syncStatus = SyncStatus.PENDING
                )
                syncRepository.saveSleepRecord(record)
                updateStreakAfterLog(userId, dateVal)
                
                // Evaluate circadian alerts (Social Jet Lag)
                evaluateCircadianAlerts(userId)
                
                // Evaluate and unlock achievements asynchronously
                evaluateAchievementsAfterLog(userId)
                
                _isSaveSuccessful.value = true
            } catch (e: Exception) {
                _errorMessage.value = "Error al guardar: ${e.localizedMessage ?: "Desconocido"}"
            }
        }
    }

    /**
     * Rules engine to evaluate and unlock achievements after a sleep log and streak update.
     */
    private suspend fun evaluateAchievementsAfterLog(userId: String) {
        try {
            val totalRecordsCount = sleepRecordDao.getRecordsByUserIdDirect(userId).size
            val currentStreakData = streakDataDao.getStreakByUserDirect(userId)
            val currentStreak = currentStreakData?.currentStreak ?: 0

            suspend fun checkAndUnlock(type: AchievementType, condition: Boolean, title: String, desc: String) {
                if (condition) {
                    val isAlreadyUnlocked = achievementDao.isAchievementUnlocked(userId, type)
                    if (!isAlreadyUnlocked) {
                        val points = when (type) {
                            AchievementType.FIRST_RECORD -> 50
                            AchievementType.DISCIPLINE_5_DAYS -> 100
                            AchievementType.PERFECT_WEEK -> 150
                            AchievementType.STREAK_10 -> 200
                            AchievementType.STREAK_30 -> 500
                            AchievementType.EASTER_EGG -> 0
                        }
                        // Insert/update achievement in database
                        val achievement = AchievementEntity(
                            userId = userId,
                            type = type,
                            unlocked = true,
                            unlockedAt = System.currentTimeMillis(),
                            points = points
                        )
                        syncRepository.saveAchievement(achievement)

                        // Trigger push notification
                        NotificationHelper.showAchievementNotification(
                            context = context,
                            title = "¡Logro Desbloqueado! 🏆",
                            message = "$title: $desc"
                        )
                    }
                }
            }

            // Evaluate specific achievements
            checkAndUnlock(
                type = AchievementType.FIRST_RECORD,
                condition = totalRecordsCount >= 1,
                title = "Primer Paso",
                desc = "Has registrado tu primer descanso."
            )
            checkAndUnlock(
                type = AchievementType.DISCIPLINE_5_DAYS,
                condition = currentStreak >= 5,
                title = "Disciplina",
                desc = "Mantuviste tu racha de sueño por 5 días."
            )
            checkAndUnlock(
                type = AchievementType.PERFECT_WEEK,
                condition = currentStreak >= 7,
                title = "Perfecto",
                desc = "Registraste tu sueño durante 7 días continuos."
            )
            checkAndUnlock(
                type = AchievementType.STREAK_10,
                condition = currentStreak >= 10,
                title = "Constancia",
                desc = "¡Alcanzaste una racha de 10 días de registro!"
            )
            checkAndUnlock(
                type = AchievementType.STREAK_30,
                condition = currentStreak >= 30,
                title = "Búho Sincronizado",
                desc = "¡Increíble! Lograste una racha de 30 días."
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Algorithmic helper to calculate and update current and max streak metrics
     * when a new sleep entry is recorded.
     */
    private suspend fun updateStreakAfterLog(userId: String, logDate: LocalDate) {
        val dateString = logDate.toString()
        val currentStreakData = streakDataDao.getStreakByUserDirect(userId)

        // Fetch all sleep records to compute the streak dynamically
        val allRecords = sleepRecordDao.getRecordsByUserIdDirect(userId)
        val recordDates = allRecords.mapNotNull { 
            try { LocalDate.parse(it.date) } catch (e: Exception) { null } 
        }.toSet()

        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        // Dynamic streak calculation: count back from either today or yesterday
        var anchorDate = when {
            recordDates.contains(today) -> today
            recordDates.contains(yesterday) -> yesterday
            else -> null
        }

        var calculatedStreak = 0
        if (anchorDate != null) {
            while (recordDates.contains(anchorDate)) {
                calculatedStreak++
                anchorDate = anchorDate!!.minusDays(1)
            }
        }

        if (currentStreakData == null) {
            val newStreak = StreakDataEntity(
                userId = userId,
                currentStreak = calculatedStreak,
                maxStreak = calculatedStreak,
                lastUpdatedDate = dateString
            )
            syncRepository.saveStreak(newStreak)
        } else {
            val newMax = maxOf(calculatedStreak, currentStreakData.maxStreak)
            val updatedStreak = currentStreakData.copy(
                currentStreak = calculatedStreak,
                maxStreak = newMax,
                lastUpdatedDate = dateString,
                updatedAt = System.currentTimeMillis()
            )
            syncRepository.saveStreak(updatedStreak)
        }
    }

    private suspend fun evaluateCircadianAlerts(userId: String) {
        try {
            val allRecords = sleepRecordDao.getRecordsByUserIdDirect(userId)
            val today = LocalDate.now()
            val sevenDaysAgo = today.minusDays(6) // Last 7 days (today + 6 previous days)

            val recentRecords = allRecords.filter {
                val recordDate = try { LocalDate.parse(it.date) } catch (e: Exception) { null }
                recordDate != null && !recordDate.isBefore(sevenDaysAgo)
            }

            val zoneId = ZoneId.systemDefault()
            val weekdayWakeTimes = mutableListOf<Float>()
            val weekendWakeTimes = mutableListOf<Float>()

            for (record in recentRecords) {
                val recordDate = try { LocalDate.parse(record.date) } catch (e: Exception) { continue }
                val dayOfWeek = recordDate.dayOfWeek
                
                // Get the local time of wakeTime
                val instant = java.time.Instant.ofEpochMilli(record.wakeTime)
                val localWakeDateTime = LocalDateTime.ofInstant(instant, zoneId)
                val decimalHour = localWakeDateTime.hour + (localWakeDateTime.minute / 60.0f)

                if (dayOfWeek == java.time.DayOfWeek.SATURDAY || dayOfWeek == java.time.DayOfWeek.SUNDAY) {
                    weekendWakeTimes.add(decimalHour)
                } else {
                    weekdayWakeTimes.add(decimalHour)
                }
            }

            if (weekdayWakeTimes.isNotEmpty() && weekendWakeTimes.isNotEmpty()) {
                val avgWeekday = weekdayWakeTimes.average().toFloat()
                val avgWeekend = weekendWakeTimes.average().toFloat()
                val delta = Math.abs(avgWeekday - avgWeekend)

                if (delta > 2.0f) {
                    // Check if there is already an active alert for the user to avoid duplication
                    val recentAlerts = circadianAlertDao.getRecentAlerts(userId, 5)
                    val activeAlert = recentAlerts.find { !it.dismissed }
                    
                    if (activeAlert != null) {
                        val twelveHoursMs = 12 * 60 * 60 * 1000L
                        val shouldNotify = System.currentTimeMillis() - activeAlert.generatedAt > twelveHoursMs

                        // Update existing active alert's deltaHours, but only reset generatedAt if we notify
                        val updatedAlert = activeAlert.copy(
                            deltaHours = delta,
                            generatedAt = if (shouldNotify) System.currentTimeMillis() else activeAlert.generatedAt
                        )
                        circadianAlertDao.insertAlert(updatedAlert)

                        if (shouldNotify) {
                            NotificationHelper.showAchievementNotification(
                                context = context,
                                title = "Ritmo Circadiano Desalineado ⏰",
                                message = "Detectamos un Jet Lag Social de ${String.format("%.1f", delta)} horas entre semana y fin de semana."
                            )
                        }
                    } else {
                        // Insert a new alert
                        val alert = CircadianAlertEntity(
                            userId = userId,
                            deltaHours = delta
                        )
                        circadianAlertDao.insertAlert(alert)
                        
                        // Trigger a push notification warning the user
                        NotificationHelper.showAchievementNotification(
                            context = context,
                            title = "Ritmo Circadiano Desalineado ⏰",
                            message = "Detectamos un Jet Lag Social de ${String.format("%.1f", delta)} horas entre semana y fin de semana."
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resetSaveStatus() {
        _isSaveSuccessful.value = false
    }
}
