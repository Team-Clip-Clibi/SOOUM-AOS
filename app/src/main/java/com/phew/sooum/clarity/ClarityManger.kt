package com.phew.sooum.clarity

import com.microsoft.clarity.Clarity
import com.phew.core_common.clarity.ClarityInterface
import javax.inject.Inject

class ClarityManger @Inject constructor() : ClarityInterface {
    override fun pause() {
        Clarity.pause()
    }

    override fun resume() {
        Clarity.resume()
    }
}