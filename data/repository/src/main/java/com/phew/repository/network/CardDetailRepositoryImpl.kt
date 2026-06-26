package com.phew.repository.network

import com.phew.core_common.APP_ERROR_CODE
import com.phew.domain.dto.CardComment
import com.phew.domain.dto.CardDetail
import com.phew.domain.dto.Poll
import com.phew.domain.dto.CardReplyRequest
import com.phew.domain.repository.network.CardDetailRepository
import com.phew.network.dto.request.feed.RequestUploadCardAnswerDTO
import com.phew.network.retrofit.CardDetailsInquiryHttp
import com.phew.repository.mapper.apiCall
import com.phew.repository.mapper.toDomain
import javax.inject.Inject
import retrofit2.Response
import com.phew.domain.dto.CardIdResponse // Added import

class CardDetailRepositoryImpl @Inject constructor(
    private val cardDetailsHttp: CardDetailsInquiryHttp
) : CardDetailRepository {

    override suspend fun likeCard(cardId: Long): Result<Unit> = executeWithoutBody {
        cardDetailsHttp.requestCardLike(cardId)
    }

    override suspend fun unlikeCard(cardId: Long): Result<Unit> = executeWithoutBody {
        cardDetailsHttp.deleteCardLike(cardId)
    }

    override suspend fun createPollVote(pollOptionId: Long): Result<Poll> {
        return apiCall(
            apiCall = { cardDetailsHttp.createPollVote(pollOptionId) },
            mapper = { it.toDomain() }
        )
    }

    override suspend fun deletePollVote(pollOptionId: Long): Result<Unit> = executeWithoutBody {
        cardDetailsHttp.deletePollVote(pollOptionId)
    }

    override suspend fun getCardDetail(
        cardId: Long,
        latitude: Double?,
        longitude: Double?
    ): Result<CardDetail> {
        return apiCall(
            apiCall = { cardDetailsHttp.requestCardDetail(cardId, latitude, longitude) },
            mapper = { it.toDomain() }
        )
    }

    override suspend fun postCardReply(
        cardId: Long,
        request: CardReplyRequest
    ): Result<CardIdResponse> { // Changed return type
        return try {
            val response = cardDetailsHttp.postCardDetail(
                cardId = cardId,
                body = request.toNetwork()
            )

            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it.toDomain()) // Extract cardId
                } ?: com.phew.core_common.resultFailure(code = response.code(), message = "Response body is null")
            } else {
                com.phew.core_common.resultFailure(code = response.code(), message = response.message())
            }
        } catch (e: Exception) {
            com.phew.core_common.resultFailure(code = APP_ERROR_CODE, message = e.message, throwable = e)
        }
    }


    override suspend fun deleteCard(cardId: Long): Result<Unit> = executeWithoutBody {
        cardDetailsHttp.deleteCard(cardId)
    }

    override suspend fun getCardComments(
        cardId: Long,
        latitude: Double?,
        longitude: Double?
    ): Result<List<CardComment>> {
        return apiCall(
            apiCall = { cardDetailsHttp.requestCardComments(cardId, latitude, longitude) },
            mapper = { list -> list.map { it.toDomain() } }
        )
    }

    override suspend fun getCardCommentsMore(
        cardId: Long,
        lastId: Long,
        latitude: Double?,
        longitude: Double?
    ): Result<List<CardComment>> {
        return apiCall(
            apiCall = {
                cardDetailsHttp.requestCardCommentsMore(cardId, lastId, latitude, longitude)
            },
            mapper = { list -> list.map { it.toDomain() } }
        )
    }

    override suspend fun blockMember(toMemberId: Long): Result<Unit> = executeWithoutBody {
        cardDetailsHttp.blockMember(toMemberId)
    }

    override suspend fun unblockMember(toMemberId: Long): Result<Unit> = executeWithoutBody {
        cardDetailsHttp.unblockMember(toMemberId)
    }

    private suspend fun executeWithoutBody(block: suspend () -> Response<Unit>): Result<Unit> {
        return try {
            val response = block()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                if (response.code() == com.phew.core_common.HTTP_CARD_ALREADY_DELETE) {
                    com.phew.core_common.resultFailure(code = response.code(), message = com.phew.core_common.ERROR_ALREADY_CARD_DELETE)
                } else {
                    com.phew.core_common.resultFailure(code = response.code(), message = response.message())
                }
            }
        } catch (e: Exception) {
            com.phew.core_common.resultFailure(code = APP_ERROR_CODE, message = e.message, throwable = e)
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
