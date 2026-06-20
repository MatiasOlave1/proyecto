package com.camposocampoolavevargas.proyecto.ui.screens

import androidx.lifecycle.viewModelScope
import com.camposocampoolavevargas.proyecto.data.local.UserSession
import com.camposocampoolavevargas.proyecto.data.local.dao.SleepRecordDao
import com.camposocampoolavevargas.proyecto.data.local.dao.StreakDataDao
import com.camposocampoolavevargas.proyecto.data.local.entity.SleepRecordEntity
import com.camposocampoolavevargas.proyecto.data.local.entity.StreakDataEntity
import com.camposocampoolavevargas.proyecto.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel for the Streaks Screen (RF07 — Seguimiento de rachas).
 * Exposes current and max streak metrics, and computes logging status for the current week's days.
 */
@HiltViewModel
class StreaksViewModel @Inject constructor(
    private val streakDataDao: StreakDataDao,
    private val sleepRecordDao: SleepRecordDao,
    private val userSession: UserSession
) : BaseViewModel() {

    private val _streakData = MutableStateFlow<StreakDataEntity?>(null)
    val streakData: StateFlow<StreakDataEntity?> = _streakData

    // Represents which days of the current week (Mon-Sun) have sleep records
    private val _weeklyLoggingStatus = MutableStateFlow<List<Boolean>>(List(7) { false })
    val weeklyLoggingStatus: StateFlow<List<Boolean>> = _weeklyLoggingStatus

    init {
        loadStreakData()
        loadWeeklyLogs()
    }

    private fun loadStreakData() {
        val userId = userSession.getActiveUserId() ?: return
        viewModelScope.launch {
            streakDataDao.getStreakByUser(userId).collect { streak ->
                _streakData.value = streak
            }
        }
    }

    private fun loadWeeklyLogs() {
        val userId = userSession.getActiveUserId() ?: return

        // Compute Monday to Sunday dates for the current week
        val today = LocalDate.now()
        val monday = today.with(DayOfWeek.MONDAY)
        val sunday = today.with(DayOfWeek.SUNDAY)

        val weekDateStrings = (0..6).map { monday.plusDays(it.toLong()).toString() }

        viewModelScope.launch {
            sleepRecordDao.getRecordsByDateRange(userId, monday.toString(), sunday.toString())
                .collect { records ->
                    // Map each day of the week to true/false depending on if a record exists for that date
                    val loggedDates = records.map { it.date }.toSet()
                    val status = weekDateStrings.map { dateStr ->
                        loggedDates.contains(dateStr)
                    }
                    _weeklyLoggingStatus.value = status
                }
        }
    }
}
