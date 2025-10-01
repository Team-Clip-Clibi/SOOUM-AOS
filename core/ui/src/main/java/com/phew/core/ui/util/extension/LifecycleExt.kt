package com.phew.core.ui.util.extension

import androidx.compose.runtime.staticCompositionLocalOf
import com.phew.core.ui.compose.LifecycleAwareComposables

/**
 * 생명 주기 등록된 Composables
 */
val LocalLifecycleAwareComposables = staticCompositionLocalOf<LifecycleAwareComposables> {
    LifecycleAwareComposablesImpl()
}