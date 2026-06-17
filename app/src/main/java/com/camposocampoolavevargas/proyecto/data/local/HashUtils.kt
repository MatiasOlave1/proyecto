package com.camposocampoolavevargas.proyecto.data.local

import java.security.MessageDigest

/**
 * Utility object for password hashing using SHA-256.
 */
object HashUtils {

    /**
     * Hashes a password string using SHA-256 and returns a 64-character hex string.
     */
    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
