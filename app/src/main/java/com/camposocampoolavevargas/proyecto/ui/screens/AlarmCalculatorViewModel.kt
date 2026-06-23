package com.camposocampoolavevargas.proyecto.ui.screens



import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.camposocampoolavevargas.proyecto.service.alarm.AlarmReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar
import javax.inject.Inject

data class SleepWindow(
    val bedtime: String,
    val cyclesCount: Int
)

@HiltViewModel
class AlarmCalculatorViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _wakeHour = MutableStateFlow(7)
    val wakeHour: StateFlow<Int> = _wakeHour

    private val _wakeMinute = MutableStateFlow(0)
    val wakeMinute: StateFlow<Int> = _wakeMinute

    private val _sleepWindows = MutableStateFlow<List<SleepWindow>>(emptyList())
    val sleepWindows: StateFlow<List<SleepWindow>> = _sleepWindows

    private val _alarmSet = MutableStateFlow(false)
    val alarmSet: StateFlow<Boolean> = _alarmSet

    private val _selectedDays = MutableStateFlow(setOf<Int>()) // Calendar.MONDAY etc
    val selectedDays: StateFlow<Set<Int>> = _selectedDays

    private val _selectedWindow = MutableStateFlow<SleepWindow?>(null)
    val selectedWindow: StateFlow<SleepWindow?> = _selectedWindow

    fun selectWindow(window: SleepWindow) {
        _selectedWindow.value = window
    }

    init {
        calculateSleepWindows(
            _wakeHour.value,
            _wakeMinute.value
        )
    }

    fun updateWakeTime(hour: Int, minute: Int) {
        _wakeHour.value = hour
        _wakeMinute.value = minute
        calculateSleepWindows(hour, minute)
    }

    fun toggleDay(day: Int) {
        val current = _selectedDays.value.toMutableSet()
        if (current.contains(day)) current.remove(day) else current.add(day)
        _selectedDays.value = current
    }

    private fun calculateSleepWindows(wakeHour: Int, wakeMinute: Int) {
        val windows = mutableListOf<SleepWindow>()
        // 14 min to fall asleep + N cycles of 90 min
        val fallAsleepMinutes = 14
        for (cycles in 6 downTo 3) {
            val totalMinutes = (cycles * 90) + fallAsleepMinutes
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, wakeHour)
                set(Calendar.MINUTE, wakeMinute)
                set(Calendar.SECOND, 0)
                add(Calendar.MINUTE, -totalMinutes)
            }
            val h = cal.get(Calendar.HOUR_OF_DAY)
            val m = cal.get(Calendar.MINUTE)
            windows.add(SleepWindow(
                bedtime = String.format("%02d:%02d", h, m),
                cyclesCount = cycles
            ))
        }
        _sleepWindows.value = windows
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
        _alarmSet.value = false
    }
}