package com.phew.presentation.detail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.phew.core_common.CardDetailTrace
import com.phew.core.ui.model.navigation.CardDetailArgs
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_ALREADY_CARD_DELETE
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.MoveDetail
import com.phew.domain.dto.CardComment
import com.phew.domain.dto.CardDetail
import com.phew.domain.usecase.BlockMember
import com.phew.domain.usecase.CheckCardAlreadyDelete
import com.phew.domain.usecase.DeleteCard
import com.phew.domain.usecase.GetCardComments
import com.phew.domain.usecase.GetCardCommentsPaging
import com.phew.domain.usecase.GetCardDetail
import com.phew.domain.usecase.LikeCard
import com.phew.domain.usecase.SaveEventLogDetailView
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
    CARD_DELETE_NO_DIALOG,
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
    val checkCardDelete:  UiState<Long> = UiState.None,
)

sealed interface UiState<out T> {
    data object None: UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Fail(val errorMessage: String) : UiState<Nothing>
}

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
    private val log : SaveEventLogDetailView,
    private val checkCardDelete: CheckCardAlreadyDelete,
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

    fun loadCardDetail(cardId: Long, isSilent: Boolean = false) {
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
                                error = if (cardDetailResult.error == ERROR_ALREADY_CARD_DELETE) {
                                    if(isSilent) CardDetailError.CARD_DELETE_NO_DIALOG else CardDetailError.CARD_DELETE
                                } else {
                                    CardDetailError.CARD_LOAD_FAILED
                                },
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

    fun verifyAndToggleLike(cardId: Long) {
        viewModelScope.launch {
            // 1. 카드가 삭제되었는지 먼저 확인
            when (val checkResult = checkCardDelete(CheckCardAlreadyDelete.Param(cardId = cardId))) {
                is DomainResult.Success -> {
                    if (checkResult.data) {
                        // 삭제된 경우 -> 에러 설정 및 다이얼로그 표시
                        _uiState.update { it.copy(error = CardDetailError.CARD_DELETE) }
                        setDeleteDialog()
                        return@launch
                    }
                }
                is DomainResult.Failure -> {
                    // 확인 실패 시 -> 네트워크 에러 처리 하고 중단
                    _uiState.update { it.copy(error = CardDetailError.NETWORK_ERROR) }
                    return@launch
                }
            }

            // 2. 삭제되지 않은 경우 좋아요 토글 수행
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

    fun verifyAndNavigateToWrite(cardId: Long) {
        viewModelScope.launch {
            when (val result = checkCardDelete(CheckCardAlreadyDelete.Param(cardId = cardId))) {
                is DomainResult.Success -> {
                    if (result.data) {
                        // 삭제된 경우 에러 설정 -> Dialog 표시
                        _uiState.update { it.copy(error = CardDetailError.CARD_DELETE) }
                        setDeleteDialog()
                    } else {
                        // 삭제되지 않은 경우 이동
                        _uiEffect.emit(CardDetailUiEffect.NavigateToWrite(cardId))
                    }
                }
                is DomainResult.Failure -> {
                    // 네트워크 에러 등 처리 (기본 에러)
                    _uiState.update { it.copy(error = CardDetailError.NETWORK_ERROR) }
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

    fun logMoveToCommentCard(event: MoveDetail, isEventCard: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            log.moveToCommentCard(event = event, isEventCard = isEventCard)
        }
    }

    fun logMoveToTagView() {
        viewModelScope.launch(Dispatchers.IO) {
            log.moveToTagView()
        }
    }

    fun logWhereComeFrom(view: CardDetailTrace) {
        viewModelScope.launch(Dispatchers.IO) {
            log.tracePreviousView(view)
        }
    }
}

sealed class PagingRequest {
    data object None : PagingRequest()
    class Ready(val param: GetCardCommentsPaging.Param) : PagingRequest()
}

sealed class CardDetailUiEffect {
    data object NavigationHome : CardDetailUiEffect()
    data class NavigateToWrite(val cardId: Long) : CardDetailUiEffect()
}

private const val TAG = "CardDetailViewModel"
