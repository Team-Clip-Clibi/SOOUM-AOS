package com.phew.presentation.write.model

import android.net.Uri
import androidx.compose.ui.text.font.FontFamily
import com.phew.core.ui.model.CameraCaptureRequest

private val DefaultFilter: String = BackgroundConfig.filterNames.firstOrNull() ?: ""
private val DefaultFilterSelection: Int? = BackgroundConfig.imagesByFilter[DefaultFilter]?.firstOrNull()
private val DefaultFilterSelections: Map<String, Int> =
    if (DefaultFilter.isNotEmpty() && DefaultFilterSelection != null) {
        mapOf(DefaultFilter to DefaultFilterSelection)
    } else {
        emptyMap()
    }

data class WriteUiState(
    val content: String = "",
    val tags: List<String> = emptyList(),
    val selectedBackgroundFilter: String = DefaultFilter,
    val filterBackgroundSelections: Map<String, Int> = DefaultFilterSelections,
    val activeBackgroundResId: Int? = DefaultFilterSelection,
    val activeBackgroundUri: Uri? = null,
    val showBackgroundPickerSheet: Boolean = false,
    val shouldLaunchBackgroundAlbum: Boolean = false,
    val shouldRequestBackgroundCameraPermission: Boolean = false,
    val pendingBackgroundCameraCapture: CameraCaptureRequest? = null,
    val selectedFont: String = FontConfig.defaultFont.name,
    val selectedFontFamily: FontFamily? = FontConfig.defaultFont.previewTypeface,
    val selectedOptionId: String = WriteOptions.defaultOption.id,
    val hasLocationPermission: Boolean = false,
    val showLocationPermissionDialog: Boolean = false,
    val focusTagInput: Boolean = false,
    val isWriteCompleted: Boolean = false,
    val shouldShowPermissionRationale: Boolean = false
) {
    val isContentValid: Boolean
        get() = content.isNotBlank()

    val currentFilterSelection: Int?
        get() = filterBackgroundSelections[selectedBackgroundFilter]

    val selectedOptionDisplayName: String
        get() = WriteOptions.findById(selectedOptionId)?.displayName.orEmpty()

    val canComplete: Boolean
        get() = isContentValid && (activeBackgroundResId != null || activeBackgroundUri != null)
}
