package com.camposocampoolavevargas.proyecto.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.camposocampoolavevargas.proyecto.data.local.entity.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the "journal_entries" table.
 */
@Dao
interface JournalEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntryEntity)

    @Update
    suspend fun updateEntry(entry: JournalEntryEntity)

    @Query("DELETE FROM journal_entries WHERE entryId = :entryId")
    suspend fun deleteEntry(entryId: String)

    @Query("SELECT * FROM journal_entries WHERE userId = :userId ORDER BY date DESC")
    fun getEntriesByUser(userId: String): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE userId = :userId AND date = :date LIMIT 1")
    fun getEntryByDate(userId: String, date: String): Flow<JournalEntryEntity?>

    @Query("SELECT * FROM journal_entries WHERE autoDelete = 1")
    suspend fun getAutoDeleteEntries(): List<JournalEntryEntity>
}

