package com.phew.presentation.write.model

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.phew.core_design.R

object FontConfig {
    val availableFonts = listOf(
        FontItem(
            name = "프리텐다드",
            serverName = "PRETENDARD",
            previewTypeface = FontFamily(Font(R.font.regular))
        ),
        FontItem(
            name = "리디바탕",
            serverName = "RIDI",
            previewTypeface = FontFamily(Font(R.font.ridibatang))
        ),
        FontItem(
            name = "윤우체",
            serverName = "YOONWOO",
            previewTypeface = FontFamily(Font(R.font.yoon))
        ),
        FontItem(
            name = "꾹꾹체",
            serverName = "KKOOKKKOOK",
            previewTypeface = FontFamily(Font(R.font.kkokko))
        )
    )
    
    val defaultFont = availableFonts.first()
}