package com.camposocampoolavevargas.proyecto.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity representing a sleep journal entry in the "journal_entries" table.
 * Content is stored as-is in Room (to be encrypted/decrypted at the repository level).
 */
@Entity(
    tableName = "journal_entries",
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
data class JournalEntryEntity(
    @PrimaryKey
    val entryId: String = UUID.randomUUID().toString(),
    val userId: String,
    val content: String,
    val date: String, // ISO 8601 "YYYY-MM-DD"
    val autoDelete: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

