package com.phew.profile

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.phew.core.ui.model.CameraCaptureRequest
import com.phew.core.ui.model.navigation.CardDetailArgs
import com.phew.core.ui.model.navigation.FollowArgs
import com.phew.core_common.DomainResult
import com.phew.domain.dto.FollowData
import com.phew.domain.dto.ProfileInfo
import com.phew.domain.dto.ProfileCard
import com.phew.domain.usecase.CheckCardAlreadyDelete
import com.phew.domain.usecase.CheckIsMyProfile
import com.phew.domain.usecase.CheckNickName
import com.phew.domain.usecase.CreateImageFile
import com.phew.domain.usecase.FinishTakePicture
import com.phew.domain.usecase.GetFollower
import com.phew.domain.usecase.GetFollowing
import com.phew.domain.usecase.GetMyProfileInfo
import com.phew.domain.usecase.GetOtherProfile
import com.phew.domain.usecase.GetProfileCommentCard
import com.phew.domain.usecase.GetProfileFeedCard
import com.phew.domain.usecase.SendBlockUser
import com.phew.domain.usecase.SendFollowUser
import com.phew.domain.usecase.SendUnBlockUser
import com.phew.domain.usecase.SendUnFollowUser
import com.phew.domain.usecase.UpdateProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getMyProfile: GetMyProfileInfo,
    private val getFeedCard: GetProfileFeedCard,
    private val getCommentCard: GetProfileCommentCard,
    private val unFollowUser: SendUnFollowUser,
    private val getFollower: GetFollower,
    private val getFollowing: GetFollowing,
    private val getOtherProfile: GetOtherProfile,
    private val followUser: SendFollowUser,
    private val blockUser: SendBlockUser,
    private val unBlockUser: SendUnBlockUser,
    private val checkNickName: CheckNickName,
    private val createFile: CreateImageFile,
    private val finishPhoto: FinishTakePicture,
    private val updateProfile: UpdateProfile,
    private val checkCardDelete: CheckCardAlreadyDelete,
    private val checkMyProfile : CheckIsMyProfile
) : ViewModel() {
    private val _uiState = MutableStateFlow(Profile())
    val uiState: StateFlow<Profile> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<ProfileUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    private val rawArgs: Any? = savedStateHandle[PROFILE_ARGS_KEY]
    private val followArgs: FollowArgs? = rawArgs as? FollowArgs

    val currentUserId: Long = followArgs?.userId ?: 0L

    fun refreshMyProfile() {
        _uiState.update { state -> state.copy(isRefreshing = true) }
        myProfile()
    }

    fun refreshOtherProfile(profileId: Long) {
        _uiState.update { state -> state.copy(isRefreshing = true) }
        otherProfile(profileId = profileId)
    }

    fun checkIsMyProfile(userId: Long, nickname: String) {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = checkMyProfile(CheckIsMyProfile.Param(userId = userId, nickName = nickname))) {
                is DomainResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            event = UiState.Fail(result.error)
                        )
                    }
                }

                is DomainResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            checkIsMyProfile = UiState.Success(result.data)
                        )
                    }
                }
            }
        }
    }

    fun initCheckIsMyProfile() {
        _uiState.update { state ->
            state.copy(
                checkIsMyProfile = UiState.None
            )
        }
    }

    fun myProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { state -> state.copy(profileInfo = UiState.Loading) }
            when (val request = getMyProfile()) {
                is DomainResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            profileInfo = UiState.Fail(request.error),
                            isRefreshing = false
                        )
                    }
                }

                is DomainResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            profileInfo = UiState.Success(request.data),
                            profileFeedCard = getFeedCard(userId = request.data.userId)
                                .cachedIn(viewModelScope)
                                .combine(_uiState.map { it.deletedCardIds }.distinctUntilChanged()) { pagingData, deletedIds ->
                                    pagingData.filter { !deletedIds.contains(it.cardId) }
                                },
                            profileCommentCard = getCommentCard().cachedIn(viewModelScope),
                            follow = getFollower(profileId = request.data.userId).cachedIn(
                                viewModelScope
                            ),
                            following = getFollowing(profileId = request.data.userId).cachedIn(
                                viewModelScope
                            ),
                            isRefreshing = false,
                            nickname = "",
                            userId = 0L,
                            newProfileImageUri = if (request.data.profileImgName.trim()
                                    .isEmpty()
                            ) listOf(Uri.EMPTY) else listOf(Uri.EMPTY) + request.data.profileImageUrl.toUri()
                        )
                    }
                }
            }
        }
    }

    fun otherProfile(profileId: Long , isShowLoading : Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            if(isShowLoading){
                _uiState.update { state ->
                    state.copy(
                        profileInfo = UiState.Loading,
                        otherProfileId = 0L
                    )
                }
            }
            when (val request = getOtherProfile(GetOtherProfile.Param(profileId = profileId))) {
                is DomainResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            profileInfo = UiState.Fail(request.error),
                            isRefreshing = false
                        )
                    }
                }

                is DomainResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            profileInfo = UiState.Success(request.data),
                            profileFeedCard = getFeedCard(userId = request.data.userId)
                                .cachedIn(viewModelScope)
                                .combine(_uiState.map { it.deletedCardIds }.distinctUntilChanged()) { pagingData, deletedIds ->
                                    pagingData.filter { !deletedIds.contains(it.cardId) }
                                },
                            profileCommentCard = getCommentCard().cachedIn(viewModelScope),
                            follow = getFollower(profileId = request.data.userId).map { pagingData ->
                                val uniqueIds = mutableSetOf<Long>()
                                pagingData.filter { user ->
                                    uniqueIds.add(user.memberId)
                                }
                            }.cachedIn(
                                viewModelScope
                            ),
                            following = getFollowing(profileId = request.data.userId).map { pagingData ->
                                val uniqueIds = mutableSetOf<Long>()
                                pagingData.filter { user ->
                                    uniqueIds.add(user.memberId)
                                }
                            }.cachedIn(
                                viewModelScope
                            ),
                            isRefreshing = false,
                            otherProfileId = profileId
                        )
                    }
                }
            }
        }
    }

    fun block(userId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { state -> state.copy(event = UiState.Loading) }
            when (val request = blockUser(SendBlockUser.Param(userId = userId))) {
                is DomainResult.Failure -> {
                    _uiState.update { state -> state.copy(event = UiState.Fail(request.error)) }
                }

                is DomainResult.Success -> {
                    _uiState.update { state -> state.copy(event = UiState.Success(Unit)) }
                    refreshOtherProfile(profileId = userId)
                }
            }
        }
    }

    fun unBlock(userId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { state -> state.copy(event = UiState.Loading) }
            when (val request = unBlockUser(SendUnBlockUser.Param(userId = userId))) {
                is DomainResult.Failure -> {
                    _uiState.update { state -> state.copy(event = UiState.Fail(request.error)) }
                }

                is DomainResult.Success -> {
                    _uiState.update { state -> state.copy(event = UiState.Success(Unit)) }
                    refreshOtherProfile(profileId = userId)
                }
            }
        }
    }

    fun followUser(userId: Long, isRefresh: Boolean = false, isMyProfile: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { state -> state.copy(event = UiState.Loading) }
            when (val request = followUser(SendFollowUser.Param(userId = userId))) {
                is DomainResult.Failure -> {
                    _uiState.update { state -> state.copy(event = UiState.Fail(request.error)) }
                }

                is DomainResult.Success -> {
                    _uiState.update { state -> state.copy(event = UiState.Success(Unit)) }
                    if (!isRefresh) return@launch
                    if (isMyProfile) refreshMyProfile() else otherProfile(
                        _uiState.value.otherProfileId,
                        isShowLoading = false
                    )
                }
            }
        }
    }

    fun unFollowUser(userId: Long, isRefresh: Boolean = false, isMyProfile: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { state -> state.copy(event = UiState.Loading) }
            when (val request = unFollowUser(SendUnFollowUser.Param(userId = userId))) {
                is DomainResult.Failure -> {
                    _uiState.update { state -> state.copy(event = UiState.Fail(request.error)) }
                }

                is DomainResult.Success -> {
                    _uiState.update { state -> state.copy(event = UiState.Success(Unit)) }
                    if (!isRefresh) return@launch
                    if (isMyProfile) refreshMyProfile() else otherProfile(
                        _uiState.value.otherProfileId,
                        isShowLoading = false
                    )
                }
            }
        }
    }

    fun setFollowUserId(data: FollowData) {
        _uiState.update { state -> state.copy(userId = data.memberId, nickname = data.nickname) }
    }

    fun update() {
        if (!_uiState.value.changeProfile) return
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = updateProfile(
                UpdateProfile.Param(
                    nickName = if (_uiState.value.changeNickName == (_uiState.value.profileInfo as UiState.Success).data.nickname) null else _uiState.value.changeNickName,
                    imgName = when {
                        !_uiState.value.useAlbum && !_uiState.value.useCamera && _uiState.value.newProfileImageUri.size == 2 -> (_uiState.value.profileInfo as UiState.Success).data.profileImgName
                        else -> ""
                    },
                    profileImage = if(_uiState.value.newProfileImageUri.last() == Uri.EMPTY) null else _uiState.value.newProfileImageUri.last().toString(),
                    isImageChange = _uiState.value.imageChange
                )
            )) {
                is DomainResult.Failure -> {
                    _uiState.update { state -> state.copy(updateProfile = UiState.Fail(result.error)) }
                }

                is DomainResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            updateProfile = UiState.Success(Unit),
                        )
                    }
                    myProfile()
                }
            }
        }
    }

    fun changeNickName(data: String) {
        _uiState.update { state ->
            state.copy(changeNickName = data)
        }
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = checkNickName(CheckNickName.Param(data))) {
                is DomainResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(nickNameHint = UiState.Fail(result.error), changeProfile = false)
                    }
                }

                is DomainResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            nickNameHint = UiState.Success(result.data),
                            changeProfile = result.data
                        )
                    }
                }
            }
        }
    }

    fun selectAlbum() {
        _uiState.update { state ->
            state.copy(
                useAlbum = true,
                useCamera = false,
                imageChange = true
            )
        }
    }

    fun selectCamera() {
        _uiState.update { state ->
            state.copy(
                useAlbum = false,
                useCamera = true,
                imageChange = true
            )
        }
    }

    fun selectDefaultImage() {
        _uiState.update { state ->
            state.copy(
                useAlbum = false,
                useCamera = false,
                changeProfile = true,
                imageChange = true,
                newProfileImageUri = listOf(Uri.EMPTY)
            )
        }
    }

    fun onProfileAlbumRequestConsumed() {
        _uiState.update { state ->
            state.copy(useAlbum = false)
        }
    }

    fun onProfileCameraPermissionRequestConsumed() {
        _uiState.update { state -> state.copy(useCamera = false) }
    }

    fun onAlbumPicked(uri: Uri) {
        _uiState.update {
            it.copy(
                newProfileImageUri = it.newProfileImageUri + uri,
                changeProfile = true
            )
        }
    }

    fun onProfileCameraPermissionResult(granted: Boolean) {
        if (!granted) return
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = createFile()) {
                is DomainResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(errorMessage = result.error, changeProfile = false)
                    }
                }

                is DomainResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            pendingProfileCameraCapture = CameraCaptureRequest(
                                id = System.currentTimeMillis(),
                                uri = result.data
                            ),
                            changeProfile = true
                        )
                    }
                }
            }
        }
    }

    fun closeFile(data: Uri, success: Boolean) {
        if (!success) return
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = finishPhoto(FinishTakePicture.Param(data))) {
                is DomainResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            errorMessage = result.error,
                            changeProfile = false
                        )
                    }
                }

                is DomainResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            newProfileImageUri = state.newProfileImageUri + result.data,
                            changeProfile = true
                        )
                    }
                }
            }
        }
    }

    fun onProfileCameraCaptureLaunched() {
        _uiState.update { state ->
            state.copy(pendingProfileCameraCapture = null)
        }
    }

    fun initEditProfile() {
        _uiState.update { state ->
            state.copy(
                pendingProfileCameraCapture = null,
                changeNickName = null,
                newProfileImageUri = listOf(Uri.EMPTY),
                errorMessage = "",
                useCamera = false,
                useAlbum = false,
                updateProfile = UiState.Loading
            )
        }
    }

    fun setImageDialog(result: Boolean) {
        _uiState.update { state ->
            state.copy(
                imageDialog = result,
                updateProfile = if (!result) state.updateProfile else UiState.Loading,
                newProfileImageUri = if (!result) {
                    if (state.newProfileImageUri.size > 1) state.newProfileImageUri.dropLast(1) else listOf(
                        Uri.EMPTY
                    )
                } else state.newProfileImageUri
            )
        }
    }

    fun navigateToDetail(cardId: Long) {
        if (_uiState.value.checkCardDelete is UiState.Loading) return
        
        viewModelScope.launch {
            _uiState.update { state -> state.copy(checkCardDelete = UiState.Loading) }
            when (val result = checkCardDelete(CheckCardAlreadyDelete.Param(cardId = cardId))) {
                is DomainResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(checkCardDelete = UiState.Fail(result.error))
                    }
                }

                is DomainResult.Success -> {
                    if (result.data) {
                        // 삭제된 경우 ID를 전달
                        _uiState.update { state -> 
                            state.copy(checkCardDelete = UiState.Success(cardId))
                        }
                    } else {
                        // 삭제되지 않음
                        _uiState.update { state -> state.copy(checkCardDelete = UiState.None) }
                        _uiEffect.emit(
                            ProfileUiEffect.NavigateToDetail(
                                CardDetailArgs(cardId)
                            )
                        )
                    }
                }
            }
        }
    }

    fun removeDeletedCard(cardId: Long) {
        _uiState.update { state ->
            state.copy(
                deletedCardIds = state.deletedCardIds + cardId,
                checkCardDelete = UiState.None
            )
        }
    }
}

