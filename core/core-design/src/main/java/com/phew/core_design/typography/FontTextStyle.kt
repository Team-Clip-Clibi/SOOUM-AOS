package com.phew.core_design.typography

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.phew.core_design.R
import com.phew.core_design.NeutralColor

private fun textStyle(
    fontSize: TextUnit,
    lineHeight: TextUnit,
    fontFamily: FontFamily,
    fontWeight: FontWeight,
    color: Color,
    letterSpacing: TextUnit = 0.sp
): TextStyle {
    return TextStyle(
        fontSize = fontSize,
        lineHeight = lineHeight,
        fontFamily = fontFamily,
        fontWeight = fontWeight,
        color = color,
        letterSpacing = letterSpacing
    )
}

private object StaticFontTextStyles {

    // 기본 폰트 패밀리 (Default - regular.otf)
    private val DefaultFamily = FontFamily(Font(R.font.regular))

    val DEFAULT_BUTTON = textStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp, // 16 * 150%
        fontFamily = DefaultFamily,
        fontWeight = FontWeight.Medium,
        color = NeutralColor.BLACK,
        letterSpacing = (-0.4).sp // -2.5%
    )

    val DEFAULT_CARD = textStyle(
        fontSize = 14.sp,
        lineHeight = 21.sp, // 14 * 150%
        fontFamily = DefaultFamily,
        fontWeight = FontWeight.Medium,
        color = NeutralColor.WHITE,
        letterSpacing = (-0.35).sp // -2.5%
    )

    val DEFAULT_TAG = textStyle(
        fontSize = 12.sp,
        lineHeight = 18.sp, // 12 * 150%
        fontFamily = DefaultFamily,
        fontWeight = FontWeight.Medium,
        color = NeutralColor.BLACK,
        letterSpacing = (-0.3).sp // -2.5%
    )

    val DEFAULT_PROFILE = textStyle(
        fontSize = 5.sp,
        lineHeight = 7.5.sp, // 5 * 150%
        fontFamily = DefaultFamily,
        fontWeight = FontWeight.Medium,
        color = NeutralColor.BLACK,
        letterSpacing = (-0.125).sp // -2.5%
    )

    // 리디바탕 (RIDIBang - ridibatang.otf)
    private val RidibatangFamily = FontFamily(Font(R.font.ridibatang))

    val RIDIBATANG_BUTTON = textStyle(
        fontSize = 15.sp,
        lineHeight = 22.5.sp, // 15 * 150%
        fontFamily = RidibatangFamily,
        fontWeight = FontWeight.Normal,
        color = NeutralColor.BLACK,
        letterSpacing = (-0.375).sp // -2.5%
    )

    val RIDIBATANG_CARD = textStyle(
        fontSize = 13.sp,
        lineHeight = 19.5.sp, // 13 * 150%
        fontFamily = RidibatangFamily,
        fontWeight = FontWeight.Normal,
        color = NeutralColor.WHITE,
        letterSpacing = (-0.325).sp // -2.5%
    )

    val RIDIBATANG_TAG = textStyle(
        fontSize = 11.sp,
        lineHeight = 16.5.sp, // 11 * 150%
        fontFamily = RidibatangFamily,
        fontWeight = FontWeight.Normal,
        color = NeutralColor.BLACK,
        letterSpacing = (-0.275).sp // -2.5%
    )

    // 윤우체 (Yoon - yoon.ttf)
    private val YoonFamily = FontFamily(Font(R.font.yoon))

    val YOON_BUTTON = textStyle(
        fontSize = 20.sp,
        lineHeight = 22.sp, // 20 * 110%
        fontFamily = YoonFamily,
        fontWeight = FontWeight.Normal,
        color = NeutralColor.BLACK,
        letterSpacing = 0.sp // 0%
    )

    val YOON_CARD = textStyle(
        fontSize = 18.sp,
        lineHeight = 19.8.sp, // 18 * 110%
        fontFamily = YoonFamily,
        fontWeight = FontWeight.Normal,
        color = NeutralColor.WHITE,
        letterSpacing = 0.sp // 0%
    )

    val YOON_TAG = textStyle(
        fontSize = 16.sp,
        lineHeight = 17.6.sp, // 16 * 110%
        fontFamily = YoonFamily,
        fontWeight = FontWeight.Normal,
        color = NeutralColor.BLACK,
        letterSpacing = 0.sp // 0%
    )

    // 꾹꾹체 (Kkokko - kkokko.otf)
    private val KkokkoFamily = FontFamily(Font(R.font.kkokko))

    val KKOKKO_BUTTON = textStyle(
        fontSize = 16.sp,
        lineHeight = 22.4.sp, // 16 * 140%
        fontFamily = KkokkoFamily,
        fontWeight = FontWeight.Normal,
        color = NeutralColor.BLACK,
        letterSpacing = 0.sp // 0%
    )

    val KKOKKO_CARD = textStyle(
        fontSize = 14.sp,
        lineHeight = 19.6.sp, // 14 * 140%
        fontFamily = KkokkoFamily,
        fontWeight = FontWeight.Normal,
        color = NeutralColor.WHITE,
        letterSpacing = 0.sp // 0%
    )

    val KKOKKO_TAG = textStyle(
        fontSize = 14.sp,
        lineHeight = 19.6.sp, // 14 * 140%
        fontFamily = KkokkoFamily,
        fontWeight = FontWeight.Normal,
        color = NeutralColor.BLACK,
        letterSpacing = 0.sp // 0%
    )
}

object FontTextStyle {
    val DEFAULT_BUTTON: TextStyle = StaticFontTextStyles.DEFAULT_BUTTON
    val DEFAULT_CARD: TextStyle = StaticFontTextStyles.DEFAULT_CARD
    val DEFAULT_TAG: TextStyle = StaticFontTextStyles.DEFAULT_TAG
    val DEFAULT_PROFILE: TextStyle = StaticFontTextStyles.DEFAULT_PROFILE
    val RIDIBATANG_BUTTON: TextStyle = StaticFontTextStyles.RIDIBATANG_BUTTON
    val RIDIBATANG_CARD: TextStyle = StaticFontTextStyles.RIDIBATANG_CARD
    val RIDIBATANG_TAG: TextStyle = StaticFontTextStyles.RIDIBATANG_TAG
    val YOON_BUTTON: TextStyle = StaticFontTextStyles.YOON_BUTTON
    val YOON_CARD: TextStyle = StaticFontTextStyles.YOON_CARD
    val YOON_TAG: TextStyle = StaticFontTextStyles.YOON_TAG
    val KKOKKO_BUTTON: TextStyle = StaticFontTextStyles.KKOKKO_BUTTON
    val KKOKKO_CARD: TextStyle = StaticFontTextStyles.KKOKKO_CARD
    val KKOKKO_TAG: TextStyle = StaticFontTextStyles.KKOKKO_TAG
}
