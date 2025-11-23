package com.phew.domain.repository.network

import com.phew.core_common.DataResult
import com.phew.domain.dto.FavoriteTagList
import com.phew.domain.model.TagInfoList
import com.phew.domain.model.TagCards

interface TagRepository {
    suspend fun addFavoriteTag(tagId: Long): DataResult<Unit>
    suspend fun removeFavoriteTag(tagId: Long): DataResult<Unit>
    suspend fun getRelatedTags(resultCnt: Long, tag: String): DataResult<TagInfoList>
    suspend fun getTagCards(tagId: Long, lastId: Long): DataResult<TagCards>
    suspend fun getTagCardsWithFavorite(tagId: Long): DataResult<TagCards>
    suspend fun getTagRank(): DataResult<TagInfoList>
    suspend fun getFavoriteTags(): DataResult<FavoriteTagList>
}