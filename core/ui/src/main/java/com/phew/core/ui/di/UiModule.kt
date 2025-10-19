package com.phew.core.ui.di

import com.phew.core.ui.compose.LifecycleAwareComposables
import com.phew.core.ui.compose.LifecycleAwareComposablesImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class UiModule {
    @Binds
    abstract fun bindLifecycleAwareComposables(
        lifecycleAwareComposables: LifecycleAwareComposablesImpl
    ): LifecycleAwareComposables
}