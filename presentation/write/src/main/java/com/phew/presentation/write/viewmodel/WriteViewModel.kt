package com.phew.presentation.write.viewmodel

import android.Manifest
import android.net.Uri
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phew.core_common.DomainResult
import com.phew.core.ui.model.CameraCaptureRequest
import com.phew.core.ui.model.CameraPickerAction
import com.phew.domain.dto.Location
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.usecase.CreateImageFile
import com.phew.domain.usecase.FinishTakePicture
import com.phew.presentation.write.model.BackgroundConfig
import com.phew.presentation.write.model.FontConfig
import com.phew.presentation.write.model.WriteOptions
import com.phew.presentation.write.model.WriteUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WriteViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val createImageFile: CreateImageFile,
    private val finishTakePicture: FinishTakePicture,
) : ViewModel() {

    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val distanceOptionId = WriteOptions.DISTANCE_OPTION_ID
    private val initialFilter: String = BackgroundConfig.filterNames.firstOrNull() ?: ""
    private val initialImage: Int? = BackgroundConfig.imagesByFilter[initialFilter]?.firstOrNull()
    private val initialSelections: Map<String, Int> =
        if (initialFilter.isNotEmpty() && initialImage != null) {
            mapOf(initialFilter to initialImage)
        } else {
            emptyMap()
        }

    private val _uiState = MutableStateFlow(
        WriteUiState(
            selectedBackgroundFilter = initialFilter,
            filterBackgroundSelections = initialSelections,
            activeBackgroundResId = initialImage
        )
    )
    val uiState: StateFlow<WriteUiState> = _uiState.asStateFlow()

    /**
     * 권한 요청
     */
    private val _requestPermissionEvent = MutableSharedFlow<Array<String>>()
    val requestPermissionEvent = _requestPermissionEvent.asSharedFlow()

    private suspend fun getLocationSafely(): Location {
        return try {
            deviceRepository.requestLocation()
        } catch (e: Exception) {
            // 위치 정보 가져오기 실패 시 빈 위치 반환
            Location.EMPTY
        }
    }

    private fun getLocation() {
        viewModelScope.launch {
            val location = getLocationSafely()
            // TODO: Update state with location if needed
        }
    }

    fun onInitialLocationPermissionCheck(isGranted: Boolean) {
        _uiState.update { state ->
            val adjusted = adjustOptionForPermission(state.selectedOptionId, isGranted)
            val resolved = if (isGranted && adjusted.isBlank()) distanceOptionId else adjusted
            state.copy(
                hasLocationPermission = isGranted,
                selectedOptionId = resolved,
                shouldShowPermissionRationale = false
            )
        }
    }

    fun onLocationPermissionResult(isGranted: Boolean) {
        _uiState.update { state ->
            val adjusted = adjustOptionForPermission(state.selectedOptionId, isGranted)
            val resolved = if (isGranted && adjusted.isBlank()) distanceOptionId else adjusted
            state.copy(
                hasLocationPermission = isGranted,
                selectedOptionId = resolved,
                showLocationPermissionDialog = false,
                shouldShowPermissionRationale = !isGranted
            )
        }
    }

    fun onDistanceOptionClickWithoutPermission() {
        _uiState.update { it.copy(showLocationPermissionDialog = true) }
    }

    fun dismissLocationPermissionDialog() {
        _uiState.update { it.copy(showLocationPermissionDialog = false) }
    }

    fun requestLocationPermission() {
        _uiState.update { it.copy(showLocationPermissionDialog = false) }
        viewModelScope.launch {
            _requestPermissionEvent.emit(locationPermissions)
        }
    }

    fun updateContent(content: String) {
        _uiState.update { it.copy(content = content) }
    }

    fun onContentEnter() {
        _uiState.update { it.copy(focusTagInput = true) }
    }

    fun onTagInputFocusHandled() {
        _uiState.update { it.copy(focusTagInput = false) }
    }

    fun addTag(tag: String) {
        val trimmed = tag.trim()
        if (trimmed.isEmpty()) return
        _uiState.update { state ->
            if (state.tags.contains(trimmed)) {
                state.copy(focusTagInput = false)
            } else {
                state.copy(tags = state.tags + trimmed, focusTagInput = false)
            }
        }
    }

    fun removeTag(tag: String) {
        _uiState.update { state ->
            state.copy(tags = state.tags.filterNot { it == tag })
        }
    }

    fun selectBackgroundFilter(filter: String) {
        _uiState.update { it.copy(selectedBackgroundFilter = filter) }
    }

    fun selectBackgroundImage(imageResId: Int) {
        _uiState.update { state ->
            val updatedSelections =
                state.filterBackgroundSelections + (state.selectedBackgroundFilter to imageResId)
            state.copy(
                filterBackgroundSelections = updatedSelections,
                activeBackgroundResId = imageResId,
                activeBackgroundUri = null
            )
        }
    }

    fun onBackgroundPickerRequested() {
        _uiState.update { it.copy(showBackgroundPickerSheet = true) }
    }

    fun onBackgroundPickerDismissed() {
        _uiState.update { it.copy(showBackgroundPickerSheet = false) }
    }

    fun onBackgroundPickerAction(action: CameraPickerAction) {
        when (action) {
            CameraPickerAction.Album -> {
                _uiState.update {
                    it.copy(
                        showBackgroundPickerSheet = false,
                        shouldLaunchBackgroundAlbum = true
                    )
                }
            }

            CameraPickerAction.Camera -> {
                _uiState.update {
                    it.copy(
                        showBackgroundPickerSheet = false,
                        shouldRequestBackgroundCameraPermission = true
                    )
                }
            }
        }
    }

    fun onBackgroundAlbumRequestConsumed() {
        _uiState.update { it.copy(shouldLaunchBackgroundAlbum = false) }
    }

    fun onBackgroundCameraPermissionRequestConsumed() {
        _uiState.update { it.copy(shouldRequestBackgroundCameraPermission = false) }
    }

    fun onBackgroundCameraPermissionResult(granted: Boolean) {
        if (granted) {
            requestCameraImageForBackground()
        }
    }

    fun onBackgroundCameraCaptureLaunched(request: CameraCaptureRequest) {
        _uiState.update { it.copy(pendingBackgroundCameraCapture = null) }
    }

    fun onBackgroundCameraCaptureResult(success: Boolean, uri: Uri) {
        if (success) {
            viewModelScope.launch(Dispatchers.IO) {
                when (val result = finishTakePicture(FinishTakePicture.Param(uri))) {
                    is DomainResult.Success -> {
                        _uiState.update { state ->
                            val updatedSelections =
                                state.filterBackgroundSelections - state.selectedBackgroundFilter
                            state.copy(
                                filterBackgroundSelections = updatedSelections,
                                activeBackgroundUri = result.data,
                                activeBackgroundResId = null
                            )
                        }
                    }

                    is DomainResult.Failure -> {
                        // 실패 시 별도 처리 없음
                    }
                }
            }
        }
    }

    fun onBackgroundAlbumImagePicked(uri: Uri) {
        _uiState.update { state ->
            val updatedSelections =
                state.filterBackgroundSelections - state.selectedBackgroundFilter
            state.copy(
                filterBackgroundSelections = updatedSelections,
                activeBackgroundUri = uri,
                activeBackgroundResId = null
            )
        }
    }

    fun selectFont(fontFamily: FontFamily) {
        val selectedFont = FontConfig.availableFonts.find { it.previewTypeface == fontFamily }
        selectedFont?.let { font ->
            _uiState.update { state ->
                state.copy(
                    selectedFont = font.name,
                    selectedFontFamily = font.previewTypeface
                )
            }
        }
    }

    fun selectOption(optionId: String) {
        _uiState.update { state ->
            if (optionId == distanceOptionId && !state.hasLocationPermission) {
                state
            } else {
                state.copy(selectedOptionId = optionId)
            }
        }
    }

private fun requestCameraImageForBackground() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = createImageFile()) {
                is DomainResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            pendingBackgroundCameraCapture = CameraCaptureRequest(
                                id = System.currentTimeMillis(),
                                uri = result.data
                            )
                        )
                    }
                }

                is DomainResult.Failure -> {
                    // 실패 시 별도 처리 없음
                }
            }
        }
    }

    fun onWriteComplete() {
        if (_uiState.value.canComplete) {
            // TODO: Handle write completion logic
            _uiState.update { it.copy(isWriteCompleted = true) }
        }
    }

    private fun adjustOptionForPermission(optionId: String, hasPermission: Boolean): String {
        return if (!hasPermission && optionId == distanceOptionId) "" else optionId
    }
}

private const val TAG = "WriteViewModel"
