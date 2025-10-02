package com.phew.repository.di

import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.NetworkRepository
import com.phew.domain.repository.network.CardFeedRepository
import com.phew.network.Http
import com.phew.network.retrofit.FeedHttp
import com.phew.repository.DeviceRepositoryImpl
import com.phew.repository.NetworkRepositoryImpl
import dagger.Binds
import com.phew.repository.network.CardFeedRepositoryImpl
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNetworkRepository(
        impl: NetworkRepositoryImpl,
    ): NetworkRepository

    @Binds
    @Singleton
    abstract fun bindDeviceRepository(
        impl: DeviceRepositoryImpl,
    ): DeviceRepository

    @Binds
    @Singleton
    abstract fun bindCardFeedRepository(
        impl: CardFeedRepositoryImpl
    ): CardFeedRepository
}