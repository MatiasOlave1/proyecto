package com.camposocampoolavevargas.proyecto.di

import android.content.Context
import com.camposocampoolavevargas.proyecto.data.local.AppDatabase
import com.camposocampoolavevargas.proyecto.data.local.dao.AchievementDao
import com.camposocampoolavevargas.proyecto.data.local.dao.CircadianAlertDao
import com.camposocampoolavevargas.proyecto.data.local.dao.JournalEntryDao
import com.camposocampoolavevargas.proyecto.data.local.dao.SleepRecordDao
import com.camposocampoolavevargas.proyecto.data.local.dao.StreakDataDao
import com.camposocampoolavevargas.proyecto.data.local.dao.UserDao
import com.camposocampoolavevargas.proyecto.data.local.dao.WeeklyGoalDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency Injection Module for local database classes using Dagger Hilt.
 * Exposes singletons of [AppDatabase] and all 7 individual DAOs.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideSleepRecordDao(database: AppDatabase): SleepRecordDao {
        return database.sleepRecordDao()
    }

    @Provides
    @Singleton
    fun provideWeeklyGoalDao(database: AppDatabase): WeeklyGoalDao {
        return database.weeklyGoalDao()
    }

    @Provides
    @Singleton
    fun provideStreakDataDao(database: AppDatabase): StreakDataDao {
        return database.streakDataDao()
    }

    @Provides
    @Singleton
    fun provideAchievementDao(database: AppDatabase): AchievementDao {
        return database.achievementDao()
    }

    @Provides
    @Singleton
    fun provideJournalEntryDao(database: AppDatabase): JournalEntryDao {
        return database.journalEntryDao()
    }

    @Provides
    @Singleton
    fun provideCircadianAlertDao(database: AppDatabase): CircadianAlertDao {
        return database.circadianAlertDao()
    }
}

