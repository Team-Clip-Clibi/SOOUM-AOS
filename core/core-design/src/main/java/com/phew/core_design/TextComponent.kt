package com.phew.core_design

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
private fun fixFontSize(
    fontSize: TextUnit,
    lineHeight: TextUnit,
    fontFamily: FontFamily,
    fontWeight: FontWeight,
    color: Color,
): TextStyle {
    val fontScale = LocalDensity.current.fontScale
    val fixedFontSize = (fontSize.value / fontScale).sp
    val fixedLineHeight = (lineHeight.value / fontScale).sp

    return TextStyle(
        fontSize = fixedFontSize,
        lineHeight = fixedLineHeight,
        fontFamily = fontFamily,
        fontWeight = fontWeight,
        color = color
    )
}

object TextComponent {
    val HEAD1_B_28
        @Composable get() = fixFontSize(
            fontSize = 28.sp,
            lineHeight = 39.2.sp,
            fontFamily = FontFamily(Font(R.font.bold)),
            fontWeight = FontWeight(700),
            color = NeutralColor.BLACK
        )

    val HEAD_2_B_24
        @Composable get() = fixFontSize(
            fontSize = 24.sp,
            lineHeight = 33.6.sp,
            fontFamily = FontFamily(Font(R.font.bold)),
            fontWeight = FontWeight(700),
            color = NeutralColor.BLACK
        )
    val HEAD_3_B_20
        @Composable get() = fixFontSize(
            fontSize = 20.sp,
            lineHeight = 28.sp,
            fontFamily = FontFamily(Font(R.font.bold)),
            fontWeight = FontWeight(700),
            color = NeutralColor.BLACK
        )
    val TITLE_1_SB_18
        @Composable get() = fixFontSize(
            fontSize = 18.sp,
            lineHeight = 27.sp,
            fontFamily = FontFamily(Font(R.font.semi_bold)),
            fontWeight = FontWeight(600),
            color = NeutralColor.BLACK
        )
    val TITLE_2_SB_16
        @Composable get() = fixFontSize(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontFamily = FontFamily(Font(R.font.semi_bold)),
            fontWeight = FontWeight(600),
            color = NeutralColor.BLACK
        )
    val SUBTITLE_1_M_16
        @Composable get() = fixFontSize(
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontFamily = FontFamily(Font(R.font.medium)),
            fontWeight = FontWeight(500),
            color = NeutralColor.BLACK
        )
    val SUBTITLE_2_SB_14
        @Composable get() = fixFontSize(
            fontSize = 14.sp,
            lineHeight = 21.sp,
            fontFamily = FontFamily(Font(R.font.semi_bold)),
            fontWeight = FontWeight(500),
            color = NeutralColor.BLACK
        )
    val SUBTITLE_3_SB_14
        @Composable get() = fixFontSize(
            fontSize = 14.sp,
            lineHeight = 21.sp,
            fontFamily = FontFamily(Font(R.font.semi_bold)),
            fontWeight = FontWeight(600),
            color = NeutralColor.BLACK
        )
    val BODY_1_M_14
        @Composable get() = fixFontSize(
            fontSize = 14.sp,
            lineHeight = 21.sp,
            fontFamily = FontFamily(Font(R.font.medium)),
            fontWeight = FontWeight(500),
            color = NeutralColor.BLACK
        )
    val CAPTION_1_SB_12
        @Composable get() = fixFontSize(
            fontSize = 12.sp,
            lineHeight = 18.sp,
            fontFamily = FontFamily(Font(R.font.semi_bold)),
            fontWeight = FontWeight(500),
            color = NeutralColor.BLACK
        )
    val CAPTION_2_M_12
        @Composable get() = fixFontSize(
            fontSize = 12.sp,
            lineHeight = 18.sp,
            fontFamily = FontFamily(Font(R.font.medium)),
            fontWeight = FontWeight(500),
            color = NeutralColor.BLACK
        )
    val CAPTION_3_M_10
        @Composable get() = fixFontSize(
            fontSize = 10.sp,
            lineHeight = 15.sp,
            fontFamily = FontFamily(Font(R.font.medium)),
            fontWeight = FontWeight(500),
            color = NeutralColor.BLACK
        )
}