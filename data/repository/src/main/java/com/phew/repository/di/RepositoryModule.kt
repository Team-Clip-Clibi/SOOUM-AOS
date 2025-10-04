package com.phew.repository.di

import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.NetworkRepository
import com.phew.domain.repository.network.CardFeedRepository
import com.phew.repository.DeviceRepositoryImpl
import com.phew.repository.NetworkRepositoryImpl
import dagger.Binds
import com.phew.repository.network.CardFeedRepositoryImpl
import com.phew.repository.network.MockCardFeedRepositoryImpl
import com.phew.core_common.IsDebug
import dagger.Module
import dagger.Provides
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

    companion object {
        @Provides
        @Singleton
        fun provideCardFeedRepository(
            @IsDebug isDebug: Boolean,
            realImpl: CardFeedRepositoryImpl,
            mockImpl: MockCardFeedRepositoryImpl
        ): CardFeedRepository {
            return if (isDebug) {
                mockImpl
            } else {
                realImpl
            }
        }
    }
}