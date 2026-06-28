package com.phew.presentation.detail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.phew.core_common.CardDetailTrace
import com.phew.core.ui.model.navigation.CardDetailArgs
import com.phew.core_common.ERROR_ALREADY_CARD_DELETE
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.MoveDetail
import com.phew.core_common.errorMessage
import com.phew.domain.dto.CardComment
import com.phew.domain.dto.CardDetail
import com.phew.domain.orchestrator.CardDetailUseCaseOrchestrator
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
import kotlinx.coroutines.flow.emptyFlow
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
    val likeAnimationKey: Int = 0,
    val isPollVoteLoading: Boolean = false,
    val isBlockLoading: Boolean = false,
    val blockSuccess: Boolean = false,
    val blockedMemberId: Long? = null,
    val blockedNickname: String? = null,
    val deleteSuccess: Boolean = false,
    val deleteErrorDialog: Boolean = false,
    val commentsPagingData: Flow<PagingData<CardComment>> = emptyFlow(),
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
    private val detailOrchestrator: CardDetailUseCaseOrchestrator,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardDetailUiState())
    val uiState: StateFlow<CardDetailUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<CardDetailUiEffect>()
    val uiEffect: Flow<CardDetailUiEffect> = _uiEffect.asSharedFlow()

    private val _pagingRequest = MutableStateFlow<PagingRequest>(PagingRequest.None)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val commentsPagingData: Flow<PagingData<CardComment>> = _pagingRequest
        .flatMapLatest { request ->
            when (request) {
                is PagingRequest.None -> flowOf(PagingData.empty())
                is PagingRequest.Ready -> detailOrchestrator.cardCommentsPaging(request.cardId)
            }
        }
        .cachedIn(viewModelScope)

    init {
        _uiState.update { it.copy(commentsPagingData = commentsPagingData) }
    }

    fun requestComment(cardId: Long) {
        _pagingRequest.update {
            PagingRequest.Ready(cardId = cardId)
        }
    }

    fun loadCardDetail(cardId: Long, isSilent: Boolean = false) {
        viewModelScope.launch {
            try {
                requestComment(cardId)
                _uiState.value = _uiState.value.copy(isLoading = true, error = null, isRefresh = true)

                val cardDetailDeferred = async { detailOrchestrator.cardDetail(cardId) }
                val commentsDeferred = async { detailOrchestrator.cardComments(cardId) }

                val cardDetailResult = cardDetailDeferred.await()
                val commentsResult = commentsDeferred.await()

                when {
                    cardDetailResult.isSuccess && commentsResult.isSuccess -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                cardDetail = cardDetailResult.getOrThrow(),
                                comments = commentsResult.getOrThrow(),
                                isRefresh = false
                            )
                        }
                    }

                    cardDetailResult.isSuccess && commentsResult.isFailure -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                cardDetail = cardDetailResult.getOrThrow(),
                                comments = emptyList(),
                                error = CardDetailError.COMMENTS_LOAD_FAILED,
                                isRefresh = false
                            )
                        }
                    }

                    cardDetailResult.isFailure -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = if (cardDetailResult.errorMessage() == ERROR_ALREADY_CARD_DELETE) {
                                    if(isSilent) CardDetailError.CARD_DELETE_NO_DIALOG else CardDetailError.CARD_DELETE
                                } else {
                                    CardDetailError.CARD_LOAD_FAILED
                                },
                                isRefresh = false
                            )
                        }
                    }

                    commentsResult.isFailure -> {
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
            if (_uiState.value.isLikeLoading) return@launch

            val currentDetail = _uiState.value.cardDetail ?: return@launch
            val updatedDetail = currentDetail.copy(
                isLike = !currentDetail.isLike,
                likeCount = if (currentDetail.isLike) {
                    (currentDetail.likeCount - 1).coerceAtLeast(0)
                } else {
                    currentDetail.likeCount + 1
                }
            )

            _uiState.update {
                it.copy(
                    cardDetail = updatedDetail,
                    isLikeLoading = true,
                    likeAnimationKey = it.likeAnimationKey + 1,
                    error = null
                )
            }

            // 1. 카드가 삭제되었는지 먼저 확인
            detailOrchestrator.checkCardDeleted(cardId = cardId).fold(
                onSuccess = { isDeleted ->
                    if (isDeleted) {
                        // 삭제된 경우 -> 에러 설정 및 다이얼로그 표시
                        _uiState.update {
                            it.copy(
                                cardDetail = currentDetail,
                                isLikeLoading = false,
                                error = CardDetailError.CARD_DELETE
                            )
                        }
                        setDeleteDialog()
                        return@launch
                    }
                },
                onFailure = {
                    // 확인 실패 시 -> 네트워크 에러 처리 하고 중단
                    _uiState.update {
                        it.copy(
                            cardDetail = currentDetail,
                            isLikeLoading = false,
                            error = CardDetailError.NETWORK_ERROR
                        )
                    }
                    return@launch
                },
            )

            // 2. 삭제되지 않은 경우 좋아요 토글 수행
            val result = if (currentDetail.isLike) {
                detailOrchestrator.setCardLike(cardId = cardId, shouldLike = false)
            } else {
                detailOrchestrator.setCardLike(cardId = cardId, shouldLike = true)
            }

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLikeLoading = false,
                        )
                    }
                },
                onFailure = { throwable ->
                    val message = Result.failure<Unit>(throwable).errorMessage()
                    _uiState.update {
                        it.copy(
                            cardDetail = currentDetail,
                            isLikeLoading = false,
                            error = when (message) {
                                ERROR_NETWORK -> CardDetailError.NETWORK_ERROR
                                ERROR_ALREADY_CARD_DELETE -> CardDetailError.CARD_DELETE
                                else -> CardDetailError.FAIL
                            }
                        )
                    }
                },
            )
        }
    }

    fun voteOrCancelPoll(pollOptionId: Long) {
        viewModelScope.launch {
            if (_uiState.value.isPollVoteLoading) return@launch

            val currentDetail = _uiState.value.cardDetail ?: return@launch
            val currentPoll = currentDetail.poll ?: return@launch

            detailOrchestrator.checkCardDeleted(currentDetail.cardId).fold(
                onSuccess = { isDeleted ->
                    if (isDeleted) {
                        _uiState.update { it.copy(error = CardDetailError.CARD_DELETE) }
                        setDeleteDialog()
                        return@launch
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(error = CardDetailError.NETWORK_ERROR) }
                    return@launch
                },
            )

            _uiState.update { it.copy(isPollVoteLoading = true, error = null) }

            if (currentPoll.isVoted) {
                val votedOptionId = currentPoll.options
                    .firstOrNull { it.isVoted }
                    ?.pollOptionId
                    ?: pollOptionId

                detailOrchestrator.deletePollVote(votedOptionId).fold(
                    onSuccess = {
                        val canceledPoll = currentPoll.copy(
                            totalVoterCount = (currentPoll.totalVoterCount - 1L).coerceAtLeast(0L),
                            isVoted = false,
                            options = currentPoll.options.map { option ->
                                option.copy(
                                    voteCount = if (option.pollOptionId == votedOptionId) {
                                        option.voteCount?.minus(1L)?.coerceAtLeast(0L)
                                    } else {
                                        option.voteCount
                                    },
                                    votePercentage = null,
                                    isVoted = false
                                )
                            }
                        )
                        _uiState.update {
                            it.copy(
                                cardDetail = currentDetail.copy(poll = canceledPoll),
                                isPollVoteLoading = false
                            )
                        }
                    },
                    onFailure = { throwable ->
                        val message = Result.failure<Unit>(throwable).errorMessage()
                        _uiState.update {
                            it.copy(
                                isPollVoteLoading = false,
                                error = when (message) {
                                    ERROR_NETWORK -> CardDetailError.NETWORK_ERROR
                                    ERROR_ALREADY_CARD_DELETE -> CardDetailError.CARD_DELETE
                                    else -> CardDetailError.FAIL
                                }
                            )
                        }
                    },
                )
                return@launch
            }

            detailOrchestrator.createPollVote(pollOptionId).fold(
                onSuccess = { poll ->
                    _uiState.update {
                        it.copy(
                            cardDetail = currentDetail.copy(poll = poll),
                            isPollVoteLoading = false
                        )
                    }
                },
                onFailure = { throwable ->
                    val message = Result.failure<Unit>(throwable).errorMessage()
                    _uiState.update {
                        it.copy(
                            isPollVoteLoading = false,
                            error = when (message) {
                                ERROR_NETWORK -> CardDetailError.NETWORK_ERROR
                                ERROR_ALREADY_CARD_DELETE -> CardDetailError.CARD_DELETE
                                else -> CardDetailError.FAIL
                            }
                        )
                    }
                },
            )
        }
    }

    fun blockMember(toMemberId: Long, nickname: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isBlockLoading = true)
            }

            val result = detailOrchestrator.blockMember(toMemberId)

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isBlockLoading = false,
                            blockSuccess = true,
                            blockedMemberId = toMemberId,
                            blockedNickname = nickname
                        )
                    }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(
                            isBlockLoading = false,
                            error = CardDetailError.NETWORK_ERROR
                        )
                    }
                },
            )
        }
    }

    fun unblockMember() {
        val memberId = _uiState.value.blockedMemberId ?: return

        viewModelScope.launch {
            val result = detailOrchestrator.unblockMember(memberId)

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            blockedMemberId = null,
                            blockedNickname = null,
                            blockSuccess = false
                        )
                    }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(
                            error = CardDetailError.NETWORK_ERROR
                        )
                    }
                },
            )
        }
    }

    fun requestDeleteCard(cardId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            detailOrchestrator.deleteCard(cardId).fold(
                onSuccess = {
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
                },
                onFailure = {
                    _uiState.update { state ->
                        state.copy(
                            deleteSuccess = false
                        )
                    }
                },
            )
        }
    }

    fun verifyAndNavigateToWrite(cardId: Long) {
        viewModelScope.launch {
            detailOrchestrator.checkCardDeleted(cardId = cardId).fold(
                onSuccess = { isDeleted ->
                    if (isDeleted) {
                        // 삭제된 경우 에러 설정 -> Dialog 표시
                        _uiState.update { it.copy(error = CardDetailError.CARD_DELETE) }
                        setDeleteDialog()
                    } else {
                        // 삭제되지 않은 경우 이동
                        _uiEffect.emit(CardDetailUiEffect.NavigateToWrite(cardId))
                    }
                },
                onFailure = {
                    // 네트워크 에러 등 처리 (기본 에러)
                    _uiState.update { it.copy(error = CardDetailError.NETWORK_ERROR) }
                },
            )
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
            detailOrchestrator.moveToCommentCard(event = event, isEventCard = isEventCard)
        }
    }

    fun logMoveToTagView() {
        viewModelScope.launch(Dispatchers.IO) {
            detailOrchestrator.moveToTagView()
        }
    }

    fun logWhereComeFrom(view: CardDetailTrace) {
        viewModelScope.launch(Dispatchers.IO) {
            detailOrchestrator.tracePreviousView(view)
        }
    }
}

sealed class PagingRequest {
    data object None : PagingRequest()
    class Ready(val cardId: Long) : PagingRequest()
}

sealed class CardDetailUiEffect {
    data object NavigationHome : CardDetailUiEffect()
    data class NavigateToWrite(val cardId: Long) : CardDetailUiEffect()
}

private const val TAG = "CardDetailViewModel"
