package com.phew.domain.usecase

import com.phew.domain.dto.FavoriteTagList
import com.phew.domain.repository.network.TagRepository
import javax.inject.Inject

class GetFavoriteTags @Inject constructor(
    private val tagRepository: TagRepository
) {
    suspend operator fun invoke(): Result<FavoriteTagList> {
        return tagRepository.getFavoriteTags()
    }
}