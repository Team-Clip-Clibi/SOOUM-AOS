package com.phew.domain.usecase.orchestrator

import androidx.paging.PagingData
import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.domain.dto.FavoriteTagList
import com.phew.domain.dto.TagCardContent
import com.phew.domain.dto.UserInfo
import com.phew.domain.model.TagInfo
import com.phew.domain.model.TagInfoList
import com.phew.domain.usecase.AddFavoriteTag
import com.phew.domain.usecase.CheckCardAlreadyDelete
import com.phew.domain.usecase.GetFavoriteTags
import com.phew.domain.usecase.GetProfileInfo
import com.phew.domain.usecase.GetRelatedTags
import com.phew.domain.usecase.GetTagCardsPaging
import com.phew.domain.usecase.GetTagRank
import com.phew.domain.usecase.GetUserInfo
import com.phew.domain.usecase.RemoveFavoriteTag
import com.phew.domain.usecase.SaveEventLogTagView
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TagUseCaseOrchestrator @Inject constructor(
    private val getTagCardsPaging: GetTagCardsPaging,
    private val getRelatedTags: GetRelatedTags,
    private val getUserInfo: GetUserInfo,
    private val getProfileInfo: GetProfileInfo,
    private val getFavoriteTags: GetFavoriteTags,
    private val addFavoriteTag: AddFavoriteTag,
    private val removeFavoriteTag: RemoveFavoriteTag,
    private val getTagRank: GetTagRank,
    private val eventLog: SaveEventLogTagView,
    private val checkCardAlreadyDelete: CheckCardAlreadyDelete,
) {
    fun tagCards(tagId: Long): Flow<PagingData<TagCardContent>> =
        getTagCardsPaging(GetTagCardsPaging.Param(tagId = tagId))

    suspend fun relatedTags(tag: String, resultCount: Long = 20L): DataResult<TagInfoList> =
        getRelatedTags(GetRelatedTags.Param(resultCnt = resultCount, tag = tag))

    suspend fun userInfo(key: String): UserInfo? = getUserInfo(GetUserInfo.Param(key = key))

    suspend fun profileNickName(profileKey: String): String? = getProfileInfo(profileKey)

    suspend fun favoriteTags(): DataResult<FavoriteTagList> = getFavoriteTags()

    suspend fun addFavoriteTag(tagId: Long): DataResult<Unit> =
        addFavoriteTag(AddFavoriteTag.Param(tagId = tagId))

    suspend fun removeFavoriteTag(tagId: Long): DataResult<Unit> =
        removeFavoriteTag(RemoveFavoriteTag.Param(tagId = tagId))

    suspend fun tagRank(): DomainResult<List<TagInfo>, String> = getTagRank()

    suspend fun logClickSearchView() {
        eventLog.logClickSearchView()
    }

    suspend fun logSelectPopularTag() {
        eventLog.logSelectPopularTag()
    }

    suspend fun checkCardDeleted(cardId: Long): DomainResult<Boolean, String> =
        checkCardAlreadyDelete(CheckCardAlreadyDelete.Param(cardId = cardId))
}
