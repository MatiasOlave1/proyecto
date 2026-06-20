package com.camposocampoolavevargas.proyecto.di

import com.camposocampoolavevargas.proyecto.service.notification.DisconnectAlarmScheduler
import com.camposocampoolavevargas.proyecto.service.notification.DisconnectAlarmSchedulerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {

    @Binds
    @Singleton
    abstract fun bindDisconnectAlarmScheduler(
        disconnectAlarmSchedulerImpl: DisconnectAlarmSchedulerImpl
    ): DisconnectAlarmScheduler
}
