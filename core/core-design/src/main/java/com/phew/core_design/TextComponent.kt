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
            color = Color(0xFF212121)
        )
}