package com.phew.network.retrofit

import com.phew.network.BuildConfig
import com.phew.network.dto.request.card.RequestBlockMemberDTO
import com.phew.network.dto.request.feed.RequestUploadCardAnswerDTO
import com.phew.network.dto.response.card.CardCommentResponseDTO
import com.phew.network.dto.response.card.CardDetailResponseDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

import com.phew.network.dto.response.feed.CardIdResponseDto // Added import

/**
 *  카드 상세 조회
 */
interface CardDetailsInquiryHttp {
    /**
     *  카드 좋아요
     */
    @POST(BuildConfig.API_URL_CARD_LIKE)
    suspend fun requestCardLike(
        @Path("cardId") cardId: Long
    ) : Response<Unit>

    /**
     *  카드 좋아요 취소
     */
    @DELETE(BuildConfig.API_URL_CARD_LIKE)
    suspend fun deleteCardLike(
        @Path("cardId") cardId: Long
    ): Response<Unit>

    /**
     *  카드 상세 조회
     */
    @GET(BuildConfig.API_URL_CARD_DETAIL)
    suspend fun requestCardDetail(
        @Path("cardId") cardId: Long,
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null
    ): Response<CardDetailResponseDTO>

    /**
     *  카드 상세 업데이트/요청 (POST)
     */
    @POST(BuildConfig.API_URL_CARD_DETAIL)
    suspend fun postCardDetail(
        @Path("cardId") cardId: Long,
        @Body body: RequestUploadCardAnswerDTO
    ): Response<CardIdResponseDto> // Changed return type

    /**
     *  카드 삭제
     */
    @DELETE(BuildConfig.API_URL_CARD_DELETE)
    suspend fun deleteCard(
        @Path("cardId") cardId: Long
    ): Response<Unit>

    /**
     *  카드 댓글 조회
     */
    @GET(BuildConfig.API_URL_CARD_COMMENT)
    suspend fun requestCardComments(
        @Path("cardId") cardId: Long,
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null
    ): Response<List<CardCommentResponseDTO>>

    /**
     *  카드 댓글 조회 (페이징)
     */
    @GET(BuildConfig.API_URL_CARD_COMMENT_MORE)
    suspend fun requestCardCommentsMore(
        @Path("cardId") cardId: Long,
        @Path("lastId") lastId: Long,
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null
    ): Response<List<CardCommentResponseDTO>>

    /**
     *  멤버 차단
     */
    @POST(BuildConfig.API_URL_BLOCK_MEMBER)
    suspend fun blockMember(
        @Path("toMemberId") toMemberId: Long
    ): Response<Unit>

    /**
     *  멤버 차단 해제
     */
    @DELETE(BuildConfig.API_URL_UNBLOCK_MEMBER)
    suspend fun unblockMember(
        @Path("toMemberId") toMemberId: Long
    ): Response<Unit>
}
