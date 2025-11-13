package com.phew.domain.usecase

import com.phew.domain.model.BlockMember
import com.phew.domain.repository.network.BlockRepository
import javax.inject.Inject

class GetBlockList @Inject constructor(
    private val blockRepository: BlockRepository
) {
    suspend operator fun invoke(): Result<List<BlockMember>> {
        return blockRepository.getBlockList()
    }
}