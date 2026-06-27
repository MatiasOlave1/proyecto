package com.camposocampoolavevargas.proyecto.diario.di

import com.camposocampoolavevargas.proyecto.diario.data.local.dao.EntradaDiarioDao
import com.camposocampoolavevargas.proyecto.diario.data.repository.DiarioRepository
import com.camposocampoolavevargas.proyecto.diario.data.repository.DiarioRepositoryImpl
import com.camposocampoolavevargas.proyecto.diario.domain.usecase.AutoEliminarEntradasUseCase
import com.camposocampoolavevargas.proyecto.diario.domain.usecase.DiarioUseCases
import com.camposocampoolavevargas.proyecto.diario.domain.usecase.EliminarEntradaUseCase
import com.camposocampoolavevargas.proyecto.diario.domain.usecase.GuardarEntradaUseCase
import com.camposocampoolavevargas.proyecto.diario.domain.usecase.ObtenerHistorialDiarioUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DiarioModule {

    @Provides
    @Singleton
    fun provideDiarioRepository(dao: EntradaDiarioDao): DiarioRepository =
        DiarioRepositoryImpl(dao)

    @Provides
    fun provideGuardarEntradaUseCase(repo: DiarioRepository) =
        GuardarEntradaUseCase(repo)

    @Provides
    fun provideObtenerHistorialUseCase(repo: DiarioRepository) =
        ObtenerHistorialDiarioUseCase(repo)

    @Provides
    fun provideEliminarEntradaUseCase(repo: DiarioRepository) =
        EliminarEntradaUseCase(repo)

    @Provides
    fun provideAutoEliminarEntradasUseCase(repo: DiarioRepository) =
        AutoEliminarEntradasUseCase(repo)

    @Provides
    fun provideDiarioUseCases(
        guardar: GuardarEntradaUseCase,
        obtener: ObtenerHistorialDiarioUseCase,
        eliminar: EliminarEntradaUseCase,
        autoEliminar: AutoEliminarEntradasUseCase
    ) = DiarioUseCases(guardar, obtener, eliminar, autoEliminar)
}