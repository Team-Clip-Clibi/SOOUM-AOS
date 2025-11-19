package com.phew.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.phew.core_common.DataResult
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.log.SooumLog
import com.phew.domain.dto.TagCardContent
import com.phew.domain.repository.network.TagRepository
import java.io.IOException
import javax.inject.Inject

class PagingTagCards @Inject constructor(
    private val tagRepository: TagRepository,
    private val tagId: Long
) : PagingSource<Long, TagCardContent>() {

    override fun getRefreshKey(state: PagingState<Long, TagCardContent>): Long? {
        val anchorPosition = state.anchorPosition ?: return null
        return state.closestItemToPosition(anchorPosition)?.cardId
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, TagCardContent> {
        val lastId = params.key ?: 0L
        SooumLog.d(TAG, "load(tagId=$tagId, lastId=$lastId, loadSize=${params.loadSize})")

        return try {
            val result = if (lastId == 0L) {
                // 첫 번째 로드 시 단순 API 호출 (페이징 없음)
                tagRepository.getTagCardsWithFavorite(tagId)
            } else {
                // 이후 페이징 로드 시 페이징 API 호출
                tagRepository.getTagCards(tagId, lastId)
            }
            
            when (result) {
                is DataResult.Success -> {
                    val tagCards = result.data
                    val cardContents = tagCards.cardContents.map { cardContent ->
                        TagCardContent(
                            cardId = cardContent.cardId,
                            cardImgName = cardContent.cardImgName,
                            cardImgUrl = cardContent.cardImgUrl,
                            cardContent = cardContent.cardContent,
                            font = cardContent.font
                        )
                    }
                    
                    val nextKey = if (cardContents.isEmpty()) {
                        null
                    } else {
                        cardContents.last().cardId
                    }

                    LoadResult.Page(
                        data = cardContents,
                        prevKey = null,
                        nextKey = nextKey
                    )
                }
                is DataResult.Fail -> {
                    when (result.code) {
                        HTTP_INVALID_TOKEN -> {
                            SooumLog.w(TAG, "Invalid token - user needs to login again")
                            LoadResult.Error(IOException("Authentication failed"))
                        }
                        else -> {
                            SooumLog.e(TAG, "Failed to load tag cards: ${result.message}")
                            LoadResult.Error(IOException("Network error: ${result.message}"))
                        }
                    }
                }
            }
        } catch (exception: Exception) {
            SooumLog.e(TAG, "Exception loading tag cards: ${exception.message}")
            LoadResult.Error(exception)
        }
    }

    companion object {
        private const val TAG = "PagingTagCards"
    }
}