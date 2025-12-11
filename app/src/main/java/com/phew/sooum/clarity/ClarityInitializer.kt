package com.phew.sooum.clarity

import android.app.Application
import android.util.Log
import com.microsoft.clarity.Clarity
import com.microsoft.clarity.ClarityConfig
import com.phew.sooum.BuildConfig
import javax.inject.Inject

class ClarityInitializer @Inject constructor(
    private val application: Application,
    private val config: ClarityConfig,
) {
    fun init() {
        if (BuildConfig.DEBUG) {
            Log.e(this.javaClass.packageName, "Clarity is not support debug mode")
            return
        }
        if (BuildConfig.CLARITY_PROJECT_ID.trim().isEmpty()) {
            Log.e(this.javaClass.packageName, "Error Clarity id is null")
            return
        }
        Clarity.initialize(application, config)
    }
}