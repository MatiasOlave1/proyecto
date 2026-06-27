package com.camposocampoolavevargas.proyecto.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persistent helper class to store and retrieve the active user session using SharedPreferences.
 * Anotated as a Singleton and injected via Hilt.
 */
@Singleton
class UserSession @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences = context.getSharedPreferences("dormibienU_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ACTIVE_USER_ID = "active_user_id"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_SPOTIFY_TRACK_URI = "spotify_track_uri"
        private const val KEY_SPOTIFY_TRACK_NAME = "spotify_track_name"
        private const val KEY_ALARM_SET = "alarm_set"
        private const val KEY_ALARM_HOUR = "alarm_hour"
        private const val KEY_ALARM_MINUTE = "alarm_minute"
    }

    /**
     * Stores the logged-in user's ID to keep the session active.
     */
    fun login(userId: String) {
        sharedPreferences.edit().putString(KEY_ACTIVE_USER_ID, userId).apply()
    }

    /**
     * Stores the authentication token.
     */
    fun saveToken(token: String) {
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    /**
     * Returns the stored authentication token.
     */
    fun getToken(): String? {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }

    /**
     * Clears the session.
     */
    fun logout() {
        sharedPreferences.edit()
            .remove(KEY_ACTIVE_USER_ID)
            .remove(KEY_AUTH_TOKEN)
            .apply()
    }

    /**
     * Returns the active user ID, or null if no session is active.
     */
    fun getActiveUserId(): String? {
        return sharedPreferences.getString(KEY_ACTIVE_USER_ID, null)
    }

    /**
     * Checks if a user is currently logged in.
     */
    fun isLoggedIn(): Boolean {
        return getActiveUserId() != null
    }

    /**
     * Saves the Spotify track preference for the wake-up alarm.
     */
    fun saveSpotifyAlarm(uri: String?, name: String?) {
        sharedPreferences.edit()
            .putString(KEY_SPOTIFY_TRACK_URI, uri)
            .putString(KEY_SPOTIFY_TRACK_NAME, name)
            .apply()
    }

    /**
     * Returns the configured Spotify track URI.
     */
    fun getSpotifyAlarmUri(): String? {
        return sharedPreferences.getString(KEY_SPOTIFY_TRACK_URI, null)
    }

    /**
     * Returns the configured Spotify track name.
     */
    fun getSpotifyAlarmName(): String? {
        return sharedPreferences.getString(KEY_SPOTIFY_TRACK_NAME, null)
    }

    /**
     * Saves the active alarm configuration state.
     */
    fun saveAlarm(set: Boolean, hour: Int, minute: Int) {
        sharedPreferences.edit()
            .putBoolean(KEY_ALARM_SET, set)
            .putInt(KEY_ALARM_HOUR, hour)
            .putInt(KEY_ALARM_MINUTE, minute)
            .apply()
    }

    /**
     * Returns true if an alarm is active.
     */
    fun getAlarmSet(): Boolean {
        return sharedPreferences.getBoolean(KEY_ALARM_SET, false)
    }

    /**
     * Returns the active alarm hour.
     */
    fun getAlarmHour(): Int {
        return sharedPreferences.getInt(KEY_ALARM_HOUR, 7)
    }

    /**
     * Returns the active alarm minute.
     */
    fun getAlarmMinute(): Int {
        return sharedPreferences.getInt(KEY_ALARM_MINUTE, 0)
    }
}
