package com.phew.core_design.typography

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.phew.core_design.R
import com.phew.core_design.NeutralColor

@Composable
private fun fixFontSize(
    fontSize: TextUnit,
    lineHeight: TextUnit,
    fontFamily: FontFamily,
    fontWeight: FontWeight,
    color: Color,
    letterSpacing: TextUnit = 0.sp
): TextStyle {
    val fontScale = LocalDensity.current.fontScale
    val fixedFontSize = (fontSize.value / fontScale).sp
    val fixedLineHeight = (lineHeight.value / fontScale).sp

    return TextStyle(
        fontSize = fixedFontSize,
        lineHeight = fixedLineHeight,
        fontFamily = fontFamily,
        fontWeight = fontWeight,
        color = color,
        letterSpacing = letterSpacing
    )
}

object FontTextStyle {
    
    // 기본 폰트 패밀리 (Default - regular.otf)
    private val DefaultFamily = FontFamily(Font(R.font.regular))
    
    val DEFAULT_BUTTON
        @Composable get() = fixFontSize(
            fontSize = 16.sp,
            lineHeight = 24.sp, // 16 * 150%
            fontFamily = DefaultFamily,
            fontWeight = FontWeight.Medium,
            color = NeutralColor.BLACK,
            letterSpacing = (-0.4).sp // -2.5%
        )
    
    val DEFAULT_CARD
        @Composable get() = fixFontSize(
            fontSize = 14.sp,
            lineHeight = 21.sp, // 14 * 150%
            fontFamily = DefaultFamily,
            fontWeight = FontWeight.Medium,
            color = NeutralColor.WHITE,
            letterSpacing = (-0.35).sp // -2.5%
        )
    
    val DEFAULT_TAG
        @Composable get() = fixFontSize(
            fontSize = 12.sp,
            lineHeight = 18.sp, // 12 * 150%
            fontFamily = DefaultFamily,
            fontWeight = FontWeight.Medium,
            color = NeutralColor.BLACK,
            letterSpacing = (-0.3).sp // -2.5%
        )
    
    val DEFAULT_PROFILE
        @Composable get() = fixFontSize(
            fontSize = 5.sp,
            lineHeight = 7.5.sp, // 5 * 150%
            fontFamily = DefaultFamily,
            fontWeight = FontWeight.Medium,
            color = NeutralColor.BLACK,
            letterSpacing = (-0.125).sp // -2.5%
        )
    
    // 리디바탕 (RIDIBang - ridibatang.otf)
    private val RidibatangFamily = FontFamily(Font(R.font.ridibatang))
    
    val RIDIBATANG_BUTTON
        @Composable get() = fixFontSize(
            fontSize = 15.sp,
            lineHeight = 22.5.sp, // 15 * 150%
            fontFamily = RidibatangFamily,
            fontWeight = FontWeight.Normal,
            color = NeutralColor.BLACK,
            letterSpacing = (-0.375).sp // -2.5%
        )
    
    val RIDIBATANG_CARD
        @Composable get() = fixFontSize(
            fontSize = 13.sp,
            lineHeight = 19.5.sp, // 13 * 150%
            fontFamily = RidibatangFamily,
            fontWeight = FontWeight.Normal,
            color = NeutralColor.WHITE,
            letterSpacing = (-0.325).sp // -2.5%
        )
    
    val RIDIBATANG_TAG
        @Composable get() = fixFontSize(
            fontSize = 11.sp,
            lineHeight = 16.5.sp, // 11 * 150%
            fontFamily = RidibatangFamily,
            fontWeight = FontWeight.Normal,
            color = NeutralColor.BLACK,
            letterSpacing = (-0.275).sp // -2.5%
        )
    
    // 윤우체 (Yoon - yoon.ttf)
    private val YoonFamily = FontFamily(Font(R.font.yoon))
    
    val YOON_BUTTON
        @Composable get() = fixFontSize(
            fontSize = 20.sp,
            lineHeight = 22.sp, // 20 * 110%
            fontFamily = YoonFamily,
            fontWeight = FontWeight.Normal,
            color = NeutralColor.BLACK,
            letterSpacing = 0.sp // 0%
        )
    
    val YOON_CARD
        @Composable get() = fixFontSize(
            fontSize = 18.sp,
            lineHeight = 19.8.sp, // 18 * 110%
            fontFamily = YoonFamily,
            fontWeight = FontWeight.Normal,
            color =NeutralColor.WHITE,
            letterSpacing = 0.sp // 0%
        )
    
    val YOON_TAG
        @Composable get() = fixFontSize(
            fontSize = 16.sp,
            lineHeight = 17.6.sp, // 16 * 110%
            fontFamily = YoonFamily,
            fontWeight = FontWeight.Normal,
            color = NeutralColor.BLACK,
            letterSpacing = 0.sp // 0%
        )
    
    // 꾹꾹체 (Kkokko - kkokko.otf)
    private val KkokkoFamily = FontFamily(Font(R.font.kkokko))
    
    val KKOKKO_BUTTON
        @Composable get() = fixFontSize(
            fontSize = 16.sp,
            lineHeight = 22.4.sp, // 16 * 140%
            fontFamily = KkokkoFamily,
            fontWeight = FontWeight.Normal,
            color = NeutralColor.BLACK,
            letterSpacing = 0.sp // 0%
        )
    
    val KKOKKO_CARD
        @Composable get() = fixFontSize(
            fontSize = 14.sp,
            lineHeight = 19.6.sp, // 14 * 140%
            fontFamily = KkokkoFamily,
            fontWeight = FontWeight.Normal,
            color = NeutralColor.WHITE,
            letterSpacing = 0.sp // 0%
        )
    
    val KKOKKO_TAG
        @Composable get() = fixFontSize(
            fontSize = 14.sp,
            lineHeight = 19.6.sp, // 14 * 140%
            fontFamily = KkokkoFamily,
            fontWeight = FontWeight.Normal,
            color = NeutralColor.BLACK,
            letterSpacing = 0.sp // 0%
        )
}