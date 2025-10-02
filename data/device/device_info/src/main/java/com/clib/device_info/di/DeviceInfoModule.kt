package com.clib.device_info.di

import android.content.Context
import com.clib.device_info.DeviceInfo
import com.clib.device_info.DeviceInfoImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DeviceInfoModule {
    @Provides
    @Singleton
    fun provideDeviceInfo(@ApplicationContext context: Context): DeviceInfo = DeviceInfoImpl(
        context = context
    )
}