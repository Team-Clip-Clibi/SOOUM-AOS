package com.phew.repository.network

import com.phew.device_info.BuildConfig
import com.phew.core_common.DataResult
import com.phew.core_common.TOKEN_FORM
import com.phew.domain.dto.Latest
import com.phew.domain.dto.Popular
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.network.CardFeedRepository
import com.phew.domain.token.TokenManger
import com.phew.network.dto.request.feed.CardFeedDto
import com.phew.network.retrofit.FeedHttp
import com.phew.repository.mapper.toDomain
import javax.inject.Inject

class CardFeedRepositoryImpl @Inject constructor(
    private val feedHttp: FeedHttp,
    private val deviceRepository: DeviceRepository,
    private val tokenManager: TokenManger
) : CardFeedRepository {
    
    override suspend fun requestFeedPopular(
        latitude: Double?,
        longitude: Double?
    ): DataResult<List<Popular>> {
        return try {
            val token = deviceRepository.requestToken(BuildConfig.TOKEN_KEY)

            val feedDto = CardFeedDto(
                latitude = latitude,
                longitude = longitude,
                lastId = null
            )
            
            val response = feedHttp.requestPopularFeed(
                bearerToken = TOKEN_FORM + token.second,
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
            val token = deviceRepository.requestToken(BuildConfig.TOKEN_KEY)

            val feedDto = CardFeedDto(
                latitude = latitude,
                longitude = longitude,
                lastId = lastId
            )
            
            val response = if (feedDto.lastId != null) {
                // 페이징이 있는 경우 - 다음 페이지 요청
                feedHttp.requestLatestFeed(
                    bearerToken = TOKEN_FORM + token.second,
                    latitude = feedDto.latitude,
                    longitude = feedDto.longitude,
                    lastId = feedDto.lastId
                )
            } else {
                // 페이징이 없는 경우 - 첫 페이지 요청
                feedHttp.requestLatestFeed(
                    bearerToken = TOKEN_FORM + token.second,
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
}