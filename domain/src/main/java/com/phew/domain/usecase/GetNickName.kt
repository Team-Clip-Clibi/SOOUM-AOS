package com.phew.domain.usecase

import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_NETWORK
import com.phew.domain.repository.network.SignUpRepository
import javax.inject.Inject

class GetNickName @Inject constructor(private val repository: SignUpRepository) {

    suspend operator fun invoke(): DomainResult<String, String> {
        val request = repository.requestNickName()
        return when (request) {
            is DataResult.Fail -> {
                DomainResult.Failure(ERROR_NETWORK)
            }

            is DataResult.Success -> {
                DomainResult.Success(request.data)
            }
        }
    }
}