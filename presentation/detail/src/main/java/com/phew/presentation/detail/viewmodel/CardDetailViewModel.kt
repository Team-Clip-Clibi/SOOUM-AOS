package com.phew.presentation.detail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phew.core_common.DomainResult
import com.phew.core_common.log.SooumLog
import com.phew.domain.dto.CardComment
import com.phew.domain.dto.CardDetail
import com.phew.domain.usecase.DeleteCard
import com.phew.domain.usecase.GetCardComments
import com.phew.domain.usecase.GetCardDetail
import com.phew.domain.usecase.GetMoreCardComments
import com.phew.domain.usecase.LikeCard
import com.phew.domain.usecase.PostCardReply
import com.phew.domain.usecase.UnlikeCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import javax.inject.Inject

data class CardDetailUiState(
    val isLoading: Boolean = false,
    val cardDetail: CardDetail? = null,
    val comments: List<CardComment> = emptyList(),
    val error: String? = null,
    val isLikeLoading: Boolean = false
)

@HiltViewModel
class CardDetailViewModel @Inject constructor(
    private val getCardDetail: GetCardDetail,
    private val getCardComments: GetCardComments,
    private val getMoreCardComments: GetMoreCardComments,
    private val likeCard: LikeCard,
    private val unLikeCard: UnlikeCard,
    private val deleteCard: DeleteCard
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardDetailUiState())
    val uiState: StateFlow<CardDetailUiState> = _uiState.asStateFlow()

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
                            error = "댓글을 불러올 수 없습니다: ${commentsResult.error}"
                        )
                    }
                    cardDetailResult is DomainResult.Failure -> {
                        SooumLog.e(TAG, "loadCardDetail() cardDetail failed: ${cardDetailResult.error}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = cardDetailResult.error
                        )
                    }
                    commentsResult is DomainResult.Failure -> {
                        SooumLog.e(TAG, "loadCardDetail() comments failed: ${commentsResult.error}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = commentsResult.error
                        )
                    }
                }
            } catch (e: Exception) {
                SooumLog.e(TAG, "loadCardDetail() exception: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "카드 정보를 불러오는 중 오류가 발생했습니다."
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
                        error = result.error
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

private const val TAG = "CardDetailViewModel"