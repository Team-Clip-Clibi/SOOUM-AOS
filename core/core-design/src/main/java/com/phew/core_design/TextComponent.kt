package com.phew.core_design

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

private val BoldFamily = FontFamily(Font(R.font.bold))
private val SemiBoldFamily = FontFamily(Font(R.font.semi_bold))
private val MediumFamily = FontFamily(Font(R.font.medium))
private val RegularFamily = FontFamily(Font(R.font.regular))

private fun textStyle(
    fontSize: TextUnit,
    lineHeight: TextUnit,
    fontFamily: FontFamily,
    fontWeight: FontWeight,
    color: Color,
): TextStyle {
    return TextStyle(
        fontSize = fontSize,
        lineHeight = lineHeight,
        fontFamily = fontFamily,
        fontWeight = fontWeight,
        color = color
    )
}

private object StaticTextStyles {
    val HEAD1_B_28 = textStyle(
        fontSize = 28.sp,
        lineHeight = 39.2.sp,
        fontFamily = BoldFamily,
        fontWeight = FontWeight(700),
        color = NeutralColor.BLACK
    )

    val HEAD_2_B_24 = textStyle(
        fontSize = 24.sp,
        lineHeight = 33.6.sp,
        fontFamily = BoldFamily,
        fontWeight = FontWeight(700),
        color = NeutralColor.BLACK
    )
    val HEAD_3_B_20 = textStyle(
        fontSize = 20.sp,
        lineHeight = 28.sp,
        fontFamily = BoldFamily,
        fontWeight = FontWeight(700),
        color = NeutralColor.BLACK
    )
    val TITLE_1_SB_18 = textStyle(
        fontSize = 18.sp,
        lineHeight = 27.sp,
        fontFamily = SemiBoldFamily,
        fontWeight = FontWeight(600),
        color = NeutralColor.BLACK
    )
    val TITLE_2_SB_16 = textStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontFamily = SemiBoldFamily,
        fontWeight = FontWeight(600),
        color = NeutralColor.BLACK
    )
    val SUBTITLE_1_M_16 = textStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontFamily = MediumFamily,
        fontWeight = FontWeight(500),
        color = NeutralColor.BLACK
    )
    val SUBTITLE_2_SB_14 = textStyle(
        fontSize = 14.sp,
        lineHeight = 21.sp,
        fontFamily = SemiBoldFamily,
        fontWeight = FontWeight(500),
        color = NeutralColor.BLACK
    )
    val SUBTITLE_3_SB_14 = textStyle(
        fontSize = 14.sp,
        lineHeight = 21.sp,
        fontFamily = SemiBoldFamily,
        fontWeight = FontWeight(600),
        color = NeutralColor.BLACK
    )
    val BODY_1_M_14 = textStyle(
        fontSize = 14.sp,
        lineHeight = 21.sp,
        fontFamily = MediumFamily,
        fontWeight = FontWeight(500),
        color = NeutralColor.BLACK
    )
    val BODY_2_R_14 = textStyle(
        fontSize = 14.sp,
        lineHeight = 21.sp,
        fontFamily = RegularFamily,
        fontWeight = FontWeight(500),
        color = NeutralColor.BLACK
    )
    val CAPTION_1_SB_12 = textStyle(
        fontSize = 12.sp,
        lineHeight = 18.sp,
        fontFamily = SemiBoldFamily,
        fontWeight = FontWeight(500),
        color = NeutralColor.BLACK
    )
    val CAPTION_2_M_12 = textStyle(
        fontSize = 12.sp,
        lineHeight = 18.sp,
        fontFamily = MediumFamily,
        fontWeight = FontWeight(500),
        color = NeutralColor.BLACK
    )
    val CAPTION_3_M_10 = textStyle(
        fontSize = 10.sp,
        lineHeight = 15.sp,
        fontFamily = MediumFamily,
        fontWeight = FontWeight(500),
        color = NeutralColor.BLACK
    )

    val CAPTION_4_M_5 = textStyle(
        fontSize = 5.sp,
        lineHeight = 7.5.sp,
        fontWeight = FontWeight(500),
        color = NeutralColor.BLACK,
        fontFamily = MediumFamily,
    )
}

object TextComponent {
    val HEAD1_B_28: TextStyle = StaticTextStyles.HEAD1_B_28
    val HEAD_2_B_24: TextStyle = StaticTextStyles.HEAD_2_B_24
    val HEAD_3_B_20: TextStyle = StaticTextStyles.HEAD_3_B_20
    val TITLE_1_SB_18: TextStyle = StaticTextStyles.TITLE_1_SB_18
    val TITLE_2_SB_16: TextStyle = StaticTextStyles.TITLE_2_SB_16
    val SUBTITLE_1_M_16: TextStyle = StaticTextStyles.SUBTITLE_1_M_16
    val SUBTITLE_2_SB_14: TextStyle = StaticTextStyles.SUBTITLE_2_SB_14
    val SUBTITLE_3_SB_14: TextStyle = StaticTextStyles.SUBTITLE_3_SB_14
    val BODY_1_M_14: TextStyle = StaticTextStyles.BODY_1_M_14
    val BODY_2_R_14: TextStyle = StaticTextStyles.BODY_2_R_14
    val CAPTION_1_SB_12: TextStyle = StaticTextStyles.CAPTION_1_SB_12
    val CAPTION_2_M_12: TextStyle = StaticTextStyles.CAPTION_2_M_12
    val CAPTION_3_M_10: TextStyle = StaticTextStyles.CAPTION_3_M_10
    val CAPTION_4_M_5: TextStyle = StaticTextStyles.CAPTION_4_M_5
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
