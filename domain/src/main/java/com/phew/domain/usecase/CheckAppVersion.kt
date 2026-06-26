package com.phew.domain.usecase

import com.phew.domain.BuildConfig
import com.phew.domain.model.AppVersionStatusType
import com.phew.domain.repository.network.SplashRepository
import javax.inject.Inject

class CheckAppVersion @Inject constructor(private val repository: SplashRepository) {
    data class Param(
        val appVersion: String,
        val isDebugMode: Boolean,
    )

    suspend operator fun invoke(data: Param): Result<AppVersionStatusType> {
        if (data.isDebugMode) {
            return Result.success(AppVersionStatusType.OK)
        }
        return repository.requestAppVersion(
            type = BuildConfig.APP_TYPE,
            appVersion = data.appVersion
        ).fold(
            onSuccess = { result ->
                Result.success(
                    if (result.status == AppVersionStatusType.UPDATE) {
                        AppVersionStatusType.UPDATE
                    } else {
                        AppVersionStatusType.OK
                    }
                )
            },
            onFailure = { Result.failure(it) },
        )
    }
}
