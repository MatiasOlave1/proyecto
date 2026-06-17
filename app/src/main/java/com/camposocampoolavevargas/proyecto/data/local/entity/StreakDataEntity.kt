package com.camposocampoolavevargas.proyecto.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Entity representing streak statistics in the "streak_data" table.
 * The primary key is the userId, establishing a 1-to-1 relationship with the UserEntity.
 */
@Entity(
    tableName = "streak_data",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class StreakDataEntity(
    @PrimaryKey
    val userId: String,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val lastUpdatedDate: String, // ISO 8601 "YYYY-MM-DD"
    val updatedAt: Long = System.currentTimeMillis()
)

