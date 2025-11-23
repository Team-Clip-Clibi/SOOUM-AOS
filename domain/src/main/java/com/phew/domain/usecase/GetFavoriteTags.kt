package com.phew.domain.usecase

import com.phew.core_common.DataResult
import com.phew.domain.dto.FavoriteTagList
import com.phew.domain.repository.network.TagRepository
import javax.inject.Inject

class GetFavoriteTags @Inject constructor(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(): DataResult<FavoriteTagList> {
        return tagRepository.getFavoriteTags()
    }
}