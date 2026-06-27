package com.camposocampoolavevargas.proyecto.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.camposocampoolavevargas.proyecto.data.local.entity.SleepRecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the "sleep_records" table.
 */
@Dao
interface SleepRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: SleepRecordEntity)

    @Update
    suspend fun updateRecord(record: SleepRecordEntity)

    @Query("DELETE FROM sleep_records WHERE recordId = :recordId")
    suspend fun deleteRecord(recordId: String)

    @Query("SELECT * FROM sleep_records WHERE userId = :userId ORDER BY date DESC")
    fun getRecordsByUserId(userId: String): Flow<List<SleepRecordEntity>>

    @Query("SELECT * FROM sleep_records WHERE userId = :userId ORDER BY date DESC")
    suspend fun getRecordsByUserIdDirect(userId: String): List<SleepRecordEntity>

    @Query("SELECT * FROM sleep_records WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getRecordsByDateRange(userId: String, startDate: String, endDate: String): Flow<List<SleepRecordEntity>>

    @Query("SELECT * FROM sleep_records WHERE userId = :userId AND date = :date LIMIT 1")
    fun getRecordByDate(userId: String, date: String): Flow<SleepRecordEntity?>

    @Query("SELECT * FROM sleep_records WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getRecordByDateDirect(userId: String, date: String): SleepRecordEntity?

    @Query("SELECT * FROM sleep_records WHERE recordId = :recordId LIMIT 1")
    suspend fun getRecordById(recordId: String): SleepRecordEntity?

    @Query("SELECT * FROM sleep_records WHERE userId = :userId AND syncStatus = 'PENDING'")
    suspend fun getPendingSyncRecords(userId: String): List<SleepRecordEntity>

    @Query("SELECT * FROM sleep_records WHERE userId = :userId ORDER BY date DESC LIMIT :limit")
    suspend fun getRecordsForStreakCheck(userId: String, limit: Int): List<SleepRecordEntity>
}

