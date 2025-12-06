package com.phew.domain.usecase

import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.ERROR_TRANSFER_CODE_INVALID
import com.phew.core_common.HTTP_BAD_REQUEST
import com.phew.domain.BuildConfig
import com.phew.domain.dto.Token
import com.phew.domain.interceptor.InterceptorManger
import com.phew.domain.repository.DeviceRepository
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
) {
    data class Param(
        val transferCode: String,
    )

    suspend operator fun invoke(param: Param): DomainResult<Unit, String> {
        val transferRsaKey = signUpRepository.requestSecurityKey()
        if (transferRsaKey is DataResult.Fail) return DomainResult.Failure(ERROR_NETWORK)
        val deviceId = deviceRepository.requestDeviceId()
        val transferEncryptedInfo =
            makeDeviceInfo(key = (transferRsaKey as DataResult.Success).data, deviceInfo = deviceId)
        val codeResult = membersRepository.transferAccount(
            transferCode = param.transferCode,
            deviceId = transferEncryptedInfo
        )
        if (codeResult != Result.success(Unit)) return DomainResult.Failure(
            ERROR_TRANSFER_CODE_INVALID
        )
        val loginKey = signUpRepository.requestSecurityKey()
        val loginEncryptedInfo =
            makeDeviceInfo(key = (loginKey as DataResult.Success).data, deviceInfo = deviceId)
        val modelName = deviceRepository.requestDeviceModel()
        val osVersion = deviceRepository.requestDeviceOS()
        when (val request = signUpRepository.requestLogin(
            info = loginEncryptedInfo,
            osVersion = osVersion,
            modelName = modelName
        )) {
            is DataResult.Fail -> {
                return when (request.code) {
                    HTTP_BAD_REQUEST -> DomainResult.Failure(ERROR_FAIL_JOB)
                    else -> DomainResult.Failure(ERROR_NETWORK)
                }
            }

            is DataResult.Success -> {
                deviceRepository.deleteAll()
                interceptorManger.resetToken()
                val saveToken = deviceRepository.saveToken(
                    key = BuildConfig.TOKEN_KEY,
                    data = Token(
                        refreshToken = request.data.refreshToken,
                        accessToken = request.data.accessToken
                    )
                )
                if (!saveToken) return DomainResult.Failure(ERROR_FAIL_JOB)
                when (val profile = profileRepository.requestMyProfile()) {
                    is DataResult.Fail -> {
                        interceptorManger.deleteAll()
                        return DomainResult.Failure(ERROR_FAIL_JOB)
                    }

                    is DataResult.Success -> {
                        val data = profile.data
                        val saveProfileResult = deviceRepository.saveProfileInfo(
                            profileKey = BuildConfig.PROFILE_KEY,
                            nickName = data.nickname
                        )
                        if (!saveProfileResult) {
                            interceptorManger.deleteAll()
                            return DomainResult.Failure(ERROR_FAIL_JOB)
                        }
                        return DomainResult.Success(Unit)
                    }
                }
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