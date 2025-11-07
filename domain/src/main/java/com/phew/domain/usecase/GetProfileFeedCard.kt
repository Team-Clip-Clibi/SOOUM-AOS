package com.phew.domain.usecase

import androidx.paging.PagingData
import com.phew.domain.dto.ProfileCard
import com.phew.domain.repository.PagerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProfileFeedCard @Inject constructor(private val repository: PagerRepository) {
    operator fun invoke(userId: Long): Flow<PagingData<ProfileCard>> =
        repository.profileFeedCard(userId = userId)
}