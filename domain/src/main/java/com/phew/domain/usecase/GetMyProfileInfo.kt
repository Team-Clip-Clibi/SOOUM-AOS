package com.phew.domain.usecase

import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_NETWORK
import com.phew.domain.dto.MyProfileInfo
import com.phew.domain.repository.network.ProfileRepository
import javax.inject.Inject

class GetMyProfileInfo @Inject constructor(private val repository: ProfileRepository) {

    suspend operator fun invoke(): DomainResult<MyProfileInfo, String> {
        return when (val request = repository.requestMyProfile()) {
            is DataResult.Fail -> {
                DomainResult.Failure(request.message ?: ERROR_NETWORK)
            }

            is DataResult.Success -> {
                DomainResult.Success(request.data)
            }
        }
    }
}