data class Profile(
    val profileInfo: UiState<ProfileInfo> = UiState.Loading,
    val profileFeedCard: Flow<PagingData<ProfileCard>> = emptyFlow(),
    val profileCommentCard: Flow<PagingData<ProfileCard>> = emptyFlow(),
    val follow: Flow<PagingData<FollowData>> = emptyFlow(),
    val following: Flow<PagingData<FollowData>> = emptyFlow(),
    val event: UiState<Unit> = UiState.Success(Unit),
    val updateProfile: UiState<Unit> = UiState.Loading,
    val isRefreshing: Boolean = false,
    val userId: Long = 0L,
    val nickname: String = "",
    val otherProfileId: Long = 0L,
    val nickNameHint: UiState<Boolean> = UiState.Loading,
    val pendingProfileCameraCapture: CameraCaptureRequest? = null,
    val useAlbum: Boolean = false,
    val useCamera: Boolean = false,
    var changeNickName: String? = null,
    val newProfileImageUri: List<Uri> = listOf(Uri.EMPTY),
    val errorMessage: String = "",
    val changeProfile: Boolean = false,
    val imageChange: Boolean = false,
    val imageDialog: Boolean = false,
    val checkCardDelete: UiState<Long> = UiState.None,
    val deletedCardIds: Set<Long> = emptySet(),
    val checkIsMyProfile: UiState<Pair<Boolean, Long>> = UiState.None,
)

sealed interface UiState<out T> {
    data object None : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Fail(val errorMessage: String) : UiState<Nothing>
}

sealed interface ProfileUiEffect {
    data class NavigateToDetail(val cardDetailArgs: CardDetailArgs): ProfileUiEffect
}