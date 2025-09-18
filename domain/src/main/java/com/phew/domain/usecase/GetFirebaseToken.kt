package com.phew.domain.usecase

import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.ERROR_NO_DATA
import com.phew.domain.BuildConfig
import com.phew.domain.TOKEN_FORM
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.NetworkRepository
import javax.inject.Inject

class GetFirebaseToken @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val networkRepository: NetworkRepository
) {

    suspend operator fun invoke(): DomainResult<Unit, String> {
        val requestFirebaseToken = deviceRepository.firebaseToken()
        if (requestFirebaseToken == ERROR) {
            return DomainResult.Failure(ERROR)
        }
        val saveFirebaseToken =
            deviceRepository.requestGetSaveFirebaseToken(BuildConfig.FCM_TOKEN_KEY)
        if (saveFirebaseToken != requestFirebaseToken) {
            val result = deviceRepository.requestSaveFirebaseToken(
                key = BuildConfig.FCM_TOKEN_KEY,
                requestFirebaseToken
            )
            if (!result) {
                return DomainResult.Failure(ERROR_FAIL_JOB)
            }
        }
        val token = deviceRepository.requestToken(BuildConfig.TOKEN_KEY)
        if (token.first == ERROR_NO_DATA) return DomainResult.Success(Unit)
        val requestUpdateFcmToken = networkRepository.requestUpdateFcm(
            token = TOKEN_FORM + token.second,
            fcmToken = requestFirebaseToken
        )
        return when (requestUpdateFcmToken) {
            is DataResult.Fail -> {
                DomainResult.Failure(ERROR_NETWORK)
            }

            is DataResult.Success -> {
                DomainResult.Success(Unit)
            }
        }
    }
}