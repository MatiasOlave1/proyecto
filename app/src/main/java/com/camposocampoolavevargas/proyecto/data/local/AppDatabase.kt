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
    version = 1,
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
                // Future Migration Strategy:
                // To modify the database schema in subsequent app updates without losing existing user data:
                // 1. Increment the version number in the @Database annotation (e.g. version = 2).
                // 2. Define a Migration object implementing the changes:
                //    val MIGRATION_1_2 = object : Migration(1, 2) {
                //        override fun migrate(db: SupportSQLiteDatabase) {
                //            // Example: db.execSQL("ALTER TABLE users ADD COLUMN age INTEGER DEFAULT 0 NOT NULL")
                //        }
                //    }
                // 3. Register the migration object on this database builder:
                //    .addMigrations(MIGRATION_1_2)
                // 4. In cases where data loss is acceptable during dev, .fallbackToDestructiveMigration() can be used temporarily.
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

