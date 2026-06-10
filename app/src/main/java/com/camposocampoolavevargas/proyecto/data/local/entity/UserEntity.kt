package com.camposocampoolavevargas.proyecto.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity representing a user in the "users" table.
 */
@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey
    val userId: String = UUID.randomUUID().toString(),
    val name: String,
    val birthDate: Long, // epoch millis
    val region: String,
    val commune: String,
    val university: String,
    val career: String,
    val email: String,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)


