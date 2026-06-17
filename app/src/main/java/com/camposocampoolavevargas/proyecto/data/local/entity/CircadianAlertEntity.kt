package com.camposocampoolavevargas.proyecto.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity representing a circadian rhythm alert in the "circadian_alerts" table.
 * It tracks sleep pattern variance (deltaHours) to advise the user.
 */
@Entity(
    tableName = "circadian_alerts",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"])
    ]
)
data class CircadianAlertEntity(
    @PrimaryKey
    val alertId: String = UUID.randomUUID().toString(),
    val userId: String,
    val deltaHours: Float, // Calculated difference in wake times
    val generatedAt: Long = System.currentTimeMillis(), // epoch millis UTC
    val dismissed: Boolean = false
)

