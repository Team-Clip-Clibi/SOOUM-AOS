package com.phew.core_design.theme

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Stable
class SooumBackgroundTheme(
    color: Color = Color.Unspecified
) {
    var color by mutableStateOf(color)
        private set

    fun update(other: SooumBackgroundTheme) {
        color = other.color
    }

    fun copy() : SooumBackgroundTheme = SooumBackgroundTheme(
        color = color
    )
}

/**
 * A composition local for [SooumBackgroundTheme].
 */
internal val LocalBackground = staticCompositionLocalOf { SooumBackgroundTheme() }
