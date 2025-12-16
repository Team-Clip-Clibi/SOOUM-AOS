package com.phew.presentation.detail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_ALREADY_CARD_DELETE
import com.phew.core_common.ERROR_NETWORK
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class CardDetailError {
    COMMENTS_LOAD_FAILED,
    CARD_LOAD_FAILED,
    NETWORK_ERROR,
    CARD_DELETE,
    FAIL
}

data class CardDetailUiState(
    val isLoading: Boolean = false,
    val isRefresh: Boolean = false,
    val cardDetail: CardDetail? = null,
    val comments: List<CardComment> = emptyList(),
    val error: CardDetailError? = null,
    val isLikeLoading: Boolean = false,
    val isBlockLoading: Boolean = false,
    val blockSuccess: Boolean = false,
    val blockedMemberId: Long? = null,
    val blockedNickname: String? = null,
    val deleteSuccess: Boolean = false,
    val deleteErrorDialog: Boolean = false,
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
    private val unblockMember: UnblockMember,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardDetailUiState())
    val uiState: StateFlow<CardDetailUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<CardDetailUiEffect>()
    val uiEffect: Flow<CardDetailUiEffect> = _uiEffect.asSharedFlow()

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

    fun requestComment(cardId: Long) {
        _pagingRequest.update {
            PagingRequest.Ready(
                GetCardCommentsPaging.Param(
                    cardId = cardId
                )
            )
        }
    }

    fun loadCardDetail(cardId: Long) {
        viewModelScope.launch {
            try {
                requestComment(cardId)
                _uiState.value = _uiState.value.copy(isLoading = true, error = null, isRefresh = true)

                val cardDetailDeferred = async { getCardDetail(GetCardDetail.Param(cardId)) }
                val commentsDeferred = async { getCardComments(GetCardComments.Param(cardId)) }

                val cardDetailResult = cardDetailDeferred.await()
                val commentsResult = commentsDeferred.await()

                when {
                    cardDetailResult is DomainResult.Success && commentsResult is DomainResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                cardDetail = cardDetailResult.data,
                                comments = commentsResult.data,
                                isRefresh = false
                            )
                        }
                    }

                    cardDetailResult is DomainResult.Success && commentsResult is DomainResult.Failure -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                cardDetail = cardDetailResult.data,
                                comments = emptyList(),
                                error = CardDetailError.COMMENTS_LOAD_FAILED,
                                isRefresh = false
                            )
                        }
                    }

                    cardDetailResult is DomainResult.Failure -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = CardDetailError.CARD_LOAD_FAILED,
                                isRefresh = false
                            )
                        }
                    }

                    commentsResult is DomainResult.Failure -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = CardDetailError.COMMENTS_LOAD_FAILED,
                                isRefresh = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = CardDetailError.NETWORK_ERROR,
                        isRefresh = false
                    )
                }
            }
        }
    }

    fun toggleLike(cardId: Long) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLikeLoading = true)
            }

            val currentDetail = _uiState.value.cardDetail
            if (currentDetail == null) {
                _uiState.update {
                    it.copy(isLikeLoading = false)
                }
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
                    _uiState.update {
                        it.copy(
                            cardDetail = updatedDetail,
                            isLikeLoading = false
                        )
                    }
                }

                is DomainResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLikeLoading = false,
                            error = when (result.error) {
                                ERROR_NETWORK -> CardDetailError.NETWORK_ERROR
                                ERROR_ALREADY_CARD_DELETE -> CardDetailError.CARD_DELETE
                                else -> CardDetailError.FAIL
                            }
                        )
                    }
                }
            }
        }
    }

    fun blockMember(toMemberId: Long, nickname: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isBlockLoading = true)
            }

            val result = blockMember(BlockMember.Param(toMemberId))

            when (result) {
                is DomainResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isBlockLoading = false,
                            blockSuccess = true,
                            blockedMemberId = toMemberId,
                            blockedNickname = nickname
                        )
                    }
                }

                is DomainResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isBlockLoading = false,
                            error = CardDetailError.NETWORK_ERROR
                        )
                    }
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
                    _uiState.update {
                        it.copy(
                            blockedMemberId = null,
                            blockedNickname = null,
                            blockSuccess = false
                        )
                    }
                }

                is DomainResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            error = CardDetailError.NETWORK_ERROR
                        )
                    }
                }
            }
        }
    }

    fun requestDeleteCard(cardId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            when (deleteCard(cardId)) {
                is DomainResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            deleteSuccess = false
                        )
                    }
                }

                is DomainResult.Success -> {
                    if (_uiState.value.cardDetail?.commentCardCount == 0) {
                        //  상세 카드에서 댓글이 없을 경우 Home으로 이동
                        _uiEffect.emit(CardDetailUiEffect.NavigationHome)
                    } else {
                        _uiState.update { state ->
                            state.copy(
                                deleteSuccess = true
                            )
                        }
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update {
            it.copy(error = null, deleteErrorDialog = false)
        }
    }

    fun setDeleteDialog() {
        _uiState.update { state -> state.copy(deleteErrorDialog = true) }
    }

    fun clearBlockSuccess() {
        _uiState.update {
            it.copy(blockSuccess = false)
        }
    }
}

sealed class PagingRequest {
    data object None : PagingRequest()
    class Ready(val param: GetCardCommentsPaging.Param) : PagingRequest()
}

sealed class CardDetailUiEffect {
    data object NavigationHome : CardDetailUiEffect()
}

private const val TAG = "CardDetailViewModel"
