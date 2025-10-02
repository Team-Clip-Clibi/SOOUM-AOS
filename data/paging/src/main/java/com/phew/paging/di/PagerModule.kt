package com.phew.paging.di

import com.phew.domain.repository.PagerRepository
import com.phew.paging.PagerRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PagerModule {
    @Binds
    @Singleton
    abstract fun providePagerRepository(
        impl : PagerRepositoryImpl
    ) : PagerRepository
}