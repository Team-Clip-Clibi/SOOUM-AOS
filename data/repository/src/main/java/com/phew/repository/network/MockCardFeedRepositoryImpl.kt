package com.phew.repository.network

import com.phew.core_common.DataResult
import com.phew.domain.dto.CardImageDefault
import com.phew.domain.dto.CheckedBaned
import com.phew.domain.dto.DistanceCard
import com.phew.domain.dto.Latest
import com.phew.domain.dto.Popular
import com.phew.domain.dto.TagInfo
import com.phew.domain.repository.network.CardFeedRepository
import kotlinx.coroutines.delay
import okhttp3.RequestBody
import javax.inject.Inject

class MockCardFeedRepositoryImpl @Inject constructor() : CardFeedRepository {
    
    override suspend fun requestFeedPopular(
        latitude: Double?,
        longitude: Double?
    ): DataResult<List<Popular>> {
        // 네트워크 지연 시뮬레이션 (감소)
        delay(300)
        
        return DataResult.Success(createMockPopularData())
    }
    
    override suspend fun requestFeedLatest(
        latitude: Double?,
        longitude: Double?,
        lastId: Int?
    ): DataResult<List<Latest>> {
        // 네트워크 지연 시뮬레이션 (감소)
        delay(200)
        
        // 페이징 시뮬레이션: lastId를 기반으로 페이지 계산
        val page = when (lastId) {
            null -> 0  // 첫 페이지
            1 -> 1     // 두 번째 페이지
            4 -> 2     // 세 번째 페이지
            else -> -1 // 더 이상 데이터 없음
        }
        
        if (page == -1) {
            return DataResult.Success(emptyList())
        }
        
        return DataResult.Success(createMockLatestData(page))
    }

    override suspend fun requestFeedDistance(
        latitude: Double?,
        longitude: Double?,
        distance: Double?,
        lastId: Int?
    ): DataResult<List<DistanceCard>> {
        // 네트워크 지연 시뮬레이션 (감소)
        delay(200)

        // 페이징 시뮬레이션: lastId를 기반으로 페이지 계산
        val page = when (lastId) {
            null -> 0  // 첫 페이지
            1 -> 1     // 두 번째 페이지
            4 -> 2     // 세 번째 페이지
            else -> -1 // 더 이상 데이터 없음
        }

        if (page == -1) {
            return DataResult.Success(emptyList())
        }

        return DataResult.Success(createMockDistanceData(page))
    }

    override suspend fun requestRelatedTag(
        resultCnt: Int,
        tag: String
    ): DataResult<List<TagInfo>> {
        return DataResult.Success(emptyList())
    }

    override suspend fun requestCardImageDefault(): DataResult<List<CardImageDefault>> {
        return DataResult.Success(emptyList())
    }

    override suspend fun requestUploadCardImage(): DataResult<CardImageDefault> {
        return DataResult.Success(CardImageDefault("",""))
    }

    override suspend fun requestCheckUploadCard(): DataResult<CheckedBaned> {
        return DataResult.Success(CheckedBaned(false , null))
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
        return 200
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
        return 200
    }

    override suspend fun requestUploadImage(
        data: RequestBody,
        url: String
    ): DataResult<Unit> {
        return DataResult.Success(Unit)
    }

    override suspend fun requestCheckImage(imageName: String): DataResult<Boolean> {
        return DataResult.Success(true)
    }

    private fun createMockPopularData(): List<Popular> {
        return listOf(
            Popular(
                cardId = "popular_boom_1",
                cardContent = "🚀 인기 급상승! 1시간 후 사라져요",
                storyExpirationTime = "01:00:00",
                isAdminCard = false,
                likeCount = 89,
                commentCardCount = 25,
                cardImgUrl = "",
                cardImageName = "",
                font = "default",
                distance = null,
                createAt = "2025-01-15T08:00:00"
            ),
            
            Popular(
                cardId = "popular_normal_1",
                cardContent = "💕 가장 인기 있는 글입니다\n정말 많은 사람들이 좋아해주셨어요!",
                storyExpirationTime = null,
                isAdminCard = false,
                likeCount = 156,
                commentCardCount = 47,
                cardImgUrl = "",
                cardImageName = "",
                font = "default",
                distance = "150m",
                createAt = "2025-01-15T07:30:00"
            ),
            
            Popular(
                cardId = "popular_admin_1",
                cardContent = "📢 [공지] 앱 업데이트 완료\n새로운 기능들을 확인해보세요!",
                storyExpirationTime = null,
                isAdminCard = true,
                likeCount = 234,
                commentCardCount = 89,
                cardImgUrl = "",
                cardImageName = "",
                font = "bold",
                distance = "공지",
                createAt = "2025-01-15T06:00:00"
            ),
            
            Popular(
                cardId = "popular_boom_2",
                cardContent = "⏰ 30분 남은 이벤트!\n놓치지 마세요",
                storyExpirationTime = "00:30:15",
                isAdminCard = false,
                likeCount = 67,
                commentCardCount = 18,
                cardImgUrl = "",
                cardImageName = "",
                font = "default",
                distance = "300m",
                createAt = "2025-01-15T09:15:00"
            ),
            
            Popular(
                cardId = "popular_normal_2",
                cardContent = "🌟 오늘의 베스트 글\n여러분의 관심에 감사드립니다",
                storyExpirationTime = "",
                isAdminCard = false,
                likeCount = 112,
                commentCardCount = 33,
                cardImgUrl = "",
                cardImageName = "",
                font = "default",
                distance = "120m",
                createAt = "2025-01-15T10:20:00"
            )
        )
    }
    
