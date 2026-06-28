package com.phew.domain.orchestrator

import androidx.paging.PagingData
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.ERROR_TAG_FAVORITE_ALREADY_EXISTS
import com.phew.core_common.ERROR_TAG_FAVORITE_MAX_EXCEEDED
import com.phew.core_common.HTTP_BAD_REQUEST
import com.phew.core_common.HTTP_CONFLICT
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.HTTP_NO_MORE_CONTENT
import com.phew.core_common.HTTP_TOKEN_ERROR
import com.phew.core_common.WITHDRAWAL_USER
import com.phew.core_common.mapFailureMessage
import com.phew.core_common.mapResult
import com.phew.core_common.resultFailure
import com.phew.domain.dto.FavoriteTagList
import com.phew.domain.dto.TagCardContent
import com.phew.domain.dto.UserInfo
import com.phew.domain.model.TagInfo
import com.phew.domain.model.TagInfoList
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.PagerRepository
import com.phew.domain.repository.event.EventRepository
import com.phew.domain.repository.network.CardFeedRepository
import com.phew.domain.repository.network.TagRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 태그 화면에서 태그 카드/연관 태그/즐겨찾기/랭킹 조회와 태그 이벤트 로그 흐름을 조율합니다.
 */
class TagUseCaseOrchestrator @Inject constructor(
    private val pagerRepository: PagerRepository,
    private val tagRepository: TagRepository,
    private val deviceRepository: DeviceRepository,
    private val eventRepository: EventRepository,
    private val cardFeedRepository: CardFeedRepository,
) {
    fun tagCards(tagId: Long): Flow<PagingData<TagCardContent>> =
        pagerRepository.tagCards(tagId)

    suspend fun relatedTags(tag: String, resultCount: Long = 20L): Result<TagInfoList> =
        tagRepository.getRelatedTags(resultCount, tag).mapResult(
            success = { data ->
                if (data.tagInfos.isEmpty()) {
                    return resultFailure(
                        message = NO_CONTENT,
                        code = HTTP_NO_MORE_CONTENT,
                    )
                }
                data
            },
        ) { code, _ ->
            when (code) {
                HTTP_NO_MORE_CONTENT -> NO_CONTENT
                HTTP_INVALID_TOKEN -> ERROR_LOGOUT
                else -> ERROR_NETWORK
            }
        }

    suspend fun userInfo(key: String): UserInfo? = deviceRepository.getUserInfo(key)

    suspend fun profileNickName(profileKey: String): String? =
        deviceRepository.getProfileInfo(profileKey)

    suspend fun favoriteTags(): Result<FavoriteTagList> =
        tagRepository.getFavoriteTags()

    suspend fun addFavoriteTag(tagId: Long): Result<Unit> {
        eventRepository.logTagRegisterTag()
        return tagRepository.addFavoriteTag(tagId).mapFailureMessage { code, _ ->
            when (code) {
                HTTP_BAD_REQUEST -> ERROR_TAG_FAVORITE_MAX_EXCEEDED
                HTTP_CONFLICT -> ERROR_TAG_FAVORITE_ALREADY_EXISTS
                HTTP_INVALID_TOKEN -> ERROR_LOGOUT
                else -> ERROR_NETWORK
            }
        }
    }

    suspend fun removeFavoriteTag(tagId: Long): Result<Unit> =
        tagRepository.removeFavoriteTag(tagId).mapFailureMessage { code, _ ->
            when (code) {
                HTTP_INVALID_TOKEN -> ERROR_LOGOUT
                else -> ERROR_NETWORK
            }
        }

    suspend fun tagRank(): Result<List<TagInfo>> =
        tagRepository.getTagRank().mapResult(
            success = { result ->
                result.tagInfos.filter { data -> data.usageCnt > 0 }
                    .sortedByDescending { data -> data.usageCnt }
            },
        ) { code, _ ->
            when (code) {
                HTTP_INVALID_TOKEN, WITHDRAWAL_USER, HTTP_TOKEN_ERROR -> ERROR_LOGOUT
                HTTP_BAD_REQUEST -> ERROR_NETWORK
                else -> ERROR_FAIL_JOB
            }
        }

    suspend fun logClickSearchView() {
        eventRepository.logTagClickSearchView()
    }

    suspend fun logSelectPopularTag() {
        eventRepository.logTagClickPopularTag()
    }

    suspend fun checkCardDeleted(cardId: Long): Result<Boolean> =
        cardFeedRepository.requestCheckCardDelete(cardId = cardId)
            .mapFailureMessage { _, message -> message.ifBlank { ERROR_NETWORK } }

    private companion object {
        private const val NO_CONTENT = "No Content"
    }
}
