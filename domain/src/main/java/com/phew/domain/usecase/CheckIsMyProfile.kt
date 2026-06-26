package com.phew.domain.usecase

import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.mapResult
import com.phew.domain.repository.network.ProfileRepository
import javax.inject.Inject

class CheckIsMyProfile @Inject constructor(private val repository: ProfileRepository) {
    data class Param(val userId: Long, val nickName: String)

    suspend operator fun invoke(param: Param): Result<Pair<Boolean, Long>> {
        return repository.requestMyProfile().mapResult(
            success = { profile ->
                if (profile.nickname != param.nickName) {
                    Pair(false, param.userId)
                } else if (profile.userId != param.userId) {
                    Pair(false, param.userId)
                } else {
                    Pair(true, profile.userId)
                }
            },
        ) { _, message -> message.ifBlank { ERROR_FAIL_JOB } }
    }
}
