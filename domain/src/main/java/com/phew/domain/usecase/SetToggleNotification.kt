package com.phew.domain.usecase

import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.mapFailureMessage
import com.phew.domain.dto.Alarm
import com.phew.domain.repository.network.MembersRepository
import javax.inject.Inject

class SetToggleNotification @Inject constructor(
    private val membersRepository: MembersRepository,
) {
    data class Param(val data: Alarm)

    suspend operator fun invoke(param: Param): Result<Unit> {
        return membersRepository.toggleNotification(param.data)
            .mapFailureMessage { _, _ -> ERROR_NETWORK }
    }
}
