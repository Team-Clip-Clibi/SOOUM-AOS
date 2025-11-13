package com.phew.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.phew.core_common.DomainResult
import com.phew.domain.dto.FollowData
import com.phew.domain.dto.ProfileInfo
import com.phew.domain.dto.ProfileCard
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getMyProfile: GetMyProfileInfo,
    private val getFeedCard: GetProfileFeedCard,
    private val getCommentCard: GetProfileCommentCard,
    private val getFollower: GetFollower,
    private val getFollowing: GetFollowing,
    private val getOtherProfile: GetOtherProfile,
    private val followUser: SendFollowUser,
    private val unFollowUser: SendUnFollowUser,
    private val blockUser: SendBlockUser,
    private val unBlockUser: SendUnBlockUser,
) : ViewModel() {
    private val _uiState = MutableStateFlow(Profile())
    val uiState: StateFlow<Profile> = _uiState.asStateFlow()

    fun refreshMyProfile() {
        _uiState.update { state -> state.copy(isRefreshing = true) }
        myProfile()
    }

    fun refreshOtherProfile(profileId: Long) {
        _uiState.update { state -> state.copy(isRefreshing = true) }
        otherProfile(profileId = profileId)
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
                            profileFeedCard = getFeedCard(userId = request.data.userId).cachedIn(
                                viewModelScope
                            ),
                            profileCommentCard = getCommentCard().cachedIn(viewModelScope),
                            follow = getFollower(profileId = request.data.userId).cachedIn(
                                viewModelScope
                            ),
                            following = getFollowing(profileId = request.data.userId).cachedIn(
                                viewModelScope
                            ),
                            isRefreshing = false,
                            nickname = "",
                            userId = 0L
                        )
                    }
                }
            }
        }
    }

    fun otherProfile(profileId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { state ->
                state.copy(
                    profileInfo = UiState.Loading,
                    otherProfileId = 0L
                )
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
                            profileFeedCard = getFeedCard(userId = request.data.userId).cachedIn(
                                viewModelScope
                            ),
                            profileCommentCard = getCommentCard().cachedIn(viewModelScope),
                            follow = getFollower(profileId = request.data.userId).cachedIn(
                                viewModelScope
                            ),
                            following = getFollowing(profileId = request.data.userId).cachedIn(
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
                    if (isMyProfile) refreshMyProfile() else otherProfile(_uiState.value.otherProfileId)
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
                    if (isMyProfile) refreshMyProfile() else otherProfile(_uiState.value.otherProfileId)
                }
            }
        }
    }

    fun setFollowUserId(data: FollowData) {
        _uiState.update { state -> state.copy(userId = data.memberId, nickname = data.nickname) }
    }

    fun changeNickName(data: String) {
        _uiState.update { state ->
            state.copy(changeNickName = data)
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
    val isRefreshing: Boolean = false,
    val userId: Long = 0L,
    val nickname: String = "",
    val otherProfileId: Long = 0L,
    var changeNickName : String = ""
)

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Fail(val errorMessage: String) : UiState<Nothing>
}