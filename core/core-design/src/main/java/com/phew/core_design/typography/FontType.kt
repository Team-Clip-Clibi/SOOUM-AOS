package com.phew.core_design.typography

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily

enum class FontType(val serverName: String) {
    RIDIBATANG("RIDI"),
    YOON("YOONWOO"),
    KKOKKO("KKOOKKKOOK"),
    PRETENDARD("PRETENDARD");
    
    @Composable
    fun getButtonStyle(): TextStyle = when (this) {
        RIDIBATANG -> FontTextStyle.RIDIBATANG_BUTTON
        YOON -> FontTextStyle.YOON_BUTTON
        KKOKKO -> FontTextStyle.KKOKKO_BUTTON
        PRETENDARD -> FontTextStyle.DEFAULT_BUTTON
    }
    
    @Composable
    fun getCardStyle(): TextStyle = when (this) {
        RIDIBATANG -> FontTextStyle.RIDIBATANG_CARD
        YOON -> FontTextStyle.YOON_CARD
        KKOKKO -> FontTextStyle.KKOKKO_CARD
        PRETENDARD -> FontTextStyle.DEFAULT_CARD
    }
    
    @Composable
    fun getTagStyle(): TextStyle = when (this) {
        RIDIBATANG -> FontTextStyle.RIDIBATANG_TAG
        YOON -> FontTextStyle.YOON_TAG
        KKOKKO -> FontTextStyle.KKOKKO_TAG
        PRETENDARD -> FontTextStyle.DEFAULT_TAG
    }
    
    @Composable
    fun getFontFamily(): FontFamily = when (this) {
        RIDIBATANG -> FontTextStyle.RIDIBATANG_BUTTON.fontFamily ?: FontFamily.Default
        YOON -> FontTextStyle.YOON_BUTTON.fontFamily ?: FontFamily.Default
        KKOKKO -> FontTextStyle.KKOKKO_BUTTON.fontFamily ?: FontFamily.Default
        PRETENDARD -> FontTextStyle.DEFAULT_BUTTON.fontFamily ?: FontFamily.Default
    }
    
    companion object {
        fun fromServerName(serverName: String): FontType? {
            return entries.find { it.serverName == serverName }
        }
    }
}