package com.phew.domain.usecase

import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.mapFailureMessage
import com.phew.domain.dto.ProfileInfo
import com.phew.domain.repository.network.ProfileRepository
import javax.inject.Inject

class GetMyProfileInfo @Inject constructor(private val repository: ProfileRepository) {

    suspend operator fun invoke(): Result<ProfileInfo> {
        return repository.requestMyProfile().mapFailureMessage { _, message ->
            message.ifBlank { ERROR_NETWORK }
        }
    }
}
