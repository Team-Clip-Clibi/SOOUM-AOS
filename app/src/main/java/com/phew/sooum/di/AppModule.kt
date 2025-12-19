package com.phew.sooum.di

import android.content.ContentResolver
import android.content.Context
import com.microsoft.clarity.ClarityConfig
import com.microsoft.clarity.models.ApplicationFramework
import com.microsoft.clarity.models.LogLevel
import com.phew.sooum.BuildConfig
import com.phew.core_common.AppVersion
import com.phew.core_common.IsDebug
import com.phew.core_common.clarity.ClarityInterface
import com.phew.core_common.di.ApplicationScope
import com.phew.sooum.clarity.ClarityManger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

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

    @Provides
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @Provides
    @Singleton
    fun provideClarityConfig(): ClarityConfig {
        return ClarityConfig(
            projectId = BuildConfig.CLARITY_PROJECT_ID,
            logLevel = LogLevel.None,
            applicationFramework = ApplicationFramework.Native
        )
    }

    @Provides
    @Singleton
    fun provideClarityInterface(
        manger: ClarityManger,
    ): ClarityInterface {
        return manger
    }
}