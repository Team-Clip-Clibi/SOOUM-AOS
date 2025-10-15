package com.phew.presentation.write.model

import androidx.compose.ui.text.font.FontFamily

data class WriteUiState(
    val content: String = "",
    val tags: List<String> = emptyList(),
    val selectedBackgroundFilter: String = BackgroundConfig.filterNames.first(),
    val selectedBackgroundImage: Int? = null,
    val selectedFont: String = FontConfig.defaultFont.name,
    val selectedFontFamily: FontFamily? = FontConfig.defaultFont.previewTypeface,
    val selectedOption: String = WriteOptions.defaultOption.displayName,
    val isWriteCompleted: Boolean = false,
    val shouldShowPermissionRationale: Boolean = false
) {
    val isContentValid: Boolean
        get() = content.isNotBlank()
        
    val canComplete: Boolean
        get() = isContentValid && selectedBackgroundImage != null
}