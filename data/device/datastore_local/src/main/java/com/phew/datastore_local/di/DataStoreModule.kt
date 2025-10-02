package com.phew.datastore_local.di

import android.content.Context
import com.phew.datastore_local.BuildConfig
import com.phew.datastore_local.DataStore
import com.phew.datastore_local.DataStoreImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context,
    ): DataStore = DataStoreImpl(context = context, fileName = BuildConfig.SOOUM_FILE_NAME)
}