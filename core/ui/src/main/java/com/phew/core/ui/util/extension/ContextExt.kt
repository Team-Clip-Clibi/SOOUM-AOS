package com.phew.core.ui.util.extension

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Activity 찾기
 * + LocalContext.current as Activity 와 동일. 단, 더 강하게 타입 체크하여 찾음.
 */
fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }

    throw IllegalStateException("no activity")
}
