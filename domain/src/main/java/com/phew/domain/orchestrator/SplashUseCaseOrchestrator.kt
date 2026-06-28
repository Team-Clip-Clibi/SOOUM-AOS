package com.phew.domain.orchestrator

import com.phew.core_common.ERROR
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.ERROR_NO_DATA
import com.phew.core_common.mapResult
import com.phew.core_common.resultFailure
import com.phew.domain.BuildConfig
import com.phew.domain.dto.Token
import com.phew.domain.model.AppVersionStatusType
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.network.ProfileRepository
import com.phew.domain.repository.network.SplashRepository
import javax.inject.Inject

/**
 * 스플래시 화면에서 앱 버전 확인, FCM 토큰 갱신, 알림 저장, 자동 로그인 흐름을 조율합니다.
 */
class SplashUseCaseOrchestrator @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val profileRepository: ProfileRepository,
    private val splashRepository: SplashRepository,
) {
    suspend fun checkAppVersion(appVersion: String, isDebugMode: Boolean): Result<AppVersionStatusType> {
        if (isDebugMode) {
            return Result.success(AppVersionStatusType.OK)
        }
        return splashRepository.requestAppVersion(
            type = BuildConfig.APP_TYPE,
            appVersion = appVersion,
        ).fold(
            onSuccess = { result ->
                Result.success(
                    if (result.status == AppVersionStatusType.UPDATE) {
                        AppVersionStatusType.UPDATE
                    } else {
                        AppVersionStatusType.OK
                    },
                )
            },
            onFailure = { Result.failure(it) },
        )
    }

    suspend fun updateFcmToken(): Result<Unit> {
        val firebaseToken = deviceRepository.firebaseToken()
        if (firebaseToken == ERROR) {
            return resultFailure(message = ERROR)
        }
        val savedFirebaseToken = deviceRepository.requestGetSaveFirebaseToken(BuildConfig.FCM_TOKEN_KEY)
        if (savedFirebaseToken != firebaseToken) {
            val result = deviceRepository.requestSaveFirebaseToken(
                key = BuildConfig.FCM_TOKEN_KEY,
                data = firebaseToken,
            )
            if (!result) {
                return resultFailure(message = ERROR_FAIL_JOB)
            }
        }
        val token = deviceRepository.requestToken(BuildConfig.TOKEN_KEY)
        if (token.first == ERROR_NO_DATA) return Result.success(Unit)
        return splashRepository.requestUpdateFcm(fcmToken = firebaseToken).mapResult(
            success = { Unit },
        ) { _, _ -> ERROR_NETWORK }
    }

    suspend fun saveNotify(status: Boolean): Result<Unit> {
        val request = deviceRepository.requestSaveNotify(key = BuildConfig.NOTIFY_KEY, data = status)
        if (!request) {
            return resultFailure(Unit)
        }
        return Result.success(Unit)
    }

    suspend fun autoLogin(): Boolean {
        val token = deviceRepository.requestToken(BuildConfig.TOKEN_KEY)
        if (token.first == ERROR_NO_DATA || token.second == ERROR_NO_DATA) return false
        profileRepository.requestMyProfile().fold(
            onSuccess = { data ->
                val saveProfileResult = deviceRepository.saveProfileInfo(
                    profileKey = BuildConfig.PROFILE_KEY,
                    nickName = data.nickname,
                )
                if (!saveProfileResult) {
                    deviceRepository.deleteAll()
                    return false
                }
                return true
            },
            onFailure = {
                deviceRepository.deleteAll()
                return false
            },
        )
    }
}
