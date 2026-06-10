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
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["phone"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey
    val userId: String = UUID.randomUUID().toString(),
    val email: String,
    val passwordHash: String,
    val phone: String? = null,
    val name: String? = null,
    val birthDate: Long? = null,
    val region: String? = null,
    val commune: String? = null,
    val university: String? = null,
    val career: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)


