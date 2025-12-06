package com.phew.presentation.write.model

import android.net.Uri
import androidx.compose.ui.text.font.FontFamily
import com.phew.core.ui.model.CameraCaptureRequest
import com.phew.core_design.CustomFont
import com.phew.domain.dto.CardImageDefault
import com.phew.domain.dto.TagInfo
import com.phew.presentation.write.component.NumberTagItem
import com.phew.presentation.write.model.BackgroundConfig.imagesByFilter
import com.phew.presentation.write.utils.WriteErrorCase
import com.phew.presentation.write.viewmodel.UiState

private val DefaultFilter: BackgroundFilterType =
    BackgroundConfig.filterNames.firstOrNull() ?: BackgroundFilterType.COLOR
private val DefaultFilterSelection: Int? = imagesByFilter[DefaultFilter]?.firstOrNull()

data class WriteUiState(
    val content: String = "",
    val tags: List<String> = emptyList(),
    val currentTagInput: String = "",
    val relatedTags: List<TagInfo> = emptyList(),
    val isLoadingRelatedTags: Boolean = false,
    val selectedBackgroundFilter: BackgroundFilterType = DefaultFilter,
    val activeBackgroundResId: Int? = DefaultFilterSelection,
    val activeBackgroundUri: Uri? = null,
    val showBackgroundPickerSheet: Boolean = false,
    val shouldLaunchBackgroundAlbum: Boolean = false,
    val shouldRequestBackgroundCameraPermission: Boolean = false,
    val pendingBackgroundCameraCapture: CameraCaptureRequest? = null,
    val selectedFont: String = CustomFont.PRETENDARD_FONT.data.name,
    val selectedFontFamily: FontFamily? = CustomFont.PRETENDARD_FONT.data.previewTypeface,
    val selectedOptionIds: List<String> = listOf(WriteOptions.defaultOption.id),
    val hasLocationPermission: Boolean = false,
    val showLocationPermissionDialog: Boolean = false,
    val showCameraPermissionDialog: Boolean = false,
    val showGalleryPermissionDialog: Boolean = false,
    val focusTagInput: Boolean = false,
    val isWriteCompleted: Boolean = false,
    val shouldShowPermissionRationale: Boolean = false,
    val isWriteInProgress: Boolean = false,
    val parentCardId: Long? = null,
    val cardDefaultImagesByCategory: Map<BackgroundFilterType, List<CardImageDefault>> = emptyMap(),
    val selectedDefaultImageName: String? = null,
    val showErrorDialog: Boolean = false,
    val activateDate : UiState<String> = UiState.Loading,
    val errorCase: WriteErrorCase = WriteErrorCase.NONE
) {
    val isContentValid: Boolean
        get() = content.isNotBlank()

    val selectedGridImageResId: Int?
        get() {
            val active = activeBackgroundResId ?: return null
            val images = imagesByFilter[selectedBackgroundFilter].orEmpty()
            return if (images.contains(active)) active else null
        }

    val selectedGridImageName: String?
        get() = selectedDefaultImageName


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
