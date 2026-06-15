package com.camposocampoolavevargas.proyecto.service.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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

    override fun scheduleReminder(bedtimeMillisFromMidnight: Long) {
        val calendar = Calendar.getInstance().apply {
            // Reset to midnight
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // Add the bedtime offset
            add(Calendar.MILLISECOND, bedtimeMillisFromMidnight.toInt())
            
            // Subtract 90 minutes as per OpenSpec
            add(Calendar.MINUTE, -90)
            
            // If the calculated time for today has already passed, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, DisconnectReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Use exact alarm to ensure reliability during Doze mode
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Fallback if exact alarm permission is not granted
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
}
