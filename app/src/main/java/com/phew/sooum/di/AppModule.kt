package com.phew.sooum.di

import com.phew.sooum.BuildConfig
import com.phew.splash.AppVersion
import com.phew.splash.IsDebug
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @IsDebug
    fun provideIsDebug(): Boolean {
        return BuildConfig.DEBUG
    }

    @Provides
    @AppVersion
    fun provideAppVersion(): String {
        return BuildConfig.VERSION_NAME
    }
}