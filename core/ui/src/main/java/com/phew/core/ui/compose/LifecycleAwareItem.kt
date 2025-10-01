package com.phew.core.ui.compose

import androidx.lifecycle.Lifecycle

data class LifecycleAwareItem(
    val uniqueId: String,
    val type: ComposableType,
    val event: Lifecycle.Event
)
