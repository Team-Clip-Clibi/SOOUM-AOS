package com.phew.domain.repository.network

import com.phew.domain.dto.CardComment
import com.phew.domain.dto.CardDetail
import com.phew.domain.dto.CardIdResponse
import com.phew.domain.dto.Poll
import com.phew.domain.dto.CardReplyRequest

interface CardDetailRepository {
    suspend fun likeCard(cardId: Long): Result<Unit>
    suspend fun unlikeCard(cardId: Long): Result<Unit>
    suspend fun createPollVote(pollOptionId: Long): Result<Poll>
    suspend fun deletePollVote(pollOptionId: Long): Result<Unit>
    suspend fun getCardDetail(cardId: Long, latitude: Double? = null, longitude: Double? = null): Result<CardDetail>
    suspend fun postCardReply(cardId: Long, request: CardReplyRequest): Result<CardIdResponse>
    suspend fun deleteCard(cardId: Long): Result<Unit>
    suspend fun getCardComments(cardId: Long, latitude: Double? = null, longitude: Double? = null): Result<List<CardComment>>
    suspend fun getCardCommentsMore(cardId: Long, lastId: Long, latitude: Double? = null, longitude: Double? = null): Result<List<CardComment>>
    suspend fun blockMember(toMemberId: Long): Result<Unit>
    suspend fun unblockMember(toMemberId: Long): Result<Unit>
}
