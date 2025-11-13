package com.phew.domain.usecase

import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_NETWORK
import com.phew.domain.dto.ProfileInfo
import com.phew.domain.repository.network.ProfileRepository
import javax.inject.Inject

class GetOtherProfile @Inject constructor(private val repository: ProfileRepository) {
    data class Param(
        val profileId: Long,
    )

    suspend operator fun invoke(param: Param): DomainResult<ProfileInfo, String> {
        return when (val request = repository.requestOtherProfile(profileId = param.profileId)) {
            is DataResult.Fail -> DomainResult.Failure(request.message ?: ERROR_NETWORK)
            is DataResult.Success -> {
                DomainResult.Success(request.data)
            }
        }
    }
}