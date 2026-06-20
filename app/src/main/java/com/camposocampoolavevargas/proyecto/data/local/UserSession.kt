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
}
