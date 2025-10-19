package com.phew.domain.usecase

import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_NETWORK
import com.phew.domain.repository.DeviceRepository
import java.security.PublicKey
import javax.inject.Inject
import java.util.Base64
import com.phew.domain.SIGN_UP_ALREADY_SIGN_UP
import com.phew.domain.SIGN_UP_BANNED
import com.phew.domain.SIGN_UP_OKAY
import com.phew.domain.SIGN_UP_WITHDRAWN
import com.phew.domain.repository.network.SignUpRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.security.spec.X509EncodedKeySpec
import java.security.KeyFactory
import javax.crypto.Cipher

class CheckSignUp @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val repository: SignUpRepository,
) {
    suspend operator fun invoke(): DomainResult<Pair<String, String>, String> = coroutineScope {
        val securityKeyResult = repository.requestSecurityKey()
        if (securityKeyResult is DataResult.Fail) {
            return@coroutineScope DomainResult.Failure(ERROR_NETWORK)
        }
        val securityKey = (securityKeyResult as DataResult.Success).data
        val key = makeSecurityKey(securityKey)

        val deviceIdDeferred = async { deviceRepository.requestDeviceId() }
        val osVersionDeferred = async { deviceRepository.requestDeviceOS() }
        val modelNameDeferred = async { deviceRepository.requestDeviceModel() }

        val deviceId = deviceIdDeferred.await()
        val osVersion = osVersionDeferred.await()
        val modelName = modelNameDeferred.await()

        val encryptedInfo = encrypt(data = deviceId, key = key)
        when (val checkSignUpResult = repository.requestCheckSignUp(
            info = encryptedInfo,
            osVersion = osVersion,
            modelName = modelName
        )) {
            is DataResult.Fail -> {
                DomainResult.Failure(ERROR_NETWORK)
            }

            is DataResult.Success -> {
                val data = checkSignUpResult.data
                val resultType = when {
                    data.registered -> SIGN_UP_ALREADY_SIGN_UP
                    data.banned -> SIGN_UP_BANNED
                    data.withdrawn -> SIGN_UP_WITHDRAWN
                    else -> SIGN_UP_OKAY
                }
                DomainResult.Success(Pair(resultType, data.time))
            }
        }
    }

    private fun makeSecurityKey(key: String): PublicKey {
        val cleanedKey = key.replace("\\s".toRegex(), "")
        val keyBytes =  Base64.getDecoder().decode(cleanedKey)
        val spec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(spec)
    }

    private fun encrypt(data: String, key: PublicKey): String {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }
}