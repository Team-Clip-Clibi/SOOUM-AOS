package com.phew.domain.usecase

import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_NETWORK
import com.phew.domain.dto.Alarm
import com.phew.domain.repository.network.MembersRepository
import javax.inject.Inject

class GetToggleNotification @Inject constructor(private val repository: MembersRepository) {
    suspend operator fun invoke(): DomainResult<Alarm, String> {
        return when (val result = repository.getToggleNotification()) {
            is DataResult.Fail -> DomainResult.Failure(ERROR_NETWORK)
            is DataResult.Success -> DomainResult.Success(result.data)
        }
    }
}