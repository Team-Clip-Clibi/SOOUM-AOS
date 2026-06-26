package com.phew.domain.usecase

import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.resultFailure
import com.phew.domain.BuildConfig
import com.phew.domain.dto.Token
import com.phew.domain.interceptor.InterceptorManger
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.event.EventRepository
import com.phew.domain.repository.network.MembersRepository
import com.phew.domain.repository.network.SignUpRepository
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.inject.Inject

class TransferAccount @Inject constructor(
    private val membersRepository: MembersRepository,
    private val deviceRepository: DeviceRepository,
    private val signUpRepository: SignUpRepository,
    private val interceptorManger: InterceptorManger,
    private val eventLogRepository: EventRepository,
) {
    data class Param(
        val transferCode: String,
    )

    suspend operator fun invoke(param: Param): Result<Unit> {
        val transferKey = signUpRepository.requestSecurityKey()
            .getOrElse { return resultFailure("Failed to get transfer key") }
        eventLogRepository.logSuccessTransfer()
        val deviceId = deviceRepository.requestDeviceId()
        val transferEncryptInfo = makeDeviceInfo(
            key = transferKey,
            deviceInfo = deviceId
        )
        val codeResult = membersRepository.transferAccount(
            transferCode = param.transferCode,
            deviceId = transferEncryptInfo
        )
        if (codeResult.isFailure) return codeResult
        val loginKey = signUpRepository.requestSecurityKey()
            .getOrElse { return resultFailure("Failed to get login key") }
        val loginEncryptInfo = makeDeviceInfo(
            key = loginKey,
            deviceInfo = deviceId
        )
        val modelName = deviceRepository.requestDeviceModel()
        val osVersion = deviceRepository.requestDeviceOS()
        return signUpRepository.requestLogin(
            info = loginEncryptInfo,
            osVersion = osVersion,
            modelName = modelName
        ).fold(
            onSuccess = { token ->
                val deleteAll = interceptorManger.deleteAll()
                if (!deleteAll) return resultFailure(ERROR_FAIL_JOB)
                interceptorManger.resetToken()
                val saveToken = deviceRepository.saveToken(
                    key = BuildConfig.TOKEN_KEY,
                    data = Token(
                        refreshToken = token.refreshToken,
                        accessToken = token.accessToken
                    )
                )
                if (!saveToken) return resultFailure("Failed to save token")
                Result.success(Unit)
            },
            onFailure = { Result.failure(it) },
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
