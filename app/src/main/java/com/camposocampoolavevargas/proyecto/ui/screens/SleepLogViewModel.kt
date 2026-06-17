package com.camposocampoolavevargas.proyecto.ui.screens

import androidx.lifecycle.viewModelScope
import com.camposocampoolavevargas.proyecto.data.local.UserSession
import com.camposocampoolavevargas.proyecto.data.local.dao.SleepRecordDao
import com.camposocampoolavevargas.proyecto.data.local.dao.StreakDataDao
import com.camposocampoolavevargas.proyecto.data.local.entity.SleepRecordEntity
import com.camposocampoolavevargas.proyecto.data.local.entity.StreakDataEntity
import com.camposocampoolavevargas.proyecto.data.local.model.SleepQuality
import com.camposocampoolavevargas.proyecto.data.local.model.SyncStatus
import com.camposocampoolavevargas.proyecto.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for the Sleep Log Screen (RF02 — Registro diario de sueño).
 * Manages the daily sleep input form state and persists entries in Room DB.
 */
@HiltViewModel
class SleepLogViewModel @Inject constructor(
    private val sleepRecordDao: SleepRecordDao,
    private val streakDataDao: StreakDataDao,
    private val userSession: UserSession
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
                sleepRecordDao.insertRecord(record)
                updateStreakAfterLog(userId, dateVal)
                _isSaveSuccessful.value = true
            } catch (e: Exception) {
                _errorMessage.value = "Error al guardar: ${e.localizedMessage ?: "Desconocido"}"
            }
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
            streakDataDao.insertOrUpdateStreak(newStreak)
        } else {
            val newMax = maxOf(calculatedStreak, currentStreakData.maxStreak)
            val updatedStreak = currentStreakData.copy(
                currentStreak = calculatedStreak,
                maxStreak = newMax,
                lastUpdatedDate = dateString,
                updatedAt = System.currentTimeMillis()
            )
            streakDataDao.insertOrUpdateStreak(updatedStreak)
        }
    }

    fun resetSaveStatus() {
        _isSaveSuccessful.value = false
    }
}
