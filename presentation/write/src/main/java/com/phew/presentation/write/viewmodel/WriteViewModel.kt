package com.phew.presentation.write.viewmodel

import android.Manifest
import android.net.Uri
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phew.core.ui.model.CameraCaptureRequest
import com.phew.core_common.log.SooumLog
import com.phew.core.ui.model.CameraPickerAction
import com.phew.core_common.DomainResult
import com.phew.domain.dto.Location
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.usecase.CreateImageFile
import com.phew.domain.usecase.FinishTakePicture
import com.phew.domain.usecase.GetCardDefaultImage
import com.phew.domain.usecase.GetRelatedTag
import com.phew.domain.usecase.PostCard
import com.phew.domain.usecase.PostCardReply
import com.phew.presentation.write.model.BackgroundConfig
import com.phew.presentation.write.model.WriteOptions
import com.phew.presentation.write.model.WriteUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.net.toUri
import com.phew.core_design.CustomFont

import com.phew.presentation.write.model.BackgroundFilterType

@HiltViewModel
class WriteViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val createImageFile: CreateImageFile,
    private val finishTakePicture: FinishTakePicture,
    private val getRelatedTag: GetRelatedTag,
    private val getCardDefaultImage: GetCardDefaultImage,
    private val postCard: PostCard,
    private val postCardReply: PostCardReply
) : ViewModel() {

    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val distanceOptionId = WriteOptions.DISTANCE_OPTION_ID
    private val initialFilter: BackgroundFilterType = BackgroundConfig.filterNames.firstOrNull() ?: BackgroundFilterType.COLOR
    private val initialImage: Int? = BackgroundConfig.imagesByFilter[initialFilter]?.firstOrNull()

    private val _uiState = MutableStateFlow(
        WriteUiState(
            selectedBackgroundFilter = initialFilter,
            activeBackgroundResId = initialImage
        )
    )
    val uiState: StateFlow<WriteUiState> = _uiState.asStateFlow()

    init {
        // 기본 이미지 로드
        loadCardDefaultImages()

        // 실시간 태그 검색 로직
        viewModelScope.launch {
            uiState
                .map { it.currentTagInput }
                .distinctUntilChanged()
                .debounce(300L)
                .filter { it.isNotBlank() }
                .flatMapLatest { tagInput ->
                    _uiState.update { it.copy(isLoadingRelatedTags = true) }
                    try {
                        when (val result = getRelatedTag(GetRelatedTag.Param(tag = tagInput, resultCnt = 8))) {
                            is DomainResult.Success -> {
                                flowOf(result.data)
                            }
                            is DomainResult.Failure -> {
                                flowOf(emptyList())
                            }
                        }
                    } catch (e: Exception) {
                        flowOf(emptyList())
                    }
                }
                .collect { relatedTags ->
                    _uiState.update { 
                        it.copy(
                            relatedTags = relatedTags,
                            isLoadingRelatedTags = false
                        ) 
                    }
                }
        }
    }

    /**
     * 권한 요청
     */
    private val _requestPermissionEvent = MutableSharedFlow<Array<String>>()
    val requestPermissionEvent = _requestPermissionEvent.asSharedFlow()

    /**
     * 완료 이벤트
     */
    private val _writeCompleteEvent = MutableSharedFlow<Long>()
    val writeCompleteEvent = _writeCompleteEvent.asSharedFlow()

    private suspend fun getLocationSafely(): Location {
        return try {
            deviceRepository.requestLocation()
        } catch (e: Exception) {
            // 위치 정보 가져오기 실패 시 빈 위치 반환
            Location.EMPTY
        }
    }

    fun onInitialLocationPermissionCheck(isGranted: Boolean) {
        _uiState.update { state ->
            val adjustedIds = adjustOptionForPermission(state.selectedOptionIds, isGranted)
            state.copy(
                hasLocationPermission = isGranted,
                selectedOptionIds = adjustedIds,
                shouldShowPermissionRationale = false
            )
        }
    }

    fun onLocationPermissionResult(isGranted: Boolean) {
        _uiState.update { state ->
            val adjustedIds = adjustOptionForPermission(state.selectedOptionIds, isGranted)
            state.copy(
                hasLocationPermission = isGranted,
                selectedOptionIds = adjustedIds,
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

    fun onCameraPermissionDenied() {
        _uiState.update { it.copy(showCameraPermissionDialog = true) }
    }

    fun onGalleryPermissionDenied() {
        _uiState.update { it.copy(showGalleryPermissionDialog = true) }
    }

    fun dismissCameraPermissionDialog() {
        _uiState.update { it.copy(showCameraPermissionDialog = false) }
    }

    fun dismissGalleryPermissionDialog() {
        _uiState.update { it.copy(showGalleryPermissionDialog = false) }
    }

    fun requestCameraPermissionFromSettings() {
        _uiState.update { it.copy(showCameraPermissionDialog = false) }
    }

    fun requestGalleryPermissionFromSettings() {
        _uiState.update { it.copy(showGalleryPermissionDialog = false) }
    }

    fun onGallerySettingsResult(granted: Boolean) {
        _uiState.update { state ->
            if (granted) {
                state.copy(
                    shouldLaunchBackgroundAlbum = true,
                    showGalleryPermissionDialog = false
                )
            } else {
                state.copy(showGalleryPermissionDialog = true)
            }
        }
    }

    fun onCameraSettingsResult(granted: Boolean) {
        if (granted) {
            requestCameraImageForBackground()
            _uiState.update { it.copy(showCameraPermissionDialog = false) }
        } else {
            _uiState.update { it.copy(showCameraPermissionDialog = true) }
        }
    }

    fun updateContent(content: String) {
        _uiState.update { it.copy(content = content) }
    }

    fun updateTagInput(input: String) {
        _uiState.update { it.copy(currentTagInput = input) }
    }

    fun onTagInputFocusHandled() {
        _uiState.update { it.copy(focusTagInput = false) }
    }

    fun completeTagInput() {
        _uiState.update {
            it.copy(tagInputCompleteSignal = it.tagInputCompleteSignal + 1)
        }
    }

    fun resetTagInput() {
        _uiState.update {
            it.copy(
                currentTagInput = "",
                tagInputCompleteSignal = it.tagInputCompleteSignal + 1
            )
        }
    }

    fun addTag(tag: String) {
        val trimmed = tag.trim()
        if (trimmed.isEmpty()) return
        _uiState.update { state ->
            if (state.tags.contains(trimmed)) {
                state.copy(focusTagInput = false, currentTagInput = "")
            } else {
                state.copy(
                    tags = state.tags + trimmed, 
                    focusTagInput = false,
                    currentTagInput = ""
                )
            }
        }
    }

    fun removeTag(tag: String) {
        _uiState.update { state ->
            state.copy(tags = state.tags.filterNot { it == tag })
        }
    }

    fun selectBackgroundFilter(filter: BackgroundFilterType) {
        _uiState.update { it.copy(selectedBackgroundFilter = filter) }
    }

    fun selectBackgroundImage(imageName: String) {
        _uiState.update { state ->
            // 서버에서 받은 기본 이미지 찾기
            val serverImage = state.cardDefaultImagesByCategory.values
                .flatten()
                .find { it.imageName == imageName }

            SooumLog.d(TAG, "selectBackgroundImage() imageName: $imageName, serverImage: $serverImage")

            if (serverImage != null) {
                // 서버 기본 이미지인 경우
                state.copy(
                    activeBackgroundResId = null,
                    activeBackgroundUri = serverImage.url.toUri(),
                    selectedDefaultImageName = imageName
                )
            } else {
                // 서버 이미지가 아닌 경우 (현재는 모든 이미지가 서버에서 오므로 이 경우는 없어야 함)
                state.copy(
                    activeBackgroundResId = null,
                    activeBackgroundUri = null,
                    selectedDefaultImageName = null
                )
            }
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
            else -> Unit
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
                            state.copy(
                                activeBackgroundUri = result.data,
                                activeBackgroundResId = null,
                                selectedDefaultImageName = null
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
            state.copy(
                activeBackgroundUri = uri,
                activeBackgroundResId = null,
                selectedDefaultImageName = null
            )
        }
    }

    fun selectFont(fontFamily: FontFamily) {
        val selectedFont = CustomFont.findFontValueByPreviewType(data = fontFamily)
        selectedFont.let { font ->
            _uiState.update { state ->
                state.copy(
                    selectedFont = font.data.name,
                    selectedFontFamily = font.data.previewTypeface
                )
            }
        }
    }

    fun selectOption(optionId: String) {
        _uiState.update { state ->
            if (optionId == distanceOptionId && !state.hasLocationPermission) {
                return@update state
            }

            val currentIds = state.selectedOptionIds
            val newIds = if (currentIds.contains(optionId)) {
                currentIds.filter { it != optionId }
            } else {
                currentIds + optionId
            }
            state.copy(selectedOptionIds = newIds)
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

    fun hideRelatedTags() {
        _uiState.update {
            it.copy(
                relatedTags = emptyList()
            )
        }
    }

    fun setParentCardId(parentCardId: Long) {
        _uiState.update { it.copy(parentCardId = parentCardId) }
    }

    fun onWriteComplete() {
        val currentState = _uiState.value
        if (currentState.canComplete && !currentState.isWriteInProgress) {
            _uiState.update { it.copy(isWriteInProgress = true) }
            viewModelScope.launch {
                val state = _uiState.value
                val selectedFontServerName = CustomFont.fundFontValueByName(state.selectedFont)
                SooumLog.d(
                    TAG,
                    "onWriteComplete state: selectedDefaultImageName=${state.selectedDefaultImageName}, activeBackgroundUri=${state.activeBackgroundUri}"
                )

                val result: DomainResult<Long, String> = try {
                    if (state.parentCardId != null) {
                        // 댓글 작성 (PostCardReply 사용)
                        val (imgType, imgName) = when {
                            state.selectedDefaultImageName != null -> "DEFAULT" to state.selectedDefaultImageName
                            state.activeBackgroundUri != null -> "CUSTOM" to ""
                            else -> "DEFAULT" to ""
                        }

                        SooumLog.d(
                            TAG,
                            "onWriteComplete reply imgType: $imgType, imgName: $imgName"
                        )

                        val replyParam = PostCardReply.Param(
                            cardId = state.parentCardId,
                            content = state.content,
                            font = selectedFontServerName.data.serverName,
                            imgType = imgType,
                            imgName = imgName,
                            tags = state.tags,
                            isDistanceShared = state.selectedOptionIds.contains(WriteOptions.DISTANCE_OPTION_ID)
                        )
                        SooumLog.d(TAG, "onWriteComplete reply: $replyParam")
                        postCardReply(replyParam)
                    } else {
                        // 새 카드 작성 (PostCard 사용)
                        val (isFromDevice, imgName, imageUrl) = when {
                            state.selectedDefaultImageName != null -> Triple(
                                false,
                                state.selectedDefaultImageName,
                                null
                            )

                            state.activeBackgroundUri != null -> Triple(
                                true,
                                null,
                                state.activeBackgroundUri.toString()
                            )

                            else -> Triple(false, null, null)
                        }

                        SooumLog.d(
                            TAG,
                            "onWriteComplete card isFromDevice: $isFromDevice, imgName: $imgName, imageUrl: $imageUrl"
                        )

                        val cardParam = PostCard.Param(
                            isFromDevice = isFromDevice,
                            answerCard = false,
                            cardId = null,
                            imageUrl = imageUrl,
                            content = state.content,
                            font = selectedFontServerName.data.serverName,
                            imgName = imgName,
                            isStory = state.selectedOptionIds.contains("twenty_four_hours"),
                            tags = state.tags
                        )
                        SooumLog.d(TAG, "onWriteComplete card: $cardParam")
                        postCard(cardParam)
                    }
                } catch (e: Exception) {
                    SooumLog.e(TAG, "onWriteComplete exception during API call: ${e.message}")
                    DomainResult.Failure("API 호출 중 예외 발생: ${e.message}")
                }

                when (result) {
                    is DomainResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isWriteCompleted = true,
                                isWriteInProgress = false
                            )
                        }
                        SooumLog.d(TAG, "onWriteComplete success: ${result.data}")
                        _writeCompleteEvent.emit(result.data)
                    }

                    is DomainResult.Failure -> {
                        _uiState.update { it.copy(isWriteInProgress = false) }
                        SooumLog.e(TAG, "onWriteComplete failed: ${result.error}")
                        // Handle error - could add error state to UI
                    }
                }
            }
        }
    }

    private fun adjustOptionForPermission(optionIds: List<String>, hasPermission: Boolean): List<String> {
        return if (!hasPermission) {
            optionIds.filter { it != distanceOptionId }
        } else {
            optionIds
        }
    }

    private fun loadCardDefaultImages() {
        viewModelScope.launch {
            try {
                when (val result = getCardDefaultImage()) {
                    is DomainResult.Success -> {
                        _uiState.update { state ->
                            val convertedMap = result.data.defaultImages.mapNotNull { (key, value) ->
                                BackgroundFilterType.fromServerKey(key)?.let { it to value }
                            }.toMap()

                            val newState = state.copy(
                                cardDefaultImagesByCategory = convertedMap
                            )
                            
                            // COLOR 카테고리의 첫 번째 이미지를 자동으로 선택
                            val colorCategoryImages = convertedMap[BackgroundFilterType.COLOR]
                            val firstColorImage = colorCategoryImages?.firstOrNull()
                            
                            if (firstColorImage != null && state.selectedDefaultImageName == null) {
                                val uri = try {
                                    firstColorImage.url.toUri()
                                } catch (e: Exception) {
                                    null
                                }
                                newState.copy(
                                    activeBackgroundUri = uri,
                                    activeBackgroundResId = null,
                                    selectedDefaultImageName = firstColorImage.imageName,
                                    selectedBackgroundFilter = BackgroundFilterType.COLOR
                                )
                            } else {
                                newState
                            }
                        }
                        SooumLog.d(TAG, "loadCardDefaultImages() success: ${result.data.defaultImages.size} categories loaded")
                    }
                    is DomainResult.Failure -> {
                        _uiState.update {
                            it.copy(
                                cardDefaultImagesByCategory = emptyMap()
                            )
                        }
                        SooumLog.e(TAG, "loadCardDefaultImages() failed: ${result.error}")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cardDefaultImagesByCategory = emptyMap()
                    )
                }
                SooumLog.e(TAG, "loadCardDefaultImages() exception: ${e.message}")
            }
        }
    }
}

private const val TAG = "WriteViewModel"
