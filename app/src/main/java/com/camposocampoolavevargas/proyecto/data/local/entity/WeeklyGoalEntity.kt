package com.camposocampoolavevargas.proyecto.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity representing a weekly goal in the "weekly_goals" table.
 */
@Entity(
    tableName = "weekly_goals",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId", "isoWeek", "isoYear"], unique = true)
    ]
)
data class WeeklyGoalEntity(
    @PrimaryKey
    val goalId: String = UUID.randomUUID().toString(),
    val userId: String,
    val isoWeek: Int, // ISO week number 1-53
    val isoYear: Int,
    val minHours: Float,
    val requiredDays: Int,
    val bedtimeLimitMillis: Long, // milliseconds from midnight, e.g. 23:00 = 82800000
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

