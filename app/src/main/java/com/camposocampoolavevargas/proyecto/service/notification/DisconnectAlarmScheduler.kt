package com.camposocampoolavevargas.proyecto.service.notification

/**
 * Interface for scheduling the disconnect reminder alarm.
 */
interface DisconnectAlarmScheduler {
    /**
     * Schedules a reminder notification [offsetMinutes] before the given [bedtimeMillisFromMidnight].
     * [bedtimeMillisFromMidnight] is milliseconds from midnight (e.g. 23:00 = 82800000).
     */
    fun scheduleReminder(bedtimeMillisFromMidnight: Long, offsetMinutes: Int)

    /**
     * Schedules a reminder notification 5 seconds from now for testing purposes.
     */
    fun scheduleTestReminder()
}
