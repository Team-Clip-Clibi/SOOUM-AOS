package com.phew.domain.usecase

import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.mapFailureMessage
import com.phew.domain.repository.network.SignUpRepository
import javax.inject.Inject

class CheckNickName @Inject constructor(private val repository: SignUpRepository) {
    data class Param(
        val nickName: String,
    )

    suspend operator fun invoke(data: Param): Result<Boolean> {
        return repository.requestCheckNickName(data.nickName)
            .mapFailureMessage { _, _ -> ERROR_NETWORK }
    }
}
