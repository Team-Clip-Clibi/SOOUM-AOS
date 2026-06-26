package com.phew.domain.usecase

import com.phew.core_common.ERROR
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.ERROR_NO_DATA
import com.phew.core_common.mapResult
import com.phew.core_common.resultFailure
import com.phew.domain.BuildConfig
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.network.SplashRepository
import javax.inject.Inject

class GetFirebaseToken @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val repository: SplashRepository
) {

    suspend operator fun invoke(): Result<Unit> {
        val requestFirebaseToken = deviceRepository.firebaseToken()
        if (requestFirebaseToken == ERROR) {
            return resultFailure(message = ERROR)
        }
        val saveFirebaseToken =
            deviceRepository.requestGetSaveFirebaseToken(BuildConfig.FCM_TOKEN_KEY)
        if (saveFirebaseToken != requestFirebaseToken) {
            val result = deviceRepository.requestSaveFirebaseToken(
                key = BuildConfig.FCM_TOKEN_KEY,
                requestFirebaseToken
            )
            if (!result) {
                return resultFailure(message = ERROR_FAIL_JOB)
            }
        }
        val token = deviceRepository.requestToken(BuildConfig.TOKEN_KEY)
        if (token.first == ERROR_NO_DATA) return Result.success(Unit)
        val requestUpdateFcmToken = repository.requestUpdateFcm(
            fcmToken = requestFirebaseToken
        )
        return requestUpdateFcmToken.mapResult(
            success = { Unit },
        ) { _, _ -> ERROR_NETWORK }
    }
}
