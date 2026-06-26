package com.phew.domain.repository.network

import com.phew.domain.dto.FavoriteTagList
import com.phew.domain.model.TagInfoList
import com.phew.domain.model.TagCards

interface TagRepository {
    suspend fun addFavoriteTag(tagId: Long): Result<Unit>
    suspend fun removeFavoriteTag(tagId: Long): Result<Unit>
    suspend fun getRelatedTags(resultCnt: Long, tag: String): Result<TagInfoList>
    suspend fun getTagCards(tagId: Long, lastId: Long): Result<TagCards>
    suspend fun getTagCardsWithFavorite(tagId: Long): Result<TagCards>
    suspend fun getTagRank(): Result<TagInfoList>
    suspend fun getFavoriteTags(): Result<FavoriteTagList>
}