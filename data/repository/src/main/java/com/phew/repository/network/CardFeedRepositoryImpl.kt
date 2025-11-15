package com.phew.repository.network

import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.DataResult
import com.phew.core_common.HTTP_NOT_FOUND
import com.phew.core_common.HTTP_SUCCESS
import com.phew.core_common.HTTP_NO_MORE_CONTENT
import com.phew.core_common.log.SooumLog
import com.phew.domain.dto.CardDefaultImagesResponse
import com.phew.domain.dto.CardImageDefault
import com.phew.domain.dto.CheckedBaned
import com.phew.domain.dto.DistanceCard
import com.phew.domain.dto.Latest
import com.phew.domain.dto.Popular
import com.phew.domain.dto.TagInfo
import com.phew.domain.repository.network.CardFeedRepository
import com.phew.network.dto.TagRequestDTO
import com.phew.network.dto.request.feed.CardFeedDto
import com.phew.network.dto.request.feed.RequestUploadCardAnswerDTO
import com.phew.network.dto.request.feed.RequestUploadCardDTO
import com.phew.network.retrofit.FeedHttp
import com.phew.repository.mapper.apiCall
import com.phew.repository.mapper.toDomain
import okhttp3.RequestBody
import javax.inject.Inject

class CardFeedRepositoryImpl @Inject constructor(
    private val feedHttp: FeedHttp,
) : CardFeedRepository {

    override suspend fun requestFeedPopular(
        latitude: Double?,
        longitude: Double?
    ): DataResult<List<Popular>> {
        return try {
            val feedDto = CardFeedDto(
                latitude = latitude,
                longitude = longitude,
                lastId = null
            )

            val response = feedHttp.requestPopularFeed(
                latitude = feedDto.latitude,
                longitude = feedDto.longitude
            )

            if (response.isSuccessful) {
                val popularList = response.body()?.map { it.toDomain() } ?: emptyList()
                DataResult.Success(popularList)
            } else {
                DataResult.Fail(
                    code = response.code(),
                    message = response.message()
                )
            }
        } catch (e: Exception) {
            DataResult.Fail(
                throwable = e,
                message = e.message
            )
        }
    }

    override suspend fun requestFeedLatest(
        latitude: Double?,
        longitude: Double?,
        lastId: Long?
    ): DataResult<List<Latest>> {
        return try {

            val feedDto = CardFeedDto(
                latitude = latitude,
                longitude = longitude,
                lastId = lastId
            )
            val response = if (feedDto.lastId != null) {
                // 페이징이 있는 경우 - 다음 페이지 요청
                feedHttp.requestLatestFeed(
                    lastId = feedDto.lastId,
                    latitude = feedDto.latitude,
                    longitude = feedDto.longitude
                )
            } else {
                // 페이징이 없는 경우 - 첫 페이지 요청
                feedHttp.requestLatestFeed(
                    latitude = feedDto.latitude,
                    longitude = feedDto.longitude
                )
            }

            if (response.isSuccessful) {
                val latestList = response.body()?.map { it.toDomain() } ?: emptyList()
                DataResult.Success(latestList)
            } else if (response.code() == HTTP_NO_MORE_CONTENT) {
                DataResult.Success(emptyList())
            } else {
                DataResult.Fail(
                    code = response.code(),
                    message = response.message()
                )
            }
        } catch (e: Exception) {
            DataResult.Fail(
                throwable = e,
                message = e.message
            )
        }
    }

    override suspend fun requestFeedDistance(
        latitude: Double?,
        longitude: Double?,
        distance: Double?,
        lastId: Long?
    ): DataResult<List<DistanceCard>> {
        try {
            val response = if (lastId == null) {
                feedHttp.requestDistanceFeed(
                    latitude = latitude,
                    longitude = longitude,
                    distance = distance
                )
            } else {
                feedHttp.requestDistanceFeed(
                    latitude = latitude,
                    longitude = longitude,
                    distance = distance,
                    lastId = lastId
                )
            }
            if (response.isSuccessful) {
                val result = response.body()?.map { data -> data.toDomain() } ?: emptyList()
                return DataResult.Success(result)
            } else if (response.code() == HTTP_NO_MORE_CONTENT) {
                return DataResult.Success(emptyList())
            } else {
                return DataResult.Fail(code = response.code(), message = response.message())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return DataResult.Fail(
                throwable = e,
                message = e.message,
                code = APP_ERROR_CODE
            )
        }
    }

    override suspend fun requestRelatedTag(resultCnt: Int, tag: String): DataResult<List<TagInfo>> {
        return apiCall(
            apiCall = {
                feedHttp.requestRelatedTag(
                    resultCnt = resultCnt,
                    request = TagRequestDTO(tag)
                )
            },
            mapper = { result -> result.tagInfo.map { data -> data.toDomain() } }
        )
    }

    override suspend fun requestCardImageDefault(): DataResult<CardDefaultImagesResponse> {
        return apiCall(
            apiCall = {
                feedHttp.requestCardImageDefault()
            },
            mapper = { result ->
                CardDefaultImagesResponse(
                    defaultImages = result.defaultImages.mapValues { (_, imageInfoList) ->
                        imageInfoList.map { it.toDomain() }
                    }
                )
            }
        )
    }

    override suspend fun requestUploadCardImage(): DataResult<CardImageDefault> {
        return apiCall(
            apiCall = {
                feedHttp.requestUploadCardUrl()
            },
            mapper = { result -> result.toDomain() }
        )
    }

    override suspend fun requestCheckUploadCard(): DataResult<CheckedBaned> {
        return apiCall(
            apiCall = {
                feedHttp.requestCheckUploadCard()
            },
            mapper = { result -> result.toDomain() }
        )
    }

    override suspend fun requestUploadCard(
        isDistanceShared: Boolean,
        latitude: Double?,
        longitude: Double?,
        content: String,
        font: String,
        imageType: String,
        imageName: String,
        isStory: Boolean,
        tag: List<String>
    ): Int {
        try {
            val request = feedHttp.requestUploadCard(
                RequestUploadCardDTO(
                    isDistanceShared = isDistanceShared,
                    latitude = latitude,
                    longitude = longitude,
                    content = content,
                    font = font,
                    imgType = imageType,
                    imgName = imageName,
                    isStory = isStory,
                    tags = tag
                )
            )
            if (!request.isSuccessful || request.code() != 200) {
                return HTTP_NOT_FOUND
            }
            return HTTP_SUCCESS
        } catch (e: Exception) {
            e.printStackTrace()
            return APP_ERROR_CODE
        }
    }

    override suspend fun requestUploadCardAnswer(
        cardId: Long,
        isDistanceShared: Boolean,
        latitude: Double?,
        longitude: Double?,
        content: String,
        font: String,
        imageType: String,
        imageName: String,
        tag: List<String>
    ): Int {
        try {
            val request = feedHttp.requestUploadAnswerCard(
                cardId = cardId,
                request = RequestUploadCardAnswerDTO(
                    content = content,
                    font = font,
                    imgName = imageName,
                    imgType = imageType,
                    isDistanceShared = isDistanceShared,
                    latitude = latitude,
                    longitude = longitude,
                    tags = tag
                )
            )
            if (!request.isSuccessful || request.code() != 200) {
                return HTTP_NOT_FOUND
            }
            return HTTP_SUCCESS
        } catch (e: Exception) {
            e.printStackTrace()
            return APP_ERROR_CODE
        }
    }

    override suspend fun requestUploadImage(
        data: RequestBody,
        url: String,
    ): DataResult<Unit> {
        return apiCall(
            apiCall = { feedHttp.requestUploadImage(url = url, body = data) },
            mapper = { result -> result }
        )
    }

    override suspend fun requestCheckImage(imageName: String): DataResult<Boolean> {
        return apiCall(
            apiCall = { feedHttp.requestCheckBackgroundImage(imgName = imageName) },
            mapper = { result -> result.isAvailableImg }
        )
    }
}
