package com.camposocampoolavevargas.proyecto.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.camposocampoolavevargas.proyecto.data.local.model.SleepQuality
import com.camposocampoolavevargas.proyecto.data.local.model.SyncStatus
import java.util.UUID

/**
 * Entity representing a sleep log in the "sleep_records" table.
 */
@Entity(
    tableName = "sleep_records",
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
data class SleepRecordEntity(
    @PrimaryKey
    val recordId: String = UUID.randomUUID().toString(),
    val userId: String,
    val sleepTime: Long, // epoch millis UTC
    val wakeTime: Long,  // epoch millis UTC
    val durationMinutes: Int = ((wakeTime - sleepTime) / (1000 * 60)).toInt(),
    val quality: SleepQuality,
    val date: String,    // ISO 8601 "YYYY-MM-DD"
    val syncStatus: SyncStatus,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

