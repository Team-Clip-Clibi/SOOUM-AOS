package com.phew.core_design.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color


/**
 * Neutral Colors (기존 NeutralColor 기반)
 */
val WHITE = Color(0xFFFFFFFF)
val GRAY_100 = Color(0xFFF5F7FA)
val GRAY_200 = Color(0xFFE4EAF1)
val GRAY_300 = Color(0xFFBFC9D3)
val GRAY_400 = Color(0xFF919DA9)
val GRAY_500 = Color(0xFF5D6369)
val GRAY_600 = Color(0xFF3A3F44)
val BLACK = Color(0xFF212121)

/**
 * Primary Colors (기존 Primary 기반)
 */
val LIGHT_1 = Color(0xFFD7F1F9)
val LIGHT_2 = Color(0xFF8CE1F4)
val MAIN = Color(0xFF20C6EC)
val DARK = Color(0xFF07ABD0)

/**
 * Success Colors (기존 Success 기반)
 */
val L_GREEN = Color(0xFFD3F5EB)
val M_GREEN = Color(0xFF009262)

/**
 * Warning Colors (기존 Warning 기반)
 */
val L_YELLOW = Color(0xFFFFF0D7)
val M_YELLOW = Color(0xFFFFB240)

/**
 * Danger Colors (기존 Danger 기반)
 */
val L_RED = Color(0xFFFFE1DF)
val M_RED = Color(0xFFEE3A26)

/**
 * Opacity & Unknown Colors (기존 값 유지)
 */
val blackSmallColor = Color(0x99000000)
val unknownColor = Color(0x1AABBED1)

/**
 * Hex 값을 Sooum 에서 사용할 수 있는 Color 로 변경한다.
 */
fun hexToColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        BLACK
    }
}

/**
 * Sooum Colors - Semantic Color System
 */
@Immutable
data class SooumColors(
    val primary: Color = Color.Unspecified,
    val onPrimary: Color = Color.Unspecified,
    val secondary: Color = Color.Unspecified,
    val onSecondary: Color = Color.Unspecified,
    val tertiary: Color = Color.Unspecified,
    val onTertiary: Color = Color.Unspecified,
    val negative: Color = Color.Unspecified,
    val onNegative: Color = Color.Unspecified,
    val positive: Color = Color.Unspecified,
    val onPositive: Color = Color.Unspecified,
    val warning: Color = Color.Unspecified,
    val onWarning: Color = Color.Unspecified,
    val mainSurface: List<Color> = listOf(Color.Unspecified),
    val depthASurface: Color = Color.Unspecified,
    val depthBSurface: Color = Color.Unspecified,
    val toastSurface: Color = Color.Unspecified,
    val background: Color = Color.Unspecified,
    val onBackground: Color = Color.Unspecified,
    val surface: Color = Color.Unspecified,
    val onSurface: Color = Color.Unspecified
)

val SooumLightColors = SooumColors(
    primary = MAIN, // 기존 Primary.MAIN
    onPrimary = WHITE,
    secondary = LIGHT_2, // 기존 Primary.LIGHT_2
    onSecondary = BLACK,
    tertiary = LIGHT_1, // 기존 Primary.LIGHT_1
    onTertiary = BLACK,
    negative = M_RED, // 기존 Danger.M_RED
    onNegative = WHITE,
    positive = M_GREEN, // 기존 Success.M_GREEN
    onPositive = WHITE,
    warning = M_YELLOW, // 기존 Warning.M_YELLOW
    onWarning = WHITE,
    mainSurface = listOf(WHITE, GRAY_100, GRAY_200),
    depthASurface = GRAY_300,
    depthBSurface = GRAY_400,
    toastSurface = GRAY_600,
    background = WHITE, // 기존 NeutralColor.WHITE
    onBackground = BLACK, // 기존 NeutralColor.BLACK
    surface = WHITE,
    onSurface = BLACK
)

val SooumDarkColors = SooumColors(
    primary = MAIN,
    onPrimary = WHITE,
    secondary = LIGHT_2,
    onSecondary = BLACK,
    tertiary = LIGHT_1,
    onTertiary = BLACK,
    negative = M_RED,
    onNegative = WHITE,
    positive = M_GREEN,
    onPositive = WHITE,
    warning = M_YELLOW,
    onWarning = WHITE,
    mainSurface = listOf(BLACK, GRAY_600, GRAY_500),
    depthASurface = GRAY_400,
    depthBSurface = GRAY_300,
    toastSurface = GRAY_200,
    background = BLACK, // Dark mode background
    onBackground = WHITE, // Dark mode text
    surface = GRAY_600,
    onSurface = WHITE
)

internal val LocalColors = staticCompositionLocalOf { SooumColors() }