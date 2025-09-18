package com.phew.domain.usecase

import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR
import com.phew.core_common.ERROR_NO_DATA
import com.phew.domain.BuildConfig
import com.phew.domain.repository.DeviceRepository
import javax.inject.Inject

class GetFirebaseToken @Inject constructor(private val deviceRepository: DeviceRepository) {

    suspend operator fun invoke(): DomainResult<String, String> {
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
                return DomainResult.Success(requestFirebaseToken)
            }
        }
        val token = deviceRepository.requestToken(BuildConfig.TOKEN_KEY)
        if (token.first == ERROR_NO_DATA) return DomainResult.Success(requestFirebaseToken)

        return DomainResult.Success(requestFirebaseToken)
    }
}