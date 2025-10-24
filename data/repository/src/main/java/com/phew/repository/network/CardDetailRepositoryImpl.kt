package com.phew.repository.network

import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.DataResult
import com.phew.core_common.log.SooumLog
import com.phew.domain.dto.CardComment
import com.phew.domain.dto.CardDetail
import com.phew.domain.dto.CardReply
import com.phew.domain.dto.CardReplyRequest
import com.phew.domain.repository.network.CardDetailRepository
import com.phew.network.dto.request.feed.RequestUploadCardAnswerDTO
import com.phew.network.retrofit.CardDetailsInquiryHttp
import com.phew.repository.mapper.apiCall
import com.phew.repository.mapper.toDomain
import java.io.EOFException
import javax.inject.Inject
import retrofit2.Response
import retrofit2.http.Tag

class CardDetailRepositoryImpl @Inject constructor(
    private val cardDetailsHttp: CardDetailsInquiryHttp
) : CardDetailRepository {

    override suspend fun likeCard(cardId: Long): DataResult<Unit> = executeWithoutBody {
        cardDetailsHttp.requestCardLike(cardId)
    }

    override suspend fun unlikeCard(cardId: Long): DataResult<Unit> = executeWithoutBody {
        cardDetailsHttp.deleteCardLike(cardId)
    }

    override suspend fun getCardDetail(
        cardId: Long,
        latitude: Double?,
        longitude: Double?
    ): DataResult<CardDetail> {
        return apiCall(
            apiCall = { cardDetailsHttp.requestCardDetail(cardId, latitude, longitude) },
            mapper = { it.toDomain() }
        )
    }

    override suspend fun postCardReply(
        cardId: Long, 
        request: CardReplyRequest
    ): DataResult<CardReply> {
        return try {
            val response = try {
                cardDetailsHttp.postCardDetail(
                    cardId = cardId,
                    body = request.toNetwork()
                )
            } catch (e: Exception) {
                throw e
            }
            
            if (response.isSuccessful) {
                try {
                    val body = response.body()
                    if (body != null) {
                        SooumLog.d(TAG, "postCardReply() body: $body")
                        // 서버가 JSON을 반환한 경우
                        DataResult.Success(body.toDomain())
                    } else {
                        // 서버가 빈 응답을 반환한 경우 - 요청한 데이터로 응답 생성
                        createCardReplyFromRequest(request)
                    }
                } catch (e: Exception) {
                    // 기타 파싱 에러
                    SooumLog.d(TAG, "postCardReply() error: ${e.message}")
                    createCardReplyFromRequest(request)
                }
            } else {
                DataResult.Fail(code = response.code(), message = response.message())
            }
        } catch (e: Exception) {

            DataResult.Fail(code = APP_ERROR_CODE, message = e.message, throwable = e)
        }
    }

    private fun createCardReplyFromRequest(request: CardReplyRequest): DataResult<CardReply> {
        val cardReply = CardReply(
            isDistanceShared = request.isDistanceShared,
            latitude = request.latitude,
            longitude = request.longitude,
            content = request.content,
            font = request.font,
            imgType = request.imgType,
            imgName = request.imgName,
            tags = request.tags
        )
        return DataResult.Success(cardReply)
    }


    override suspend fun deleteCard(cardId: Long): DataResult<Unit> = executeWithoutBody {
        cardDetailsHttp.deleteCard(cardId)
    }

    override suspend fun getCardComments(
        cardId: Long,
        latitude: Double?,
        longitude: Double?
    ): DataResult<List<CardComment>> {
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

private const val TAG = "CardDetailRepository"
