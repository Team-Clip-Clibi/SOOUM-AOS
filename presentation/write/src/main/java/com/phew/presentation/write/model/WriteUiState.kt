package com.phew.presentation.write.model

import android.net.Uri
import androidx.compose.ui.text.font.FontFamily
import com.phew.core.ui.model.CameraCaptureRequest
import com.phew.domain.dto.TagInfo
import com.phew.presentation.write.component.NumberTagItem

private val DefaultFilter: String = BackgroundConfig.filterNames.firstOrNull() ?: ""
private val DefaultFilterSelection: Int? = BackgroundConfig.imagesByFilter[DefaultFilter]?.firstOrNull()

data class WriteUiState(
    val content: String = "",
    val tags: List<String> = emptyList(),
    val currentTagInput: String = "",
    val relatedTags: List<TagInfo> = emptyList(),
    val isLoadingRelatedTags: Boolean = false,
    val selectedBackgroundFilter: String = DefaultFilter,
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
    val showCameraPermissionDialog: Boolean = false,
    val showGalleryPermissionDialog: Boolean = false,
    val focusTagInput: Boolean = false,
    val isWriteCompleted: Boolean = false,
    val shouldShowPermissionRationale: Boolean = false
) {
    val isContentValid: Boolean
        get() = content.isNotBlank()

    val selectedGridImageResId: Int?
        get() {
            val active = activeBackgroundResId ?: return null
            val images = BackgroundConfig.imagesByFilter[selectedBackgroundFilter].orEmpty()
            return if (images.contains(active)) active else null
        }

    val selectedOptionDisplayName: String
        get() = WriteOptions.findById(selectedOptionId)?.displayName.orEmpty()

    val canComplete: Boolean
        get() = isContentValid && (activeBackgroundResId != null || activeBackgroundUri != null)

    val relatedNumberTags: List<NumberTagItem>
        get() = relatedTags.map { tagInfo ->
            NumberTagItem(
                id = tagInfo.id,
                name = tagInfo.name,
                countLabel = tagInfo.tag,
                countValue = tagInfo.countDisplay.value,
                countUnit = tagInfo.countDisplay.unit
            )
        }
}
