package com.phew.core.ui.util.extension

import androidx.compose.runtime.staticCompositionLocalOf
import com.phew.core.ui.compose.LifecycleAwareComposables
import com.phew.core.ui.compose.LifecycleAwareComposablesImpl

/**
 * 생명 주기 등록된 Composables
 */
val LocalLifecycleAwareComposables = staticCompositionLocalOf<LifecycleAwareComposables> {
    LifecycleAwareComposablesImpl()
}