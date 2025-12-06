package com.phew.presentation.write.utils

import android.content.Context


internal fun getDrawableNameByResId(context: Context, resId: Int): String {
    return context.resources.getResourceEntryName(resId)
}

enum class WriteErrorCase {
    ERROR_RESTRICT,
    ERROR_DELETE,
    ERROR_NETWORK,
    ERROR_JOB_FAIL,
    NONE
}