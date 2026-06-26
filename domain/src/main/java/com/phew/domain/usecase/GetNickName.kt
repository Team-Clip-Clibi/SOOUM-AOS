package com.phew.domain.usecase

import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.mapFailureMessage
import com.phew.domain.repository.network.SignUpRepository
import javax.inject.Inject

class GetNickName @Inject constructor(private val repository: SignUpRepository) {

    suspend operator fun invoke(): Result<String> {
        return repository.requestNickName().mapFailureMessage { _, _ -> ERROR_NETWORK }
    }
}
