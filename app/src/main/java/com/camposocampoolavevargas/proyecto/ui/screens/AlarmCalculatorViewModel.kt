package com.camposocampoolavevargas.proyecto.ui.screens

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.camposocampoolavevargas.proyecto.data.local.UserSession
import com.camposocampoolavevargas.proyecto.service.alarm.AlarmReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AlarmCalculatorViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userSession: UserSession
) : ViewModel() {

    private val _bedtimeHour = MutableStateFlow(23) // default bedtime 11 PM
    val bedtimeHour: StateFlow<Int> = _bedtimeHour

    private val _bedtimeMinute = MutableStateFlow(0)
    val bedtimeMinute: StateFlow<Int> = _bedtimeMinute

    private val _selectedCycles = MutableStateFlow(5) // default 5 cycles (7.5 hours)
    val selectedCycles: StateFlow<Int> = _selectedCycles

    private val _wakeHour = MutableStateFlow(7)
    val wakeHour: StateFlow<Int> = _wakeHour

    private val _wakeMinute = MutableStateFlow(0)
    val wakeMinute: StateFlow<Int> = _wakeMinute

    private val _alarmSet = MutableStateFlow(false)
    val alarmSet: StateFlow<Boolean> = _alarmSet

    private val _selectedDays = MutableStateFlow(setOf<Int>()) // Calendar.MONDAY etc
    val selectedDays: StateFlow<Set<Int>> = _selectedDays

    init {
        // Load initial alarm settings if saved
        val isSet = userSession.getAlarmSet()
        _alarmSet.value = isSet
        if (isSet) {
            _wakeHour.value = userSession.getAlarmHour()
            _wakeMinute.value = userSession.getAlarmMinute()
            // Estimate bedtime from wake time using saved cycles
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, _wakeHour.value)
                set(Calendar.MINUTE, _wakeMinute.value)
                set(Calendar.SECOND, 0)
                add(Calendar.MINUTE, -((_selectedCycles.value * 90) + 14))
            }
            _bedtimeHour.value = cal.get(Calendar.HOUR_OF_DAY)
            _bedtimeMinute.value = cal.get(Calendar.MINUTE)
        } else {
            // Default bedtime to now
            setBedtimeToNow()
        }
    }

    fun updateBedtime(hour: Int, minute: Int) {
        _bedtimeHour.value = hour
        _bedtimeMinute.value = minute
        recalculateWakeTime()
    }

    fun updateCycles(cycles: Int) {
        _selectedCycles.value = cycles
        recalculateWakeTime()
    }

    fun setBedtimeToNow() {
        val now = Calendar.getInstance()
        _bedtimeHour.value = now.get(Calendar.HOUR_OF_DAY)
        _bedtimeMinute.value = now.get(Calendar.MINUTE)
        recalculateWakeTime()
    }

    private fun recalculateWakeTime() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, _bedtimeHour.value)
            set(Calendar.MINUTE, _bedtimeMinute.value)
            set(Calendar.SECOND, 0)
            // Add cycles * 90 minutes + 14 minutes to fall asleep
            add(Calendar.MINUTE, (_selectedCycles.value * 90) + 14)
        }
        _wakeHour.value = cal.get(Calendar.HOUR_OF_DAY)
        _wakeMinute.value = cal.get(Calendar.MINUTE)
    }

    fun toggleDay(day: Int) {
        val current = _selectedDays.value.toMutableSet()
        if (current.contains(day)) current.remove(day) else current.add(day)
        _selectedDays.value = current
    }

    fun setAlarm() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, _wakeHour.value)
            set(Calendar.MINUTE, _wakeMinute.value)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S &&
            alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                cal.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                cal.timeInMillis,
                pendingIntent
            )
        }
        userSession.saveAlarm(true, _wakeHour.value, _wakeMinute.value)
        _alarmSet.value = true
    }

    fun cancelAlarm() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        userSession.saveAlarm(false, _wakeHour.value, _wakeMinute.value)
        _alarmSet.value = false
    }

    fun simulateAlarm() {
        val intent = Intent(context, AlarmReceiver::class.java)
        context.sendBroadcast(intent)
    }
}