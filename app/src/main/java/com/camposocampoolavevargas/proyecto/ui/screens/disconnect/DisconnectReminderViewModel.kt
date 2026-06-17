package com.camposocampoolavevargas.proyecto.ui.screens.disconnect

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.camposocampoolavevargas.proyecto.data.repository.DisconnectMode
import com.camposocampoolavevargas.proyecto.data.repository.DisconnectSettingsRepository
import com.camposocampoolavevargas.proyecto.data.repository.WeeklyGoalRepository
import com.camposocampoolavevargas.proyecto.service.notification.DisconnectAlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class DisconnectUiState(
    val mode: DisconnectMode = DisconnectMode.WEEKLY_STREAK,
    val manualBedtimeMillis: Long = 82800000L, // 23:00 default
    val weeklyGoalBedtimeMillis: Long? = null,
    val reminderOffsetMinutes: Int = 90,
    val isLoading: Boolean = false
)

@HiltViewModel
class DisconnectReminderViewModel @Inject constructor(
    private val settingsRepository: DisconnectSettingsRepository,
    private val weeklyGoalRepository: WeeklyGoalRepository,
    private val alarmScheduler: DisconnectAlarmScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(DisconnectUiState())
    val uiState: StateFlow<DisconnectUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val mode = settingsRepository.getDisconnectMode()
            val manualBedtime = settingsRepository.getManualBedtimeMillis()
            val offset = settingsRepository.getReminderOffsetMinutes()
            
            _uiState.update { 
                it.copy(
                    mode = mode,
                    manualBedtimeMillis = manualBedtime,
                    reminderOffsetMinutes = offset
                )
            }
            
            loadWeeklyGoal()
        }
    }

    private suspend fun loadWeeklyGoal() {
        val calendar = Calendar.getInstance()
        val week = calendar.get(Calendar.WEEK_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)
        
        // Hardcoded userId for now
        val userId = "user123" 
        
        val goal = weeklyGoalRepository.getCurrentGoal(userId, week, year).firstOrNull()
        _uiState.update { it.copy(weeklyGoalBedtimeMillis = goal?.bedtimeLimitMillis) }
        
        // Initial scheduling based on saved settings
        rescheduleAlarm()
    }

    fun onModeChange(newMode: DisconnectMode) {
        settingsRepository.setDisconnectMode(newMode)
        _uiState.update { it.copy(mode = newMode) }
        rescheduleAlarm()
    }

    fun onManualTimeChange(hour: Int, minute: Int) {
        val millis = (hour * 3600 + minute * 60) * 1000L
        settingsRepository.setManualBedtimeMillis(millis)
        _uiState.update { it.copy(manualBedtimeMillis = millis) }
        
        if (_uiState.value.mode == DisconnectMode.MANUAL) {
            rescheduleAlarm()
        }
    }

    fun onOffsetChange(minutes: Int) {
        settingsRepository.setReminderOffsetMinutes(minutes)
        _uiState.update { it.copy(reminderOffsetMinutes = minutes) }
        rescheduleAlarm()
    }

    fun onTestNotification() {
        alarmScheduler.scheduleTestReminder()
    }

    private fun rescheduleAlarm() {
        val state = _uiState.value
        val bedtime = if (state.mode == DisconnectMode.MANUAL) {
            state.manualBedtimeMillis
        } else {
            state.weeklyGoalBedtimeMillis ?: 82800000L // Fallback to 23:00
        }
        
        Log.d("DisconnectAlarm", "VM Rescheduling: Mode=${state.mode}, Bedtime=$bedtime, Offset=${state.reminderOffsetMinutes}")
        alarmScheduler.scheduleReminder(bedtime, state.reminderOffsetMinutes)
    }
}
