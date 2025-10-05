package com.phew.core.ui.compose

import androidx.lifecycle.Lifecycle
import javax.inject.Inject
import javax.inject.Singleton


// Todo 추후 수정
@Singleton
class LifecycleAwareComposablesImpl @Inject constructor(

) : LifecycleAwareComposables {
    override fun getItem(uniqueId: String): LifecycleAwareItem? {
        TODO("Not yet implemented")
    }

    override fun getItems(type: ComposableType): List<LifecycleAwareItem> {
        TODO("Not yet implemented")
    }

    override fun isVisible(item: LifecycleAwareItem): Boolean {
        TODO("Not yet implemented")
    }

    override fun register(
        uniqueId: String,
        type: ComposableType,
        event: Lifecycle.Event
    ) {
        TODO("Not yet implemented")
    }

}