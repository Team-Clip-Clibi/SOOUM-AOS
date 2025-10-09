package com.phew.core_design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

private val SooumLightBackgroundTheme = SooumBackgroundTheme(color = SooumLightColors.background)
private val SooumDarkBackgroundTheme = SooumBackgroundTheme(color = SooumDarkColors.background)

@Composable
fun SooumTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val currentDensity = LocalDensity.current
    val background = if (darkTheme) SooumDarkBackgroundTheme else SooumLightBackgroundTheme
    val colors = if (darkTheme) SooumDarkColors else SooumLightColors

    CompositionLocalProvider(
        LocalDensity provides Density(currentDensity.density, fontScale = 1f),
        LocalBackground provides background,
        LocalColors provides colors,
        content = content
    )
}

/**
 *  Custom Theme
 */
object SooumTheme {
    val colors: SooumColors
        @Composable
        get() = LocalColors.current

    val background: SooumBackgroundTheme
        @Composable
        get() = LocalBackground.current
}

@OptIn(ExperimentalMaterial3Api::class)
val SooumRippleConfiguration =
    RippleConfiguration(/*isEnabled = false,*/ color = androidx.compose.ui.graphics.Color.Transparent, rippleAlpha = null)