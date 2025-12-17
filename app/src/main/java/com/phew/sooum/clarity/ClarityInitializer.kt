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
        Clarity.initialize(application, config)
    }
}