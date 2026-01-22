package com.phew.domain.usecase

import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.domain.repository.network.ProfileRepository
import javax.inject.Inject

class CheckIsMyProfile @Inject constructor(private val repository: ProfileRepository) {
    data class Param(val userId: Long, val nickName: String)

    suspend operator fun invoke(param: Param): DomainResult<Pair<Boolean, Long>, String> {
        return when (val request = repository.requestMyProfile()) {
            is DataResult.Fail -> {
                DomainResult.Failure(request.message ?: ERROR_FAIL_JOB)
            }

            is DataResult.Success -> {
                if (request.data.nickname != param.nickName) {
                    return DomainResult.Success(Pair(false, param.userId))
                }
                if (request.data.userId != param.userId) {
                    return DomainResult.Success(Pair(false, param.userId))
                }
                return DomainResult.Success(Pair(true, request.data.userId))
            }
        }
    }
}