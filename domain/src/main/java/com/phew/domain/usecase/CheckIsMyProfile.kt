package com.phew.domain.usecase

import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.domain.BuildConfig
import com.phew.domain.repository.DeviceRepository
import javax.inject.Inject

class CheckIsMyProfile @Inject constructor(private val repository: DeviceRepository) {
    data class Param(val userId: Long, val nickName: String)

    suspend operator fun invoke(param: Param): DomainResult<Pair<Boolean, Long>, String> {
        val myProfile =
            repository.getProfileInfo(BuildConfig.PROFILE_KEY) ?: return DomainResult.Failure(
                ERROR_FAIL_JOB
            )
        if (myProfile == param.nickName) return DomainResult.Success(Pair(true, param.userId))
        return DomainResult.Success(Pair(false, param.userId))
    }
}