package com.camposocampoolavevargas.proyecto.di

import com.camposocampoolavevargas.proyecto.data.repository.WeeklyGoalRepository
import com.camposocampoolavevargas.proyecto.data.repository.WeeklyGoalRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWeeklyGoalRepository(
        weeklyGoalRepositoryImpl: WeeklyGoalRepositoryImpl
    ): WeeklyGoalRepository
}
