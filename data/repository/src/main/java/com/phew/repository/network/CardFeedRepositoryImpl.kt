package com.phew.repository.network

import com.phew.core_common.DataResult
import com.phew.domain.dto.CardImageDefault
import com.phew.domain.dto.Latest
import com.phew.domain.dto.Popular
import com.phew.domain.dto.TagInfo
import com.phew.domain.repository.network.CardFeedRepository
import com.phew.network.dto.TagRequestDTO
import com.phew.network.dto.request.feed.CardFeedDto
import com.phew.network.retrofit.FeedHttp
import com.phew.repository.mapper.apiCall
import com.phew.repository.mapper.toDomain
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
        lastId: Int?
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
                    latitude = feedDto.latitude,
                    longitude = feedDto.longitude,
                    lastId = feedDto.lastId
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

    override suspend fun requestCardImageDefault(): DataResult<List<CardImageDefault>> {
        return apiCall(
            apiCall = {
                feedHttp.requestCardImageDefault()
            },
            mapper = { result ->
                result.defaultImages.values.flatMap { imageInfoList ->
                    imageInfoList.map { it.toDomain() }
                }
            }
        )
    }
}