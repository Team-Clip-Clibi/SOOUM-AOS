package com.phew.domain.repository.network

/**
 *  TODO
 *  1. CardFeedRepositoryImpl 생성
 *  2. 위도 경도 없을떄 어떻게 할지 처리
 *  3. 페이징 처리 어찌 할지
 *  4. Dto 값 널일 경우 처리 필요
 */
interface CardFeedRepository {
    suspend fun requestFeedPopular(accessToken: String , latitude: Double, longitude: Double)
    suspend fun requestFeedLatest(accessToken: String ,latitude: Double, longitude: Double, lastId: Long)
}