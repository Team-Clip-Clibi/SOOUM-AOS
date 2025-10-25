package com.phew.domain.repository.network

import com.phew.core_common.DataResult
import com.phew.domain.dto.CardComment
import com.phew.domain.dto.CardDetail
import com.phew.domain.dto.CardReplyRequest

interface CardDetailRepository {
    suspend fun likeCard(cardId: Long): DataResult<Unit>
    suspend fun unlikeCard(cardId: Long): DataResult<Unit>
    suspend fun getCardDetail(cardId: Long, latitude: Double? = null, longitude: Double? = null): DataResult<CardDetail>
    suspend fun postCardReply(cardId: Long, request: CardReplyRequest): DataResult<Unit>
    suspend fun deleteCard(cardId: Long): DataResult<Unit>
    suspend fun getCardComments(cardId: Long, latitude: Double? = null, longitude: Double? = null): DataResult<List<CardComment>>
    suspend fun getCardCommentsMore(cardId: Long, lastId: Long, latitude: Double? = null, longitude: Double? = null): DataResult<List<CardComment>>
}
