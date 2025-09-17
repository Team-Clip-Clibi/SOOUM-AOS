package com.phew.device.di

import android.content.Context
import com.phew.device.device.Device
import com.phew.device.device.DeviceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DeviceModule {
    @Provides
    @Singleton
    fun provideDevice(
        @ApplicationContext context: Context,
    ): Device = DeviceImpl(context)
}