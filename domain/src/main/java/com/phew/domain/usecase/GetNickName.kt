package com.phew.domain.usecase

import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_NETWORK
import com.phew.domain.repository.NetworkRepository
import javax.inject.Inject

class GetNickName @Inject constructor(private val networkRepository: NetworkRepository) {

    suspend operator fun invoke(): DomainResult<String, String> {
        val request = networkRepository.requestNickName()
        when (request) {
            is DataResult.Fail -> {
                return DomainResult.Failure(ERROR_NETWORK)
            }

            is DataResult.Success -> {
                return DomainResult.Success(request.data)
            }
        }
    }
}