package com.phew.domain.usecase

import androidx.paging.PagingData
import com.phew.domain.model.BlockMember
import com.phew.domain.repository.PagerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBlockUserPaging @Inject constructor(
    private val repository: PagerRepository
) {
    operator fun invoke(): Flow<PagingData<BlockMember>> = repository.getBlockUserPaging()
}