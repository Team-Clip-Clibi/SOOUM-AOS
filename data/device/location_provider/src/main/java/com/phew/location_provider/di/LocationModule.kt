package com.phew.location_provider.di

import android.content.Context
import com.phew.location_provider.LocationProvider
import com.phew.location_provider.LocationProviderImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {
    @Provides
    @Singleton
    fun provideLocation(
        @ApplicationContext context: Context,
    ): LocationProvider = LocationProviderImpl(context = context)
}