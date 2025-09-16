package com.phew.network.di

import com.phew.network.Http
import com.phew.network.RetrofitFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class NetworkModule {
    companion object {
        @Singleton
        @Provides
        fun provideRetrofit(): Retrofit = RetrofitFactory.create()

        @Singleton
        @Provides
        fun provideHttp(retrofit: Retrofit): Http = retrofit.create(Http::class.java)
    }
}