package com.phew.core.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.Lifecycle
import com.phew.core.ui.util.extension.LocalLifecycleAwareComposables

@Stable
class ComposableVisibleState {
    var enabled = mutableStateOf(value = false)
        private set

    fun setEnabled(enabled: Boolean) {
        this.enabled.value = enabled
    }
}

@Composable
fun LifecycleAwareComposableRegister(
    uniqueId: String,
    type: ComposableType,
    visibleState: ComposableVisibleState,
    lifecycleAwareComposables: LifecycleAwareComposables = LocalLifecycleAwareComposables.current
) {
//    ComposableHostStateListener(visibleState = visibleState) { isEnabled ->
//        lifecycleAwareComposables.register(
//            uniqueId = uniqueId,
//            type = type,
//            event = if (isEnabled) Lifecycle.Event.ON_RESUME else Lifecycle.Event.ON_DESTROY
//        )
//    }
}
