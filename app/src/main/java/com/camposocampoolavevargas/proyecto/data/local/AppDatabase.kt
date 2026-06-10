package com.camposocampoolavevargas.proyecto.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.camposocampoolavevargas.proyecto.data.local.dao.AchievementDao
import com.camposocampoolavevargas.proyecto.data.local.dao.CircadianAlertDao
import com.camposocampoolavevargas.proyecto.data.local.dao.JournalEntryDao
import com.camposocampoolavevargas.proyecto.data.local.dao.SleepRecordDao
import com.camposocampoolavevargas.proyecto.data.local.dao.StreakDataDao
import com.camposocampoolavevargas.proyecto.data.local.dao.UserDao
import com.camposocampoolavevargas.proyecto.data.local.dao.WeeklyGoalDao
import com.camposocampoolavevargas.proyecto.data.local.entity.AchievementEntity
import com.camposocampoolavevargas.proyecto.data.local.entity.CircadianAlertEntity
import com.camposocampoolavevargas.proyecto.data.local.entity.JournalEntryEntity
import com.camposocampoolavevargas.proyecto.data.local.entity.SleepRecordEntity
import com.camposocampoolavevargas.proyecto.data.local.entity.StreakDataEntity
import com.camposocampoolavevargas.proyecto.data.local.entity.UserEntity
import com.camposocampoolavevargas.proyecto.data.local.entity.WeeklyGoalEntity

/**
 * Main Room Database configuration for the DormiBienU application.
 * Manages 7 entities and declares abstract getters for all corresponding DAOs.
 */
@Database(
    entities = [
        UserEntity::class,
        SleepRecordEntity::class,
        WeeklyGoalEntity::class,
        StreakDataEntity::class,
        AchievementEntity::class,
        JournalEntryEntity::class,
        CircadianAlertEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun sleepRecordDao(): SleepRecordDao
    abstract fun weeklyGoalDao(): WeeklyGoalDao
    abstract fun streakDataDao(): StreakDataDao
    abstract fun achievementDao(): AchievementDao
    abstract fun journalEntryDao(): JournalEntryDao
    abstract fun circadianAlertDao(): CircadianAlertDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the singleton instance of the database, building it if it doesn't exist.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dormibienU_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

