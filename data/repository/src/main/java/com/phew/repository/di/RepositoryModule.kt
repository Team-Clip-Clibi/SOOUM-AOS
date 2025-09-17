package com.phew.repository.di

import com.phew.domain.repository.NetworkRepository
import com.phew.network.Http
import com.phew.repository.NetworkRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {
    @Provides
    @Singleton
    fun provideNetworkRepository(
        http: Http
    ) : NetworkRepository{
        return NetworkRepositoryImpl(http)
    }
}