package com.camposocampoolavevargas.proyecto.service.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android implementation of [DisconnectAlarmScheduler] using [AlarmManager].
 */
@Singleton
class DisconnectAlarmSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : DisconnectAlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun scheduleReminder(bedtimeMillisFromMidnight: Long, offsetMinutes: Int) {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MILLISECOND, bedtimeMillisFromMidnight.toInt())
            add(Calendar.MINUTE, -offsetMinutes)
            
            // If the calculated time is in the past (or within the next minute), schedule for tomorrow
            if (timeInMillis <= now + 60000) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        Log.d("DisconnectAlarm", "--- Scheduling Reminder ---")
        Log.d("DisconnectAlarm", "Input Bedtime Millis: $bedtimeMillisFromMidnight")
        Log.d("DisconnectAlarm", "Input Offset Minutes: $offsetMinutes")
        Log.d("DisconnectAlarm", "Calculated Trigger: ${calendar.time} (${calendar.timeInMillis})")
        Log.d("DisconnectAlarm", "Current System Time: ${java.util.Date(now)} ($now)")

        val intent = Intent(context, DisconnectReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: Exception) {
            Log.e("DisconnectAlarm", "Error scheduling exact alarm", e)
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    override fun scheduleTestReminder() {
        val triggerTime = System.currentTimeMillis() + 10000 // 10 seconds for more buffer
        Log.d("DisconnectAlarm", "Scheduling TEST reminder for 10s from now")
        
        val intent = Intent(context, DisconnectReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.d("DisconnectAlarm", "Exact test alarm set successfully")
        } catch (e: Exception) {
            Log.e("DisconnectAlarm", "Error scheduling test alarm", e)
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }
}
