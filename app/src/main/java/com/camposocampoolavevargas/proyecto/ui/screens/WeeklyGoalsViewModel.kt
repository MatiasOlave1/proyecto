package com.camposocampoolavevargas.proyecto.ui.screens

import androidx.lifecycle.viewModelScope
import com.camposocampoolavevargas.proyecto.data.local.UserSession
import com.camposocampoolavevargas.proyecto.data.local.entity.WeeklyGoalEntity
import com.camposocampoolavevargas.proyecto.data.repository.WeeklyGoalRepository
import com.camposocampoolavevargas.proyecto.data.repository.SyncRepository
import com.camposocampoolavevargas.proyecto.ui.BaseViewModel
import com.camposocampoolavevargas.proyecto.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class WeeklyGoalsViewModel @Inject constructor(
    private val weeklyGoalRepository: WeeklyGoalRepository, // ← corregido
    private val userSession: UserSession,
    private val syncRepository: SyncRepository
) : BaseViewModel() {

    private val _goalState = MutableStateFlow<WeeklyGoalEntity?>(null)
    val goalState: StateFlow<WeeklyGoalEntity?> = _goalState

    private val _saveState = MutableStateFlow<UiState<String>>(UiState.Success(""))
    val saveState: StateFlow<UiState<String>> = _saveState

    init {
        loadCurrentGoal()
    }

    private fun loadCurrentGoal() {
        val userId = userSession.getActiveUserId() ?: return
        val (isoWeek, isoYear) = DateUtils.getIsoWeekYear()

        viewModelScope.launch {
            weeklyGoalRepository.getCurrentGoal(userId, isoWeek, isoYear).collect { goal ->
                _goalState.value = goal
            }
        }
    }

    fun getCurrentWeekInfo(): String {
        val (week, year) = DateUtils.getIsoWeekYear()
        return "Semana $week, $year"
    }

    fun saveGoal(minHours: Float, requiredDays: Int, bedtimeHour: Int, bedtimeMinute: Int) {
        val userId = userSession.getActiveUserId()
        if (userId == null) {
            _saveState.value = UiState.Error("Usuario no autenticado. Inicie sesión nuevamente.")
            return
        }

        viewModelScope.launch {
            _saveState.value = UiState.Loading
            try {
                val (isoWeek, isoYear) = DateUtils.getIsoWeekYear()
                val bedtimeLimitMillis = (bedtimeHour * 60 * 60 * 1000L) + (bedtimeMinute * 60 * 1000L)
                val existingGoal = _goalState.value
                val goalId = existingGoal?.goalId ?: UUID.randomUUID().toString()

                val goal = WeeklyGoalEntity(
                    goalId = goalId,
                    userId = userId,
                    isoWeek = isoWeek,
                    isoYear = isoYear,
                    minHours = minHours,
                    requiredDays = requiredDays,
                    bedtimeLimitMillis = bedtimeLimitMillis,
                    createdAt = existingGoal?.createdAt ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                syncRepository.saveWeeklyGoal(goal)
                _saveState.value = UiState.Success("¡Meta semanal guardada con éxito!")
            } catch (e: Exception) {
                _saveState.value = UiState.Error(e.localizedMessage ?: "Error al guardar la meta de sueño.")
            }
        }
    }

    fun clearSaveState() {
        _saveState.value = UiState.Success("")
    }
}