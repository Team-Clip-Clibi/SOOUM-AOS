package com.phew.device_haptic.di

import android.content.Context
import com.phew.device_haptic.HapticProvider
import com.phew.device_haptic.HapticProviderImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DeviceHapticModule {
    @Provides
    @Singleton
    fun provideDeviceHaptic(@ApplicationContext context: Context): HapticProvider =
        HapticProviderImpl(context = context)
}