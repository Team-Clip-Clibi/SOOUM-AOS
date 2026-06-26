package com.phew.domain.usecase

import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.ERROR_TRANSFER_CODE_INVALID
import com.phew.core_common.HTTP_BAD_REQUEST
import com.phew.core_common.exception.asSooumException
import com.phew.core_common.resultFailure
import com.phew.domain.BuildConfig
import com.phew.domain.dto.Token
import com.phew.domain.interceptor.InterceptorManger
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.event.EventRepository
import com.phew.domain.repository.network.MembersRepository
import com.phew.domain.repository.network.ProfileRepository
import com.phew.domain.repository.network.SignUpRepository
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.inject.Inject

class RestoreAccount @Inject constructor(
    private val membersRepository: MembersRepository,
    private val signUpRepository: SignUpRepository,
    private val deviceRepository: DeviceRepository,
    private val interceptorManger: InterceptorManger,
    private val profileRepository: ProfileRepository,
    private val eventLogRepository: EventRepository,
) {
    data class Param(
        val transferCode: String,
    )

    suspend operator fun invoke(param: Param): Result<Unit> {
        val transferRsaKey = signUpRepository.requestSecurityKey()
            .getOrElse { return resultFailure(ERROR_NETWORK) }
        val deviceId = deviceRepository.requestDeviceId()
        val transferEncryptedInfo =
            makeDeviceInfo(key = transferRsaKey, deviceInfo = deviceId)
        val codeResult = membersRepository.transferAccount(
            transferCode = param.transferCode,
            deviceId = transferEncryptedInfo
        )
        if (codeResult.isFailure) return resultFailure(ERROR_TRANSFER_CODE_INVALID)
        eventLogRepository.logSuccessTransfer()
        val loginKey = signUpRepository.requestSecurityKey()
            .getOrElse { return resultFailure(ERROR_NETWORK) }
        val loginEncryptedInfo =
            makeDeviceInfo(key = loginKey, deviceInfo = deviceId)
        val modelName = deviceRepository.requestDeviceModel()
        val osVersion = deviceRepository.requestDeviceOS()
        return signUpRepository.requestLogin(
            info = loginEncryptedInfo,
            osVersion = osVersion,
            modelName = modelName
        ).fold(
            onSuccess = { token ->
                deviceRepository.deleteAll()
                interceptorManger.resetToken()
                val saveToken = deviceRepository.saveToken(
                    key = BuildConfig.TOKEN_KEY,
                    data = Token(
                        refreshToken = token.refreshToken,
                        accessToken = token.accessToken
                    )
                )
                if (!saveToken) return resultFailure(ERROR_FAIL_JOB)
                profileRepository.requestMyProfile().fold(
                    onSuccess = { data ->
                        val saveProfileResult = deviceRepository.saveProfileInfo(
                            profileKey = BuildConfig.PROFILE_KEY,
                            nickName = data.nickname
                        )
                        if (!saveProfileResult) {
                            interceptorManger.deleteAll()
                            return resultFailure(ERROR_FAIL_JOB)
                        }
                        Result.success(Unit)
                    },
                    onFailure = {
                        interceptorManger.deleteAll()
                        resultFailure(ERROR_FAIL_JOB)
                    },
                )
            },
            onFailure = { throwable ->
                when (throwable.asSooumException().code) {
                    HTTP_BAD_REQUEST -> resultFailure(ERROR_FAIL_JOB)
                    else -> resultFailure(ERROR_NETWORK)
                }
            },
        )
    }

    private fun makeDeviceInfo(key: String, deviceInfo: String): String {
        val rsaKey = makeSecurityKey(key)
        return encrypt(deviceInfo, rsaKey)
    }

    private fun makeSecurityKey(key: String): PublicKey {
        val cleanedKey = key.replace("\\s".toRegex(), "")
        val keyBytes = java.util.Base64.getDecoder().decode(cleanedKey)
        val spec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(spec)
    }

    private fun encrypt(data: String, key: PublicKey): String {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        return java.util.Base64.getEncoder().encodeToString(encryptedBytes)
    }
}
