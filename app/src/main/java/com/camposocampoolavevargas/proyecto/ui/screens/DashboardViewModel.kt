package com.camposocampoolavevargas.proyecto.ui.screens

import androidx.lifecycle.viewModelScope
import com.camposocampoolavevargas.proyecto.data.local.UserSession
import com.camposocampoolavevargas.proyecto.data.local.dao.WeeklyGoalDao
import com.camposocampoolavevargas.proyecto.data.local.dao.StreakDataDao
import com.camposocampoolavevargas.proyecto.data.local.dao.CircadianAlertDao
import com.camposocampoolavevargas.proyecto.data.local.dao.SleepRecordDao
import com.camposocampoolavevargas.proyecto.data.local.entity.WeeklyGoalEntity
import com.camposocampoolavevargas.proyecto.data.local.entity.StreakDataEntity
import com.camposocampoolavevargas.proyecto.data.local.entity.CircadianAlertEntity
import com.camposocampoolavevargas.proyecto.data.local.entity.SleepRecordEntity
import com.camposocampoolavevargas.proyecto.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import com.camposocampoolavevargas.proyecto.util.DateUtils
import com.camposocampoolavevargas.proyecto.data.local.dao.SleepRecordDao



import com.camposocampoolavevargas.proyecto.data.repository.SyncRepository

/**
 * ViewModel for the Dashboard Screen.
 * Provides active sleep goal metrics, circadian alerts, and recent sleep records for the home dashboard widgets.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val weeklyGoalDao: WeeklyGoalDao,
    private val streakDataDao: StreakDataDao,
    private val sleepRecordDao: SleepRecordDao,
    private val userSession: UserSession
    private val circadianAlertDao: CircadianAlertDao,
    private val sleepRecordDao: SleepRecordDao,
    private val userSession: UserSession,
    private val syncRepository: SyncRepository
) : BaseViewModel() {

    private val _currentGoal = MutableStateFlow<WeeklyGoalEntity?>(null)
    val currentGoal: StateFlow<WeeklyGoalEntity?> = _currentGoal

    private val _streakData = MutableStateFlow<StreakDataEntity?>(null)
    val streakData: StateFlow<StreakDataEntity?> = _streakData

    private val _averageHours = MutableStateFlow(0.0)
    val averageHours: StateFlow<Double> = _averageHours

    private val _mostCommonQuality = MutableStateFlow("Sin datos")
    val mostCommonQuality: StateFlow<String> = _mostCommonQuality

    private val _monthlyGoalsCompleted = MutableStateFlow(0)
    val monthlyGoalsCompleted: StateFlow<Int> = _monthlyGoalsCompleted
    private val _circadianAlerts = MutableStateFlow<List<CircadianAlertEntity>>(emptyList())
    val circadianAlerts: StateFlow<List<CircadianAlertEntity>> = _circadianAlerts

    private val _recentSleepRecords = MutableStateFlow<List<SleepRecordEntity>>(emptyList())
    val recentSleepRecords: StateFlow<List<SleepRecordEntity>> = _recentSleepRecords

    private var currentGoalJob: Job? = null
    private var streakDataJob: Job? = null
    private var circadianAlertsJob: Job? = null
    private var recentSleepRecordsJob: Job? = null

    init {
        loadCurrentGoal()
        loadStreakData()
        loadMonthlyStatistics()
        loadCircadianAlerts()
        loadRecentSleepRecords()
        triggerSync()
    }

    private fun triggerSync() {
        val userId = userSession.getActiveUserId() ?: return
        viewModelScope.launch {
            syncRepository.syncAll(userId)
        }
    }

    /**
     * Checks user session and queries Room for any weekly goal active this ISO week.
     */
    fun loadCurrentGoal() {
        val userId = userSession.getActiveUserId() ?: return
        
        val (isoWeek, isoYear) = DateUtils.getIsoWeekYear()

        currentGoalJob?.cancel()
        currentGoalJob = viewModelScope.launch {
            weeklyGoalDao.getCurrentGoal(userId, isoWeek, isoYear).collect { goal ->
                _currentGoal.value = goal
            }
        }
    }

    /**
     * Listens to streak changes for the active user session and updates the UI flow.
     */
    fun loadStreakData() {
        val userId = userSession.getActiveUserId() ?: return
        streakDataJob?.cancel()
        streakDataJob = viewModelScope.launch {
            streakDataDao.getStreakByUser(userId).collect { streak ->
                _streakData.value = streak
            }
        }
    }
    fun loadMonthlyStatistics() {
        val userId = userSession.getActiveUserId() ?: return

        viewModelScope.launch {

            val records = sleepRecordDao.getRecordsByUserIdDirect(userId)

            if (records.isEmpty()) {
                _averageHours.value = 0.0
                _mostCommonQuality.value = "Sin datos"
                _monthlyGoalsCompleted.value = 0
                return@launch
            }

            val durations = records.map {
                it.durationMinutes / 60.0
            }

            _averageHours.value = durations.average()

            val qualityMode =
                records.groupingBy { it.quality.name }
                    .eachCount()
                    .maxByOrNull { it.value }
                    ?.key ?: "Sin datos"

            _mostCommonQuality.value = qualityMode

            _monthlyGoalsCompleted.value =
                records.count {
                    (it.durationMinutes / 60.0) >= 7.0
                }

    /**
     * Listens to active circadian alerts for the active user session and updates the UI flow.
     */
    fun loadCircadianAlerts() {
        val userId = userSession.getActiveUserId() ?: return
        circadianAlertsJob?.cancel()
        circadianAlertsJob = viewModelScope.launch {
            circadianAlertDao.getActiveAlerts(userId).collect { alerts ->
                _circadianAlerts.value = alerts
            }
        }
    }

    /**
     * Fetches sleep records for the last 7 days to display in the circadian analysis calendar.
     */
    fun loadRecentSleepRecords() {
        val userId = userSession.getActiveUserId() ?: return
        val today = LocalDate.now().toString()
        val sevenDaysAgo = LocalDate.now().minusDays(6).toString()
        recentSleepRecordsJob?.cancel()
        recentSleepRecordsJob = viewModelScope.launch {
            sleepRecordDao.getRecordsByDateRange(userId, sevenDaysAgo, today).collect { records ->
                _recentSleepRecords.value = records
            }
        }
    }
}


