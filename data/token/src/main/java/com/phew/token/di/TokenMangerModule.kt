package com.phew.token.di

import com.phew.domain.interceptor.InterceptorManger
import com.phew.token.InterceptorMangerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TokenMangerModule {
    @Binds
    @Singleton
    abstract fun bindTokenManger(
        impl: InterceptorMangerImpl
    ): InterceptorManger
}