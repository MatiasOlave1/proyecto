package com.camposocampoolavevargas.proyecto.service.notification

/**
 * Interface for scheduling the disconnect reminder alarm.
 */
interface DisconnectAlarmScheduler {
    /**
     * Schedules a reminder notification 90 minutes before the given [bedtimeMillisFromMidnight].
     * [bedtimeMillisFromMidnight] is milliseconds from midnight (e.g. 23:00 = 82800000).
     */
    fun scheduleReminder(bedtimeMillisFromMidnight: Long)
}
