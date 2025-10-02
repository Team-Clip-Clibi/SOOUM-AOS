package com.phew.domain.usecase

import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.domain.APP_UPDATE
import com.phew.domain.BuildConfig
import com.phew.domain.repository.NetworkRepository
import javax.inject.Inject

class CheckAppVersion @Inject constructor(private val networkRepository: NetworkRepository) {
    data class Param(
        val appVersion: String,
        val isDebugMode: Boolean,
    )

    suspend operator fun invoke(data: Param): DomainResult<Boolean, Unit> {
        val result = networkRepository.requestAppVersion(
            type = BuildConfig.APP_TYPE,
            appVersion = data.appVersion
        )
        when (result) {
            is DataResult.Fail -> {
                return DomainResult.Failure(Unit)
            }

            is DataResult.Success -> {
                if (result.data == APP_UPDATE) {
                    return DomainResult.Success(false)
                }
                return DomainResult.Success(true)
            }
        }
    }
}