package com.camposocampoolavevargas.proyecto.data.repository

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class DisconnectMode {
    MANUAL,
    WEEKLY_STREAK
}

/**
 * Repository for managing disconnection settings using SharedPreferences.
 */
interface DisconnectSettingsRepository {
    fun getDisconnectMode(): DisconnectMode
    fun setDisconnectMode(mode: DisconnectMode)
    fun getManualBedtimeMillis(): Long
    fun setManualBedtimeMillis(millis: Long)
    fun getReminderOffsetMinutes(): Int
    fun setReminderOffsetMinutes(minutes: Int)
}

@Singleton
class DisconnectSettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : DisconnectSettingsRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "disconnect_settings",
        Context.MODE_PRIVATE
    )

    override fun getDisconnectMode(): DisconnectMode {
        val modeName = prefs.getString(KEY_DISCONNECT_MODE, DisconnectMode.WEEKLY_STREAK.name)
        return try {
            DisconnectMode.valueOf(modeName ?: DisconnectMode.WEEKLY_STREAK.name)
        } catch (e: Exception) {
            DisconnectMode.WEEKLY_STREAK
        }
    }

    override fun setDisconnectMode(mode: DisconnectMode) {
        prefs.edit().putString(KEY_DISCONNECT_MODE, mode.name).apply()
    }

    override fun getManualBedtimeMillis(): Long {
        // Default bedtime: 23:00 (11:00 PM) -> 23 * 3600 * 1000 = 82,800,000 ms
        return prefs.getLong(KEY_MANUAL_BEDTIME, 82800000L)
    }

    override fun setManualBedtimeMillis(millis: Long) {
        prefs.edit().putLong(KEY_MANUAL_BEDTIME, millis).apply()
    }

    override fun getReminderOffsetMinutes(): Int {
        return prefs.getInt(KEY_REMINDER_OFFSET, 90)
    }

    override fun setReminderOffsetMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_REMINDER_OFFSET, minutes).apply()
    }

    companion object {
        private const val KEY_DISCONNECT_MODE = "key_disconnect_mode"
        private const val KEY_MANUAL_BEDTIME = "key_manual_bedtime"
        private const val KEY_REMINDER_OFFSET = "key_reminder_offset"
    }
}
