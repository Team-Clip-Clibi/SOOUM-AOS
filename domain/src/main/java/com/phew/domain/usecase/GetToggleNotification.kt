package com.phew.domain.usecase

import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.mapFailureMessage
import com.phew.domain.dto.Alarm
import com.phew.domain.repository.network.MembersRepository
import javax.inject.Inject

class GetToggleNotification @Inject constructor(private val repository: MembersRepository) {
    suspend operator fun invoke(): Result<Alarm> {
        return repository.getToggleNotification().mapFailureMessage { _, _ -> ERROR_NETWORK }
    }
}