    private fun createMockLatestData(page: Int): List<Latest> {
        val allData = listOf(
            // 첫 번째 페이지 (page 0)
            Latest(
                cardId = "1",
                cardContent = "🔥 2시간 후 삭제되는 글입니다!\n지금 확인하세요",
                storyExpirationTime = "02:00:00",
                isAdminCard = false,
                likeCount = 15,
                commentCardCount = 3,
                cardImgUrl = "",
                cardImageName = "",
                font = "default",
                distance = "100m",
                createAt = "2025-01-15T10:30:00"
            ),
            
            Latest(
                cardId = "2",
                cardContent = "📢 [관리자 공지] 앱 업데이트 안내\n새로운 기능이 추가되었습니다",
                storyExpirationTime = "",
                isAdminCard = true,
                likeCount = 50,
                commentCardCount = 12,
                cardImgUrl = "",
                cardImageName = "",
                font = "bold",
                distance = "공지",
                createAt = "2025-01-15T09:00:00"
            ),
            
            Latest(
                cardId = "3",
                cardContent = "오늘 날씨가 정말 좋네요! ☀️\n산책하기 딱 좋은 날씨입니다",
                storyExpirationTime = "",
                isAdminCard = false,
                likeCount = 8,
                commentCardCount = 2,
                cardImgUrl = "",
                cardImageName = "",
                font = "default",
                distance = "50m",
                createAt = "2025-01-15T11:00:00"
            ),
            
            // 두 번째 페이지 (page 1)
            Latest(
                cardId = "4",
                cardContent = "⚡ 15분 후 사라집니다!\n빠른 확인 부탁드려요",
                storyExpirationTime = "00:15:30",
                isAdminCard = false,
                likeCount = 23,
                commentCardCount = 7,
                cardImgUrl = "",
                cardImageName = "",
                font = "default",
                distance = "75m",
                createAt = "2025-01-15T11:30:00"
            ),
            
            Latest(
                cardId = "5",
                cardContent = "맛있는 점심 추천 🍽️\n근처 맛집 정보 공유합니다",
                storyExpirationTime = "",
                isAdminCard = false,
                likeCount = 31,
                commentCardCount = 14,
                cardImgUrl = "",
                cardImageName = "",
                font = "default",
                distance = "200m",
                createAt = "2025-01-15T12:00:00"
            ),
            
            Latest(
                cardId = "6",
                cardContent = "📚 [관리자] 이용 가이드\n앱 사용법을 확인하세요",
                storyExpirationTime = "",
                isAdminCard = true,
                likeCount = 78,
                commentCardCount = 25,
                cardImgUrl = "",
                cardImageName = "",
                font = "bold",
                distance = "가이드",
                createAt = "2025-01-15T08:30:00"
            ),
            
            // 세 번째 페이지 (page 2)
            Latest(
                cardId = "7",
                cardContent = "🎯 마지막 5분!\n놓치면 후회할 기회",
                storyExpirationTime = "00:05:00",
                isAdminCard = false,
                likeCount = 45,
                commentCardCount = 11,
                cardImgUrl = "",
                cardImageName = "",
                font = "default",
                distance = "150m",
                createAt = "2025-01-15T12:30:00"
            ),
            
            Latest(
                cardId = "8",
                cardContent = "커피 한 잔의 여유 ☕\n오후 시간을 달콤하게",
                storyExpirationTime = "",
                isAdminCard = false,
                likeCount = 19,
                commentCardCount = 5,
                cardImgUrl = "",
                cardImageName = "",
                font = "default",
                distance = "80m",
                createAt = "2025-01-15T13:00:00"
            ),
            
            Latest(
                cardId = "9",
                cardContent = "운동 후 기분이 좋네요! 💪\n건강한 하루 만들기",
                storyExpirationTime = "",
                isAdminCard = false,
                likeCount = 27,
                commentCardCount = 8,
                cardImgUrl = "",
                cardImageName = "",
                font = "default",
                distance = "250m",
                createAt = "2025-01-15T13:30:00"
            )
        )
        
        // 페이지당 3개씩 반환
        val startIndex = page * 3
        val endIndex = minOf(startIndex + 3, allData.size)
        
        return if (startIndex < allData.size) {
            allData.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
    }
    private fun createMockDistanceData(page: Int): List<DistanceCard> {
        val allData = listOf(
            // 첫 번째 페이지 (page 0)
            DistanceCard(
                cardId = "1",
                cardContent = "🔥 2시간 후 삭제되는 글입니다!\n지금 확인하세요",
                storyExpirationTime = "02:00:00",
                isAdminCard = false,
                likeCount = 15,
                commentCardCount = 3,
                cardImgUrl = "",
                cardImageName = "",
                font = "default",
                distance = "100m",
                createAt = "2025-01-15T10:30:00"
            ),

            DistanceCard(
                cardId = "2",
                cardContent = "📢 [관리자 공지] 앱 업데이트 안내\n새로운 기능이 추가되었습니다",
                storyExpirationTime = "",
                isAdminCard = true,
                likeCount = 50,
                commentCardCount = 12,
                cardImgUrl = "",
                cardImageName = "",
                font = "bold",
                distance = "공지",
                createAt = "2025-01-15T09:00:00"
            ),

            DistanceCard(
                cardId = "3",
                cardContent = "오늘 날씨가 정말 좋네요! ☀️\n산책하기 딱 좋은 날씨입니다",
                storyExpirationTime = "",
                isAdminCard = false,
                likeCount = 8,
                commentCardCount = 2,
                cardImgUrl = "",
                cardImageName = "",
                font = "default",
                distance = "50m",
                createAt = "2025-01-15T11:00:00"
            ),

            // 두 번째 페이지 (page 1)
            DistanceCard(
                cardId = "4",
                cardContent = "⚡ 15분 후 사라집니다!\n빠른 확인 부탁드려요",
                storyExpirationTime = "00:15:30",
                isAdminCard = false,
                likeCount = 23,
                commentCardCount = 7,
                cardImgUrl = "",
                cardImageName = "",
                font = "default",
                distance = "75m",
                createAt = "2025-01-15T11:30:00"
            ),

            DistanceCard(
                cardId = "5",
                cardContent = "맛있는 점심 추천 🍽️\n근처 맛집 정보 공유합니다",
                storyExpirationTime = "",
                isAdminCard = false,
                likeCount = 31,
                commentCardCount = 14,
                cardImgUrl = "",
                cardImageName = "",
                font = "default",
                distance = "200m",
                createAt = "2025-01-15T12:00:00"
            ),

            DistanceCard(
                cardId = "6",
                cardContent = "📚 [관리자] 이용 가이드\n앱 사용법을 확인하세요",
                storyExpirationTime = "",
                isAdminCard = true,
                likeCount = 78,
                commentCardCount = 25,
                cardImgUrl = "",
                cardImageName = "",
                font = "bold",
                distance = "가이드",
                createAt = "2025-01-15T08:30:00"
            ),

            // 세 번째 페이지 (page 2)
            DistanceCard(
                cardId = "7",
                cardContent = "🎯 마지막 5분!\n놓치면 후회할 기회",
                storyExpirationTime = "00:05:00",
                isAdminCard = false,
                likeCount = 45,
                commentCardCount = 11,
                cardImgUrl = "",
                cardImageName = "",
                font = "default",
                distance = "150m",
                createAt = "2025-01-15T12:30:00"
            ),

            DistanceCard(
                cardId = "8",
                cardContent = "커피 한 잔의 여유 ☕\n오후 시간을 달콤하게",
                storyExpirationTime = "",
                isAdminCard = false,
                likeCount = 19,
                commentCardCount = 5,
                cardImgUrl = "",
                cardImageName = "",
                font = "default",
                distance = "80m",
                createAt = "2025-01-15T13:00:00"
            ),

            DistanceCard(
                cardId = "9",
                cardContent = "운동 후 기분이 좋네요! 💪\n건강한 하루 만들기",
                storyExpirationTime = "",
                isAdminCard = false,
                likeCount = 27,
                commentCardCount = 8,
                cardImgUrl = "",
                cardImageName = "",
                font = "default",
                distance = "250m",
                createAt = "2025-01-15T13:30:00"
            )
        )

        // 페이지당 3개씩 반환
        val startIndex = page * 3
        val endIndex = minOf(startIndex + 3, allData.size)

        return if (startIndex < allData.size) {
            allData.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
    }
}