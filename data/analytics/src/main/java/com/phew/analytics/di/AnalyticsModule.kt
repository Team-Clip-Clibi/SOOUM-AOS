package com.phew.analytics.di

import com.phew.analytics.AppEventLog
import com.phew.analytics.AppEventLogImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {
    @Binds
    @Singleton
    abstract fun bindAppEventLog(
        impl: AppEventLogImpl,
    ): AppEventLog
}