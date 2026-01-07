package com.phew.sooum.clarity

import android.app.Application
import com.microsoft.clarity.Clarity
import com.microsoft.clarity.ClarityConfig
import com.phew.core_common.log.SooumLog
import com.phew.sooum.BuildConfig
import javax.inject.Inject

class ClarityInitializer @Inject constructor(
    private val application: Application,
    private val config: ClarityConfig,
) {
    fun init() {
        if (BuildConfig.DEBUG) {
            SooumLog.d(msg = "Clarity not support DEBUG mode")
            return
        }
        Clarity.initialize(application, config)
    }
}