package com.phew.presentation.detail.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.phew.domain.dto.CardComment
import com.phew.domain.usecase.GetCardCommentsPaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CardDetailViewModel @Inject constructor(commentPaging: GetCardCommentsPaging) :
    ViewModel() {
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

    fun requestComment(cardId: Int, latitude: Double? = null, longitude: Double? = null) {
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
}

sealed class PagingRequest {
    data object None : PagingRequest()
    data class Ready(val param: GetCardCommentsPaging.Param) : PagingRequest()
}