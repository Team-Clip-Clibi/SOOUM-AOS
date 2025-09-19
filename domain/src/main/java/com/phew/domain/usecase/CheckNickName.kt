package com.phew.domain.usecase

import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_NETWORK
import com.phew.domain.repository.NetworkRepository
import javax.inject.Inject

class CheckNickName @Inject constructor(private val networkRepository: NetworkRepository) {
    data class Param(
        val nickName: String,
    )

    suspend operator fun invoke(data: Param): DomainResult<Boolean, String> {
        return when (val request = networkRepository.requestCheckNickName(data.nickName)) {
            is DataResult.Fail -> {
                DomainResult.Failure(ERROR_NETWORK)
            }

            is DataResult.Success -> {
                DomainResult.Success(request.data)
            }
        }
    }
}