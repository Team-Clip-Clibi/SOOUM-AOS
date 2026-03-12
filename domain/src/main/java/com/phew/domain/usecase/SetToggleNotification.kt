package com.phew.domain.usecase

import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_NETWORK
import com.phew.domain.dto.Alarm
import com.phew.domain.repository.network.MembersRepository
import javax.inject.Inject

class SetToggleNotification @Inject constructor(
    private val membersRepository: MembersRepository,
) {
    data class Param(val data: Alarm)

    suspend operator fun invoke(param: Param): DomainResult<Unit, String> {
        return when (membersRepository.toggleNotification(param.data)) {
            is DataResult.Fail -> {
                DomainResult.Failure(ERROR_NETWORK)
            }

            is DataResult.Success -> DomainResult.Success(Unit)
        }
    }
}