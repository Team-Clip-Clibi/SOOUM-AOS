package com.phew.token.di

import com.phew.domain.token.TokenManger
import com.phew.token.TokenMangerImpl
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
        impl: TokenMangerImpl
    ): TokenManger
}