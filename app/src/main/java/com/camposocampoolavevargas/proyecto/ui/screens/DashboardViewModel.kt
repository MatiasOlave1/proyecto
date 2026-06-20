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

import com.camposocampoolavevargas.proyecto.data.repository.SyncRepository

/**
 * ViewModel for the Dashboard Screen.
 * Provides active sleep goal metrics for the home dashboard widgets.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val weeklyGoalDao: WeeklyGoalDao,
    private val streakDataDao: StreakDataDao,
    private val userSession: UserSession,
    private val syncRepository: SyncRepository
) : BaseViewModel() {

    private val _currentGoal = MutableStateFlow<WeeklyGoalEntity?>(null)
    val currentGoal: StateFlow<WeeklyGoalEntity?> = _currentGoal

    private val _streakData = MutableStateFlow<StreakDataEntity?>(null)
    val streakData: StateFlow<StreakDataEntity?> = _streakData

    init {
        loadCurrentGoal()
        loadStreakData()
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
}
