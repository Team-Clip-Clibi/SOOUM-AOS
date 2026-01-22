package com.phew.domain.usecase

import androidx.paging.PagingData
import com.phew.domain.dto.FollowData
import com.phew.domain.repository.PagerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFollowing @Inject constructor(private val repository: PagerRepository) {
    operator fun invoke(profileId: Long): Flow<PagingData<FollowData>> =
        repository.following(profileId = profileId)
}