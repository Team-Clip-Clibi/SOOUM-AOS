package com.phew.presentation.detail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.phew.core_common.DomainResult
import com.phew.core_common.log.SooumLog
import com.phew.domain.dto.CardComment
import com.phew.domain.dto.CardDetail
import com.phew.domain.usecase.BlockMember
import com.phew.domain.usecase.DeleteCard
import com.phew.domain.usecase.GetCardComments
import com.phew.domain.usecase.GetCardCommentsPaging
import com.phew.domain.usecase.GetCardDetail
import com.phew.domain.usecase.LikeCard
import com.phew.domain.usecase.UnblockMember
import com.phew.domain.usecase.UnlikeCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class CardDetailError {
    COMMENTS_LOAD_FAILED,
    CARD_LOAD_FAILED,
    NETWORK_ERROR
}

data class CardDetailUiState(
    val isLoading: Boolean = false,
    val cardDetail: CardDetail? = null,
    val comments: List<CardComment> = emptyList(),
    val error: CardDetailError? = null,
    val isLikeLoading: Boolean = false,
    val isBlockLoading: Boolean = false,
    val blockSuccess: Boolean = false,
    val blockedMemberId: Long? = null,
    val blockedNickname: String? = null
)

@HiltViewModel
class CardDetailViewModel @Inject constructor(
    private val getCardDetail: GetCardDetail,
    private val getCardComments: GetCardComments,
    private val commentPaging: GetCardCommentsPaging,
    private val likeCard: LikeCard,
    private val unLikeCard: UnlikeCard,
    private val deleteCard: DeleteCard,
    private val blockMember: BlockMember,
    private val unblockMember: UnblockMember
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardDetailUiState())
    val uiState: StateFlow<CardDetailUiState> = _uiState.asStateFlow()

    private val _pagingRequest = MutableStateFlow<PagingRequest>(PagingRequest.None)

    @OptIn(ExperimentalCoroutinesApi::class)
    val commentsPagingData: Flow<PagingData<CardComment>> = _pagingRequest
        .flatMapLatest { request ->
            when (request) {
                is PagingRequest.None -> flowOf(PagingData.empty())
                is PagingRequest.Ready -> commentPaging(request.param)
            }
        }
        .cachedIn(viewModelScope)

    fun requestComment(cardId: Long, latitude: Double? = null, longitude: Double? = null) {
        _pagingRequest.update { state ->
            if (state is PagingRequest.Ready && state.param.cardId == cardId) {
                return@update state
            }
            PagingRequest.Ready(
                GetCardCommentsPaging.Param(
                    cardId = cardId,
                    latitude = latitude,
                    longitude = longitude
                )
            )
        }
    }

    fun loadCardDetail(cardId: Long) {
        viewModelScope.launch {
            try {
                SooumLog.d(TAG, "loadCardDetail() start cardId: $cardId")
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)

                val cardDetailDeferred = async { getCardDetail(GetCardDetail.Param(cardId)) }
                val commentsDeferred = async { getCardComments(GetCardComments.Param(cardId)) }

                val cardDetailResult = cardDetailDeferred.await()
                val commentsResult = commentsDeferred.await()

                SooumLog.d(TAG, "loadCardDetail() results - cardDetail: $cardDetailResult, comments: $commentsResult")

                when {
                    cardDetailResult is DomainResult.Success && commentsResult is DomainResult.Success -> {
                        SooumLog.d(TAG, "loadCardDetail() success cardDetail: ${cardDetailResult.data}, comments: ${commentsResult.data}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            cardDetail = cardDetailResult.data,
                            comments = commentsResult.data
                        )
                    }
                    cardDetailResult is DomainResult.Success && commentsResult is DomainResult.Failure -> {
                        SooumLog.w(TAG, "loadCardDetail() cardDetail success but comments failed: ${commentsResult.error}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            cardDetail = cardDetailResult.data,
                            comments = emptyList(), // 빈 댓글 목록으로 설정
                            error = CardDetailError.COMMENTS_LOAD_FAILED
                        )
                    }
                    cardDetailResult is DomainResult.Failure -> {
                        SooumLog.e(TAG, "loadCardDetail() cardDetail failed: ${cardDetailResult.error}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = CardDetailError.CARD_LOAD_FAILED
                        )
                    }
                    commentsResult is DomainResult.Failure -> {
                        SooumLog.e(TAG, "loadCardDetail() comments failed: ${commentsResult.error}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = CardDetailError.COMMENTS_LOAD_FAILED
                        )
                    }
                }
            } catch (e: Exception) {
                SooumLog.e(TAG, "loadCardDetail() exception: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = CardDetailError.NETWORK_ERROR
                )
            }
        }
    }

    fun toggleLike(cardId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLikeLoading = true)

            val currentDetail = _uiState.value.cardDetail
            if (currentDetail == null) {
                _uiState.value = _uiState.value.copy(isLikeLoading = false)
                return@launch
            }

            val result = if (currentDetail.isLike) {
                unLikeCard(cardId)
            } else {
                likeCard(cardId)
            }

            when (result) {
                is DomainResult.Success -> {
                    val updatedDetail = currentDetail.copy(
                        isLike = !currentDetail.isLike,
                        likeCount = if (currentDetail.isLike) {
                            currentDetail.likeCount - 1
                        } else {
                            currentDetail.likeCount + 1
                        }
                    )
                    _uiState.value = _uiState.value.copy(
                        cardDetail = updatedDetail,
                        isLikeLoading = false
                    )
                }
                is DomainResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isLikeLoading = false,
                        error = CardDetailError.NETWORK_ERROR
                    )
                }
            }
        }
    }

    fun blockMember(toMemberId: Long, nickname: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBlockLoading = true)
            
            val result = blockMember(BlockMember.Param(toMemberId))
            
            when (result) {
                is DomainResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isBlockLoading = false,
                        blockSuccess = true,
                        blockedMemberId = toMemberId,
                        blockedNickname = nickname
                    )
                }
                is DomainResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isBlockLoading = false,
                        error = CardDetailError.NETWORK_ERROR
                    )
                }
            }
        }
    }

    fun unblockMember() {
        val memberId = _uiState.value.blockedMemberId ?: return
        
        viewModelScope.launch {
            val result = unblockMember(UnblockMember.Param(memberId))
            
            when (result) {
                is DomainResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        blockedMemberId = null,
                        blockedNickname = null,
                        blockSuccess = false
                    )
                }
                is DomainResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        error = CardDetailError.NETWORK_ERROR
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearBlockSuccess() {
        _uiState.value = _uiState.value.copy(blockSuccess = false)
    }
}

sealed class PagingRequest {
    data object None : PagingRequest()
    data class Ready(val param: GetCardCommentsPaging.Param) : PagingRequest()
}

private const val TAG = "CardDetailViewModel"