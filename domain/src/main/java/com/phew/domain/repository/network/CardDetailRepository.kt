package com.phew.domain.repository.network

import com.phew.core_common.DataResult
import com.phew.domain.dto.CardComment
import com.phew.domain.dto.CardDetail
import com.phew.domain.dto.CardReply
import com.phew.domain.dto.CardReplyRequest

interface CardDetailRepository {
    suspend fun likeCard(cardId: Int): DataResult<Unit>
    suspend fun unlikeCard(cardId: Int): DataResult<Unit>
    suspend fun getCardDetail(cardId: Int, latitude: Double? = null, longitude: Double? = null): DataResult<CardDetail>
    suspend fun postCardReply(cardId: Int, request: CardReplyRequest): DataResult<CardReply>
    suspend fun deleteCard(cardId: Int): DataResult<Unit>
    suspend fun getCardComments(cardId: Int, latitude: Double? = null, longitude: Double? = null): DataResult<List<CardComment>>
    suspend fun getCardCommentsMore(cardId: Int, lastId: Int, latitude: Double? = null, longitude: Double? = null): DataResult<List<CardComment>>
}
