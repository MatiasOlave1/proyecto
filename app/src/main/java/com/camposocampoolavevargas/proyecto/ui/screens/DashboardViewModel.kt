package com.camposocampoolavevargas.proyecto.ui.screens

import androidx.lifecycle.viewModelScope
import com.camposocampoolavevargas.proyecto.data.local.UserSession
import com.camposocampoolavevargas.proyecto.data.local.dao.WeeklyGoalDao
import com.camposocampoolavevargas.proyecto.data.local.dao.StreakDataDao
import com.camposocampoolavevargas.proyecto.data.local.entity.WeeklyGoalEntity
import com.camposocampoolavevargas.proyecto.data.local.entity.StreakDataEntity
import com.camposocampoolavevargas.proyecto.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.camposocampoolavevargas.proyecto.util.DateUtils
import com.camposocampoolavevargas.proyecto.data.local.dao.SleepRecordDao



/**
 * ViewModel for the Dashboard Screen.
 * Provides active sleep goal metrics for the home dashboard widgets.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val weeklyGoalDao: WeeklyGoalDao,
    private val streakDataDao: StreakDataDao,
    private val sleepRecordDao: SleepRecordDao,
    private val userSession: UserSession
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

    init {
        loadCurrentGoal()
        loadStreakData()
        loadMonthlyStatistics()
    }

    /**
     * Checks user session and queries Room for any weekly goal active this ISO week.
     */
    fun loadCurrentGoal() {
        val userId = userSession.getActiveUserId() ?: return
        
        val (isoWeek, isoYear) = DateUtils.getIsoWeekYear()

        viewModelScope.launch {
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
        viewModelScope.launch {
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
        }
    }
}


