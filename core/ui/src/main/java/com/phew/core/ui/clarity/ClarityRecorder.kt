package com.phew.core.ui.clarity

import androidx.compose.runtime.staticCompositionLocalOf
import com.phew.core_common.clarity.ClarityInterface

val LocalSessionRecorder = staticCompositionLocalOf<ClarityInterface> {
    object : ClarityInterface {
        override fun pause() {}

        override fun resume() {}

    }
}