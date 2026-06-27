package com.camposocampoolavevargas.proyecto.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.camposocampoolavevargas.proyecto.data.local.model.AchievementType
import java.util.UUID

/**
 * Entity representing an achievement in the "achievements" table.
 * It prevents duplicate achievements of the same type for a user using a unique composite index.
 */
@Entity(
    tableName = "achievements",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId", "type"], unique = true)
    ]
)
data class AchievementEntity(
    @PrimaryKey
    val achievementId: String = UUID.randomUUID().toString(),
    val userId: String,
    val type: AchievementType,
    val unlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val points: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

