package com.phew.repository.network

import com.phew.core_common.DataResult
import com.phew.core_common.HTTP_NO_MORE_CONTENT
import com.phew.core_common.log.SooumLog
import com.phew.domain.dto.FavoriteTagList
import com.phew.domain.model.TagInfoList
import com.phew.domain.model.TagCards
import com.phew.domain.repository.network.TagRepository
import com.phew.network.dto.TagRequestDTO
import com.phew.network.retrofit.TagHttp
import com.phew.repository.mapper.apiCall
import com.phew.repository.mapper.toDomain
import com.phew.repository.mapper.toDomainModel
import javax.inject.Inject

class TagRepositoryImpl @Inject constructor(
    private val tagHttp: TagHttp
) : TagRepository {
    override suspend fun addFavoriteTag(tagId: Long): DataResult<Unit> {
        SooumLog.d(TAG, "addFavoriteTag - tagId: $tagId")
        return apiCall(
            apiCall = { tagHttp.addFavoriteTag(tagId) },
            mapper = { }
        )
    }

    override suspend fun removeFavoriteTag(tagId: Long): DataResult<Unit> {
        SooumLog.d(TAG, "removeFavoriteTag - tagId: $tagId")
        return apiCall(
            apiCall = { tagHttp.removeFavoriteTag(tagId) },
            mapper = { }
        )
    }

    override suspend fun getRelatedTags(resultCnt: Long, tag: String): DataResult<TagInfoList> {
        SooumLog.d(TAG, "getRelatedTags - resultCnt: $resultCnt, tag: $tag")
        
        // Handle null or empty tag
        if (tag.isBlank()) {
            return DataResult.Fail(
                message = "No Content",
                code = HTTP_NO_MORE_CONTENT
            )
        }
        
        return apiCall(
            apiCall = { 
                tagHttp.getRelatedTags(
                    resultCnt = resultCnt,
                    request = TagRequestDTO(tag = tag)
                )
            },
            mapper = { it.toDomainModel() }
        )
    }

    override suspend fun getTagCards(tagId: Long, lastId: Long): DataResult<TagCards> {
        SooumLog.d(TAG, "getTagCards - tagId: $tagId, lastId: $lastId")
        return apiCall(
            apiCall = { tagHttp.getTagCards(tagId = tagId, lastId = lastId) },
            mapper = { it.toDomainModel() }
        )
    }

    override suspend fun getTagCardsWithFavorite(tagId: Long): DataResult<TagCards> {
        SooumLog.d(TAG, "getTagCardsWithFavorite - tagId: $tagId")
        return apiCall(
            apiCall = { tagHttp.getTagCardsWithFavorite(tagId = tagId) },
            mapper = { it.toDomainModel() }
        )
    }

    override suspend fun getTagRank(): DataResult<TagInfoList> {
        SooumLog.d(TAG, "getTagRank")
        return apiCall(
            apiCall = { tagHttp.getTagRank() },
            mapper = { it.toDomainModel() }
        )
    }

    override suspend fun getFavoriteTags(): DataResult<FavoriteTagList> {
        SooumLog.d(TAG, "getFavoriteTags")
        return apiCall(
            apiCall = { tagHttp.getFavoriteTags() },
            mapper = { it.toDomain() }
        )
    }
}

private const val TAG = "TagRepository"