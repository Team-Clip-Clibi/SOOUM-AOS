package com.phew.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.phew.domain.dto.FeedCardType
import com.phew.domain.repository.FeedPagingFactory
import com.phew.domain.repository.FeedPagingQuery
import com.phew.domain.repository.network.CardFeedRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FeedPagingFactoryImpl @Inject constructor(
    private val cardFeedRepository: CardFeedRepository,
) : FeedPagingFactory {
    override fun create(query: FeedPagingQuery): Flow<PagingData<FeedCardType>> {
        return Pager(
            config = PagingConfig(
                pageSize = FEED_PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                FeedPagingSource(
                    repository = cardFeedRepository,
                    query = query
                )
            }
        ).flow
    }
}

private const val FEED_PAGE_SIZE = 30
