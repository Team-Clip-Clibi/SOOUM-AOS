package com.phew.presentation.write.utils

import android.content.Context


internal fun getDrawableNameByResId(context: Context, resId: Int): String {
    return context.resources.getResourceEntryName(resId)
}