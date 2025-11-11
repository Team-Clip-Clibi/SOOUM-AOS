package com.phew.domain.usecase

import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.domain.repository.network.ProfileRepository
import javax.inject.Inject

class SendFollowUser @Inject constructor(private val repository: ProfileRepository) {
    data class Param(
        val userId: Long,
    )

    suspend operator fun invoke(param: Param): DomainResult<Unit, String> {
        when (val request = repository.requestFollowUser(profileId = param.userId)) {
            is DataResult.Fail -> {
                if (request.code == HTTP_INVALID_TOKEN) {
                    return DomainResult.Failure(ERROR_LOGOUT)
                }
                return DomainResult.Failure(ERROR_NETWORK)
            }

            is DataResult.Success<*> -> {
                return DomainResult.Success(Unit)
            }
        }
    }
}