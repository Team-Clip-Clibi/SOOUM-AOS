package com.phew.domain.usecase

import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.mapFailureMessage
import com.phew.domain.dto.ProfileInfo
import com.phew.domain.repository.network.ProfileRepository
import javax.inject.Inject

class GetOtherProfile @Inject constructor(private val repository: ProfileRepository) {
    data class Param(
        val profileId: Long,
    )

    suspend operator fun invoke(param: Param): Result<ProfileInfo> {
        return repository.requestOtherProfile(profileId = param.profileId)
            .mapFailureMessage { _, message -> message.ifBlank { ERROR_NETWORK } }
    }
}
