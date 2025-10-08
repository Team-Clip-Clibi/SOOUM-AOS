package com.phew.repository.di

import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.network.NotifyRepository
import com.phew.domain.repository.network.CardFeedRepository
import com.phew.repository.DeviceRepositoryImpl
import com.phew.repository.NotifyRepositoryImpl
import dagger.Binds
import com.phew.repository.network.CardFeedRepositoryImpl
import com.phew.repository.network.MockCardFeedRepositoryImpl
import com.phew.core_common.IsDebug
import com.phew.domain.repository.network.SignUpRepository
import com.phew.domain.repository.network.SplashRepository
import com.phew.repository.network.SignUpRepositoryImpl
import com.phew.repository.network.SplashRepositoryImpl
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
        impl: NotifyRepositoryImpl,
    ): NotifyRepository

    @Binds
    @Singleton
    abstract fun bindDeviceRepository(
        impl: DeviceRepositoryImpl,
    ): DeviceRepository

    @Binds
    @Singleton
    abstract fun bindSplashRepository(impl: SplashRepositoryImpl): SplashRepository

    @Binds
    @Singleton
    abstract fun bindSignUpRepository(impl: SignUpRepositoryImpl): SignUpRepository

    companion object {
        @Provides
        @Singleton
        fun provideCardFeedRepository(
            @IsDebug isDebug: Boolean,
            realImpl: CardFeedRepositoryImpl,
            mockImpl: MockCardFeedRepositoryImpl
        ): CardFeedRepository {
            return realImpl
            // 임시로 mock 데이터로 확인하고 싶을떄 사용
//            return if (isDebug) {
//                mockImpl
//            } else {
//                realImpl
//            }
        }
    }
}