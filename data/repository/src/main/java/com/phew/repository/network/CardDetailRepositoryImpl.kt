package com.phew.repository.network

import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.DataResult
import com.phew.domain.dto.CardComment
import com.phew.domain.dto.CardDetail
import com.phew.domain.dto.CardReply
import com.phew.domain.dto.CardReplyRequest
import com.phew.domain.repository.network.CardDetailRepository
import com.phew.network.dto.request.feed.RequestUploadCardAnswerDTO
import com.phew.network.retrofit.CardDetailsInquiryHttp
import com.phew.repository.mapper.apiCall
import com.phew.repository.mapper.toDomain
import retrofit2.Response
import javax.inject.Inject

class CardDetailRepositoryImpl @Inject constructor(
    private val cardDetailsHttp: CardDetailsInquiryHttp
) : CardDetailRepository {

    override suspend fun likeCard(cardId: Int): DataResult<Unit> = executeWithoutBody {
        cardDetailsHttp.requestCardLike(cardId)
    }

    override suspend fun unlikeCard(cardId: Int): DataResult<Unit> = executeWithoutBody {
        cardDetailsHttp.deleteCardLike(cardId)
    }

    override suspend fun getCardDetail(
        cardId: Int,
        latitude: Double?,
        longitude: Double?
    ): DataResult<CardDetail> {
        return apiCall(
            apiCall = { cardDetailsHttp.requestCardDetail(cardId, latitude, longitude) },
            mapper = { it.toDomain() }
        )
    }

    override suspend fun postCardReply(cardId: Int, request: CardReplyRequest): DataResult<CardReply> {
        return apiCall(
            apiCall = {
                cardDetailsHttp.postCardDetail(
                    cardId = cardId,
                    body = request.toNetwork()
                )
            },
            mapper = { it.toDomain() }
        )
    }

    override suspend fun deleteCard(cardId: Int): DataResult<Unit> = executeWithoutBody {
        cardDetailsHttp.deleteCard(cardId)
    }

    override suspend fun getCardComments(
        cardId: Int,
        latitude: Double?,
        longitude: Double?
    ): DataResult<List<CardComment>> {
        return apiCall(
            apiCall = { cardDetailsHttp.requestCardComments(cardId, latitude, longitude) },
            mapper = { list -> list.map { it.toDomain() } }
        )
    }

    override suspend fun getCardCommentsMore(
        cardId: Int,
        lastId: Int,
        latitude: Double?,
        longitude: Double?
    ): DataResult<List<CardComment>> {
        return apiCall(
            apiCall = {
                cardDetailsHttp.requestCardCommentsMore(cardId, lastId, latitude, longitude)
            },
            mapper = { list -> list.map { it.toDomain() } }
        )
    }

    private suspend fun executeWithoutBody(block: suspend () -> Response<Unit>): DataResult<Unit> {
        return try {
            val response = block()
            if (response.isSuccessful) {
                DataResult.Success(Unit)
            } else {
                DataResult.Fail(code = response.code(), message = response.message())
            }
        } catch (e: Exception) {
            DataResult.Fail(code = APP_ERROR_CODE, message = e.message, throwable = e)
        }
    }

    private fun CardReplyRequest.toNetwork(): RequestUploadCardAnswerDTO {
        return RequestUploadCardAnswerDTO(
            isDistanceShared = isDistanceShared,
            latitude = latitude,
            longitude = longitude,
            content = content,
            font = font,
            imgType = imgType,
            imgName = imgName,
            tags = tags
        )
    }
}
