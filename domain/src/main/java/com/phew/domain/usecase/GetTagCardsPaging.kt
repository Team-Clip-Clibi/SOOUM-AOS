package com.phew.domain.usecase

import androidx.paging.PagingData
import com.phew.domain.dto.TagCardContent
import com.phew.domain.repository.PagerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTagCardsPaging @Inject constructor(
    private val repository: PagerRepository
) {
    data class Param(
        val tagId: Long
    )

    operator fun invoke(param: Param): Flow<PagingData<TagCardContent>> {
        return repository.tagCards(param.tagId)
    }
}