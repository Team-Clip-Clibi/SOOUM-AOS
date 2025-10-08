package com.phew.domain.usecase

import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_NETWORK
import com.phew.domain.BuildConfig
import com.phew.domain.dto.Token
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.network.SignUpRepository
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.inject.Inject

class Login @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val repository: SignUpRepository,
) {
    suspend operator fun invoke(): DomainResult<Unit, String> {
        val deviceId = deviceRepository.requestDeviceId()
        val requestKey = repository.requestSecurityKey()
        if (requestKey is DataResult.Fail) {
            return DomainResult.Failure(ERROR_NETWORK)
        }
        val securityKey = (requestKey as DataResult.Success).data
        val key = makeSecurityKey(securityKey)
        val encryptedInfo = encrypt(data = deviceId, key = key)
        when (val requestLogin = repository.requestLogin(encryptedInfo)) {
            is DataResult.Fail -> {
                return DomainResult.Failure(ERROR_NETWORK)
            }

            is DataResult.Success -> {
                val saveToken = deviceRepository.saveToken(
                    key = BuildConfig.TOKEN_KEY,
                    data = Token(refreshToken = requestLogin.data.refreshToken, accessToken = requestLogin.data.accessToken)
                )
                if (!saveToken) {
                    return DomainResult.Failure(ERROR_FAIL_JOB)
                }
                return DomainResult.Success(Unit)
            }
        }
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