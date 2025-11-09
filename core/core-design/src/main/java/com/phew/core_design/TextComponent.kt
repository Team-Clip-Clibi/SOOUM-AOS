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
    val BODY_2_R_14
        @Composable get() = fixFontSize(
            fontSize = 14.sp,
            lineHeight = 21.sp,
            fontFamily = FontFamily(Font(R.font.regular)),
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

    val CAPTION_4_M_5
        @Composable get() = fixFontSize(
            fontSize = 5.sp,
            lineHeight = 7.5.sp,
            fontWeight = FontWeight(500),
            color = NeutralColor.BLACK,
            fontFamily = FontFamily(Font(R.font.medium)),
        )
}

enum class CustomFont(val data: FontItem) {
    KOKO_FONT(
        FontItem(
            name = "꾹꾹체",
            serverName = "KKOOKKKOOK",
            previewTypeface = FontFamily(Font(R.font.kkokko))
        )
    ),
    YOONWOO_FONT(
        FontItem(
            name = "윤우체",
            serverName = "YOONWOO",
            previewTypeface = FontFamily(Font(R.font.yoon))
        )
    ),
    RIDI_FONT(
        FontItem(
            name = "리디바탕",
            serverName = "RIDI",
            previewTypeface = FontFamily(Font(R.font.ridibatang))
        )
    ),
    PRETENDARD_FONT(
        FontItem(
            name = "프리텐다드",
            serverName = "PRETENDARD",
            previewTypeface = FontFamily(Font(R.font.regular))
        )
    );

    companion object {
        fun findFontValueByServerName(serverName: String): CustomFont {
            return entries.firstOrNull { data -> data.data.serverName == serverName }
                ?: PRETENDARD_FONT
        }

        fun findFontValueByPreviewType(data: FontFamily): CustomFont {
            return entries.firstOrNull { font -> font.data.previewTypeface == data }
                ?: PRETENDARD_FONT
        }

        fun fundFontValueByName(name: String): CustomFont {
            return entries.firstOrNull { font -> font.data.name == name } ?: PRETENDARD_FONT
        }
        val fontData = listOf<FontItem>(
            PRETENDARD_FONT.data,
            RIDI_FONT.data,
            YOONWOO_FONT.data,
            KOKO_FONT.data
        )
    }
}

data class FontItem(
    val name: String,
    val serverName: String,
    val previewTypeface: FontFamily
)
