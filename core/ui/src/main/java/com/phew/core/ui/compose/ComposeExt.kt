package com.phew.core.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.phew.core.ui.util.extension.LocalLifecycleAwareComposables

/**
 * Lifecycle-Aware Composable 관리 대상으로 등록
 * + Composable 함수의 선두에서 호출 할 것
 * + [uniqueId] Composable 의 고유 ID
 * + [type] Composable 의 Type
 */
@Composable
fun LifecycleAwareComposableRegister(
    uniqueId: String,
    type: ComposableType,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    lifecycleAwareComposables: LifecycleAwareComposables = LocalLifecycleAwareComposables.current
) {
    ComposableLifecycleEventListener(lifecycleOwner = lifecycleOwner) { _, event ->
        lifecycleAwareComposables.register(uniqueId = uniqueId, type = type, event = event)
    }
}


/**
 * Composable 의 Lifecycle Event 을 수신한다.
 * + Lifecycle 의 값이 변경될 때, [onEvent] 가 호출된다.
 */
@Composable
fun ComposableLifecycleEventListener(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onEvent:(LifecycleOwner, Lifecycle.Event) ->Unit
) {
    DisposableEffect(lifecycleOwner){
        val observer = LifecycleEventObserver { source, event->
            onEvent(source,event)
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

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
    ComposableHostStateListener(visibleState = visibleState) { isEnabled ->
        lifecycleAwareComposables.register(
            uniqueId = uniqueId,
            type = type,
            event = if (isEnabled) Lifecycle.Event.ON_RESUME else Lifecycle.Event.ON_DESTROY
        )
    }
}


@Composable
private fun ComposableHostStateListener(
    visibleState: ComposableVisibleState,
    onChanged: (Boolean) -> Unit
) {
    LaunchedEffect(key1 = visibleState.enabled.value) {
        onChanged.invoke(visibleState.enabled.value)
    }
}

