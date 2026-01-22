package com.phew.domain.usecase

import androidx.paging.PagingData
import com.phew.domain.dto.ProfileCard
import com.phew.domain.repository.PagerRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetProfileCommentCard @Inject constructor(private val repository: PagerRepository) {
    operator fun invoke(): Flow<PagingData<ProfileCard>> = repository.profileCommentCard()
}