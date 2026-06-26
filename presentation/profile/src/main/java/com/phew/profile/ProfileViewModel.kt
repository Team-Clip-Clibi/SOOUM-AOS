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
import com.phew.core_common.errorMessage
import com.phew.domain.dto.FollowData
import com.phew.domain.dto.ProfileCard
import com.phew.domain.dto.ProfileInfo
import com.phew.domain.usecase.UpdateProfile
import com.phew.domain.usecase.orchestrator.ProfileUseCaseOrchestrator
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
    private val profileOrchestrator: ProfileUseCaseOrchestrator,
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
            profileOrchestrator.checkIsMyProfile(userId = userId, nickName = nickname).fold(
                onSuccess = { isMyProfile ->
                    _uiState.update { state ->
                        state.copy(
                            checkIsMyProfile = UiState.Success(isMyProfile)
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update { state ->
                        state.copy(
                            event = UiState.Fail(throwable.toUiMessage())
                        )
                    }
                }
            )
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
            profileOrchestrator.myProfile().fold(
                onSuccess = { profile ->
                    _uiState.update { state ->
                        state.copy(
                            profileInfo = UiState.Success(profile),
                            profileFeedCard = profileOrchestrator.profileFeedCards(userId = profile.userId)
                                .cachedIn(viewModelScope)
                                .combine(_uiState.map { it.deletedCardIds }.distinctUntilChanged()) { pagingData, deletedIds ->
                                    pagingData.filter { !deletedIds.contains(it.cardId) }
                                },
                            profileCommentCard = profileOrchestrator.profileCommentCards().cachedIn(viewModelScope),
                            follow = profileOrchestrator.followers(profileId = profile.userId).map { pagingData ->
                                val uniqueIds = mutableSetOf<Long>()
                                pagingData.filter { user ->
                                    uniqueIds.add(user.memberId)
                                }
                            }.cachedIn(
                                viewModelScope
                            ),
                            following = profileOrchestrator.followings(profileId = profile.userId).map { pagingData ->
                                val uniqueIds = mutableSetOf<Long>()
                                pagingData.filter { user ->
                                    uniqueIds.add(user.memberId)
                                }
                            }.cachedIn(
                                viewModelScope
                            ),
                            isRefreshing = false,
                            nickname = "",
                            userId = 0L,
                            changeBio = null,
                            changeProfile = false,
                            imageChange = false,
                            newProfileImageUri = if (profile.profileImgName.trim()
                                    .isEmpty()
                            ) listOf(Uri.EMPTY) else listOf(Uri.EMPTY) + profile.profileImageUrl.toUri()
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update { state ->
                        state.copy(
                            profileInfo = UiState.Fail(throwable.toUiMessage()),
                            isRefreshing = false
                        )
                    }
                }
            )
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
            profileOrchestrator.otherProfile(profileId = profileId).fold(
                onSuccess = { profile ->
                    _uiState.update { state ->
                        state.copy(
                            profileInfo = UiState.Success(profile),
                            profileFeedCard = profileOrchestrator.profileFeedCards(userId = profile.userId)
                                .cachedIn(viewModelScope)
                                .combine(_uiState.map { it.deletedCardIds }.distinctUntilChanged()) { pagingData, deletedIds ->
                                    pagingData.filter { !deletedIds.contains(it.cardId) }
                                },
                            profileCommentCard = profileOrchestrator.profileCommentCards().cachedIn(viewModelScope),
                            follow = profileOrchestrator.followers(profileId = profile.userId).map { pagingData ->
                                val uniqueIds = mutableSetOf<Long>()
                                pagingData.filter { user ->
                                    uniqueIds.add(user.memberId)
                                }
                            }.cachedIn(
                                viewModelScope
                            ),
                            following = profileOrchestrator.followings(profileId = profile.userId).map { pagingData ->
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
                },
                onFailure = { throwable ->
                    _uiState.update { state ->
                        state.copy(
                            profileInfo = UiState.Fail(throwable.toUiMessage()),
                            isRefreshing = false
                        )
                    }
                }
            )
        }
    }

    fun block(userId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { state -> state.copy(event = UiState.Loading) }
            profileOrchestrator.blockUser(userId = userId).fold(
                onSuccess = {
                    _uiState.update { state -> state.copy(event = UiState.Success(Unit)) }
                    refreshOtherProfile(profileId = userId)
                },
                onFailure = { throwable ->
                    _uiState.update { state -> state.copy(event = UiState.Fail(throwable.toUiMessage())) }
                }
            )
        }
    }

    fun unBlock(userId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { state -> state.copy(event = UiState.Loading) }
            profileOrchestrator.unBlockUser(userId = userId).fold(
                onSuccess = {
                    _uiState.update { state -> state.copy(event = UiState.Success(Unit)) }
                    refreshOtherProfile(profileId = userId)
                },
                onFailure = { throwable ->
                    _uiState.update { state -> state.copy(event = UiState.Fail(throwable.toUiMessage())) }
                }
            )
        }
    }

    fun followUser(userId: Long, isRefresh: Boolean = false, isMyProfile: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { state -> state.copy(event = UiState.Loading) }
            profileOrchestrator.followUser(userId = userId).fold(
                onSuccess = {
                    _uiState.update { state -> state.copy(event = UiState.Success(Unit)) }
                    if (!isRefresh) return@launch
                    if (isMyProfile) refreshMyProfile() else otherProfile(
                        _uiState.value.otherProfileId,
                        isShowLoading = false
                    )
                },
                onFailure = { throwable ->
                    _uiState.update { state -> state.copy(event = UiState.Fail(throwable.toUiMessage())) }
                }
            )
        }
    }

    fun unFollowUser(userId: Long, isRefresh: Boolean = false, isMyProfile: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { state -> state.copy(event = UiState.Loading) }
            profileOrchestrator.unFollowUser(userId = userId).fold(
                onSuccess = {
                    _uiState.update { state -> state.copy(event = UiState.Success(Unit)) }
                    if (!isRefresh) return@launch
                    if (isMyProfile) refreshMyProfile() else otherProfile(
                        _uiState.value.otherProfileId,
                        isShowLoading = false
                    )
                },
                onFailure = { throwable ->
                    _uiState.update { state -> state.copy(event = UiState.Fail(throwable.toUiMessage())) }
                }
            )
        }
    }

    fun setFollowUserId(data: FollowData) {
        _uiState.update { state -> state.copy(userId = data.memberId, nickname = data.nickname) }
    }

    fun update() {
        if (!_uiState.value.changeProfile) return
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _uiState.value
            val profile = (currentState.profileInfo as UiState.Success).data
            val nextNickname = currentState.changeNickName
                ?.takeIf { it != profile.nickname }
                ?: profile.nickname
            val nextBio = currentState.changeBio ?: profile.bio
            profileOrchestrator.updateProfile(
                UpdateProfile.Param(
                    nickName = currentState.changeNickName?.takeIf { it != profile.nickname },
                    imgName = when {
                        !currentState.useAlbum && !currentState.useCamera && currentState.newProfileImageUri.size == 2 -> profile.profileImgName
                        else -> ""
                    },
                    profileBio = nextBio,
                    profileImage = if (currentState.newProfileImageUri.last() == Uri.EMPTY) null else currentState.newProfileImageUri.last().toString(),
                    isImageChange = currentState.imageChange
                )
            ).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            updateProfile = UiState.Success(Unit),
                            profileInfo = UiState.Success(
                                profile.copy(
                                    nickname = nextNickname,
                                    bio = nextBio
                                )
                            )
                        )
                    }
                    myProfile()
                },
                onFailure = { throwable ->
                    _uiState.update { state -> state.copy(updateProfile = UiState.Fail(throwable.toUiMessage())) }
                }
            )
        }
    }

    fun changeNickName(data: String) {
        _uiState.update { state ->
            state.copy(
                changeNickName = data,
                nickNameHint = UiState.Loading
            ).withChangeProfileState()
        }
        val currentProfile = (_uiState.value.profileInfo as? UiState.Success)?.data ?: return
        if (data == currentProfile.nickname) {
            _uiState.update { state ->
                state.copy(nickNameHint = UiState.Loading).withChangeProfileState()
            }
            return
        }
        if (data.length < 2) {
            _uiState.update { state ->
                state.copy(nickNameHint = UiState.Success(false)).withChangeProfileState()
            }
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            profileOrchestrator.checkNickName(data).fold(
                onSuccess = { available ->
                    _uiState.update { state ->
                        if (state.changeNickName != data) {
                            state
                        } else {
                            state.copy(nickNameHint = UiState.Success(available)).withChangeProfileState()
                        }
                    }
                },
                onFailure = { throwable ->
                    _uiState.update { state ->
                        if (state.changeNickName != data) {
                            state
                        } else {
                            state.copy(nickNameHint = UiState.Fail(throwable.toUiMessage())).withChangeProfileState()
                        }
                    }
                }
            )
        }
    }

    fun changeBio(data: String) {
        _uiState.update { state ->
            state.copy(changeBio = data).withChangeProfileState()
        }
    }

    fun selectAlbum() {
        _uiState.update { state ->
            state.copy(
                useAlbum = true,
                useCamera = false,
                imageChange = true
            ).withChangeProfileState()
        }
    }

    fun selectCamera() {
        _uiState.update { state ->
            state.copy(
                useAlbum = false,
                useCamera = true,
                imageChange = true
            ).withChangeProfileState()
        }
    }

    fun selectDefaultImage() {
        _uiState.update { state ->
            state.copy(
                useAlbum = false,
                useCamera = false,
                imageChange = true,
                newProfileImageUri = listOf(Uri.EMPTY)
            ).withChangeProfileState()
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
                imageChange = true
            ).withChangeProfileState()
        }
    }

    fun onProfileCameraPermissionResult(granted: Boolean) {
        if (!granted) return
        viewModelScope.launch(Dispatchers.IO) {
            profileOrchestrator.createImageFile().fold(
                onSuccess = { uri ->
                    _uiState.update { state ->
                        state.copy(
                            pendingProfileCameraCapture = CameraCaptureRequest(
                                id = System.currentTimeMillis(),
                                uri = uri
                            ),
                            imageChange = true
                        ).withChangeProfileState()
                    }
                },
                onFailure = { throwable ->
                    _uiState.update { state ->
                        state.copy(errorMessage = throwable.toUiMessage(), changeProfile = false)
                    }
                }
            )
        }
    }

    fun closeFile(data: Uri, success: Boolean) {
        if (!success) return
        viewModelScope.launch(Dispatchers.IO) {
            profileOrchestrator.finishTakePicture(data).fold(
                onSuccess = { uri ->
                    _uiState.update { state ->
                        state.copy(
                            newProfileImageUri = state.newProfileImageUri + uri,
                            imageChange = true
                        ).withChangeProfileState()
                    }
                },
                onFailure = { throwable ->
                    _uiState.update { state ->
                        state.copy(
                            errorMessage = throwable.toUiMessage(),
                            changeProfile = false
                        )
                    }
                }
            )
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
                changeBio = null,
                newProfileImageUri = listOf(Uri.EMPTY),
                errorMessage = "",
                useCamera = false,
                useAlbum = false,
                changeProfile = false,
                imageChange = false,
                nickNameHint = UiState.Loading,
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
                } else state.newProfileImageUri,
                imageChange = if (!result) false else state.imageChange
            ).withChangeProfileState()
        }
    }

    fun navigateToDetail(cardId: Long) {
        if (_uiState.value.checkCardDelete is UiState.Loading) return
        
        viewModelScope.launch {
            _uiState.update { state -> state.copy(checkCardDelete = UiState.Loading) }
            profileOrchestrator.checkCardDeleted(cardId = cardId).fold(
                onSuccess = { isDeleted ->
                    if (isDeleted) {
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
                },
                onFailure = { throwable ->
                    _uiState.update { state ->
                        state.copy(checkCardDelete = UiState.Fail(throwable.toUiMessage()))
                    }
                }
            )
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
    val changeBio: String? = null,
    val newProfileImageUri: List<Uri> = listOf(Uri.EMPTY),
    val errorMessage: String = "",
    val changeProfile: Boolean = false,
    val imageChange: Boolean = false,
    val imageDialog: Boolean = false,
    val checkCardDelete: UiState<Long> = UiState.None,
    val deletedCardIds: Set<Long> = emptySet(),
    val checkIsMyProfile: UiState<Pair<Boolean, Long>> = UiState.None,
)

private fun Profile.withChangeProfileState(): Profile {
    return copy(changeProfile = canUpdateProfile())
}

private fun Profile.canUpdateProfile(): Boolean {
    val profile = (profileInfo as? UiState.Success)?.data ?: return false
    val isNicknameChanged = changeNickName != null && changeNickName != profile.nickname
    val isBioChanged = changeBio != null && changeBio != profile.bio
    val hasChanges = imageChange || isNicknameChanged || isBioChanged
    if (!hasChanges) return false
    if (!isNicknameChanged) return true
    return changeNickName.orEmpty().length >= 2 &&
        (nickNameHint as? UiState.Success<Boolean>)?.data == true
}

sealed interface UiState<out T> {
    data object None : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Fail(val errorMessage: String) : UiState<Nothing>
}

sealed interface ProfileUiEffect {
    data class NavigateToDetail(val cardDetailArgs: CardDetailArgs): ProfileUiEffect
}

private fun Throwable.toUiMessage(): String = Result.failure<Unit>(this).errorMessage()
