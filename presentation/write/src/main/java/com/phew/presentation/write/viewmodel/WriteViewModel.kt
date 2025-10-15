package com.phew.presentation.write.viewmodel

import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phew.domain.dto.Location
import com.phew.domain.repository.DeviceRepository
import com.phew.presentation.write.model.BackgroundConfig
import com.phew.presentation.write.model.FontConfig
import com.phew.presentation.write.model.WriteOptions
import com.phew.presentation.write.model.WriteUiState
import dagger.hilt.android.lifecycle.HiltViewModel
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
) : ViewModel() {

    private val _uiState = MutableStateFlow(WriteUiState())
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

    fun onPermissionResult(isGranted: Boolean) {
        if (!isGranted) {
            _uiState.update { state ->
                state.copy(shouldShowPermissionRationale = true)
            }
        }
    }

    fun updateContent(content: String) {
        _uiState.update { it.copy(content = content) }
    }

    fun updateTags(tags: List<String>) {
        _uiState.update { it.copy(tags = tags) }
    }

    fun selectBackgroundFilter(filter: String) {
        _uiState.update { state ->
            val images = BackgroundConfig.imagesByFilter[filter] ?: emptyList()
            state.copy(
                selectedBackgroundFilter = filter,
                selectedBackgroundImage = images.firstOrNull()
            )
        }
    }

    fun selectBackgroundImage(imageResId: Int?) {
        _uiState.update { it.copy(selectedBackgroundImage = imageResId) }
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

    fun selectOption(option: String) {
        _uiState.update { it.copy(selectedOption = option) }
    }

    fun onWriteComplete() {
        if (_uiState.value.canComplete) {
            // TODO: Handle write completion logic
            _uiState.update { it.copy(isWriteCompleted = true) }
        }
    }
}

private const val TAG = "WriteViewModel"