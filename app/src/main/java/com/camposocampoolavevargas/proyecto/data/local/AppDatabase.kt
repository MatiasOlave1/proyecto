package com.camposocampoolavevargas.proyecto.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
import com.camposocampoolavevargas.proyecto.relajacion.data.local.entity.SesionRelajacionEntity
import com.camposocampoolavevargas.proyecto.relajacion.data.local.dao.SesionRelajacionDao

/**
 * Main Room Database configuration for the DormiBienU application.
 * Manages 8 entities and declares abstract getters for all corresponding DAOs.
 */
@Database(
    entities = [
        UserEntity::class,
        SleepRecordEntity::class,
        WeeklyGoalEntity::class,
        StreakDataEntity::class,
        AchievementEntity::class,
        JournalEntryEntity::class,
        CircadianAlertEntity::class,
        SesionRelajacionEntity::class
    ],
    version = 4,
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
    abstract fun sesionRelajacionDao(): SesionRelajacionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Migration from version 3 to 4: adds sesion_relajacion table
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS sesion_relajacion (
                        uuid TEXT PRIMARY KEY NOT NULL,
                        userId TEXT NOT NULL,
                        tipo TEXT NOT NULL,
                        subtipo TEXT NOT NULL,
                        duracionSegundos INTEGER NOT NULL,
                        completada INTEGER NOT NULL,
                        audioActivo INTEGER NOT NULL,
                        iniciadoEn TEXT NOT NULL,
                        finalizadoEn TEXT,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

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
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

