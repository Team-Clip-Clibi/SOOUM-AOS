package com.phew.repository.di

import com.phew.device.dataStore.DataStore
import com.phew.device.device.Device
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.NetworkRepository
import com.phew.domain.repository.network.CardFeedRepository
import com.phew.network.Http
import com.phew.network.retrofit.FeedHttp
import com.phew.repository.DeviceRepositoryImpl
import com.phew.repository.NetworkRepositoryImpl
import com.phew.repository.network.CardFeedRepositoryImpl
import dagger.Binds
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
        http: Http,
    ): NetworkRepository {
        return NetworkRepositoryImpl(http)
    }

    @Provides
    @Singleton
    fun provideDeviceRepository(
        device: Device,
        dataStore: DataStore,
    ): DeviceRepository {
        return DeviceRepositoryImpl(device, dataStore)
    }

    @Provides
    @Singleton
    fun provideCardFeedRepository(
        feedHttp: FeedHttp
    ): CardFeedRepository {
        return CardFeedRepositoryImpl(feedHttp)
    }
}