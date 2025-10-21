package com.phew.domain.usecase

import androidx.paging.PagingData
import com.phew.domain.dto.CardComment
import com.phew.domain.repository.PagerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCardCommentsPaging @Inject constructor(
    private val repository: PagerRepository
) {
    data class Param(
        val cardId: Int,
        val latitude: Double? = null,
        val longitude: Double? = null
    )

    operator fun invoke(param: Param): Flow<PagingData<CardComment>> =
        repository.cardComments(
            cardId = param.cardId,
            latitude = param.latitude,
            longitude = param.longitude
        )
}
