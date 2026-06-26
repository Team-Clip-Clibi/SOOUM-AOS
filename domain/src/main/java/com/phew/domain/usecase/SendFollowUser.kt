package com.phew.domain.usecase

import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.mapResult
import com.phew.domain.repository.network.ProfileRepository
import javax.inject.Inject

class SendFollowUser @Inject constructor(private val repository: ProfileRepository) {
    data class Param(
        val userId: Long,
    )

    suspend operator fun invoke(param: Param): Result<Unit> {
        return repository.requestFollowUser(profileId = param.userId).mapResult(
            success = { Unit },
        ) { code, _ ->
            if (code == HTTP_INVALID_TOKEN) ERROR_LOGOUT else ERROR_NETWORK
        }
    }
}
