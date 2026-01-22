package com.phew.sooum.clarity

import com.microsoft.clarity.Clarity
import com.phew.core_common.clarity.ClarityInterface
import com.phew.core_common.log.SooumLog
import com.phew.sooum.BuildConfig
import javax.inject.Inject

class ClarityManger @Inject constructor() : ClarityInterface {
    override fun pause() {
        if (BuildConfig.DEBUG) {
            SooumLog.d(msg = "Clarity not support DEBUG mode")
            return
        }
        Clarity.pause()
    }

    override fun resume() {
        if (BuildConfig.DEBUG) {
            SooumLog.d(msg = "Clarity not support DEBUG mode")
            return
        }
        Clarity.resume()
    }
}