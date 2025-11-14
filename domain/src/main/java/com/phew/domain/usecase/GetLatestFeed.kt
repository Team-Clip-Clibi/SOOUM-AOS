package com.phew.domain.usecase

import androidx.paging.PagingData
import com.phew.domain.dto.Latest
import com.phew.domain.repository.PagerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLatestFeed @Inject constructor(private val repository: PagerRepository) {
    operator fun invoke(latitude: Double?, longitude: Double?): Flow<PagingData<Latest>> = 
        repository.latestFeed(latitude, longitude)
}