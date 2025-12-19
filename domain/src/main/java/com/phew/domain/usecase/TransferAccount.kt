package com.phew.domain.usecase

import com.phew.core_common.DataResult
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.domain.BuildConfig
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
        val requestTransferKey = signUpRepository.requestSecurityKey()
        if (requestTransferKey is DataResult.Fail) return Result.failure(Exception("Failed to get transfer key"))
        eventLogRepository.logSuccessTransfer()
        val deviceId = deviceRepository.requestDeviceId()
        val transferEncryptInfo = makeDeviceInfo(
            key = (requestTransferKey as DataResult.Success).data,
            deviceInfo = deviceId
        )
        val codeResult = membersRepository.transferAccount(
            transferCode = param.transferCode,
            deviceId = transferEncryptInfo
        )
        if (codeResult != Result.success(Unit)) return codeResult
        val requestLoginKey = signUpRepository.requestSecurityKey()
        val loginEncryptInfo = makeDeviceInfo(
            key = (requestLoginKey as DataResult.Success).data,
            deviceInfo = deviceId
        )
        val modelName = deviceRepository.requestDeviceModel()
        val osVersion = deviceRepository.requestDeviceOS()
        when (val request = signUpRepository.requestLogin(
            info = loginEncryptInfo,
            osVersion = osVersion,
            modelName = modelName
        )) {
            is DataResult.Fail -> {
                return Result.failure(request.throwable ?: Exception("Login request failed"))
            }

            is DataResult.Success -> {
                val deleteAll = interceptorManger.deleteAll()
                if (!deleteAll) return Result.failure(Exception(ERROR_FAIL_JOB))
                interceptorManger.resetToken()
                val saveToken = deviceRepository.saveToken(
                    key = BuildConfig.TOKEN_KEY,
                    data = com.phew.domain.dto.Token(
                        refreshToken = request.data.refreshToken,
                        accessToken = request.data.accessToken
                    )
                )
                if (!saveToken) return Result.failure(Exception("Failed to save token"))
                return Result.success(Unit)
            }
        }
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