package com.phew.domain.usecase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.phew.domain.repository.NetworkRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import androidx.core.net.toUri
import java.io.ByteArrayOutputStream
import android.util.Base64
import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_NETWORK
import com.phew.domain.BuildConfig
import com.phew.domain.repository.DeviceRepository
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

class RequestSignUp @Inject constructor(
    private val networkRepository: NetworkRepository,
    private val deviceRepository: DeviceRepository,
    @ApplicationContext private val context: Context,
) {
    data class Param(
        val nickName: String,
        val profileImage: String,
        val agreedToTermsOfService: Boolean,
        val agreedToLocationTerms: Boolean,
        val agreedToPrivacyPolicy: Boolean,
    )

    suspend operator fun invoke(data: Param): DomainResult<Unit, String> {
        val image = if (data.profileImage == "") "" else convertUriToJpegBase64(
            context = context,
            uriString = data.profileImage
        ) ?: return DomainResult.Failure(ERROR_FAIL_JOB)
        val fcmToken = deviceRepository.requestGetSaveFirebaseToken(BuildConfig.FCM_TOKEN_KEY)
        val notifyStatus = deviceRepository.requestGetNotify(BuildConfig.NOTIFY_KEY)
        if (fcmToken == ERROR) {
            return DomainResult.Failure(ERROR_FAIL_JOB)
        }
        val deviceId = deviceRepository.requestDeviceId()
        val requestKey = networkRepository.requestSecurityKey()
        if(requestKey is DataResult.Fail){
            return DomainResult.Failure(ERROR_NETWORK)
        }
        val makeKey = makeSecurityKey((requestKey as DataResult.Success).data)
        val encryptedDeviceId = encrypt(data = deviceId , key = makeKey)

        val request = networkRepository.requestSignUp(
            encryptedDeviceId = encryptedDeviceId,
            fcmToken = fcmToken,
            isNotificationAgreed = notifyStatus,
            nickname = data.nickName,
            profileImage = image,
            agreedToTermsOfService = data.agreedToTermsOfService,
            agreedToLocationTerms = data.agreedToLocationTerms,
            agreedToPrivacyPolicy = data.agreedToPrivacyPolicy
        )
        when (request) {
            is DataResult.Fail -> {
                return DomainResult.Failure(ERROR_NETWORK)
            }

            is DataResult.Success -> {
                val saveUserInfo = deviceRepository.saveUserInfo(
                    key = BuildConfig.USER_INFO_KEY,
                    nickName = data.nickName,
                    agreedToPrivacyPolicy = data.agreedToPrivacyPolicy,
                    agreedToLocationTerms = data.agreedToLocationTerms,
                    agreedToTermsOfService = data.agreedToTermsOfService,
                    isNotifyAgree = notifyStatus
                )
                if (!saveUserInfo) return DomainResult.Failure(ERROR_FAIL_JOB)
                return DomainResult.Success(Unit)
            }
        }
    }
    private fun makeSecurityKey(key: String): PublicKey {
        val cleanedKey = key.replace("\\s".toRegex(), "")
        val keyBytes =  java.util.Base64.getDecoder().decode(cleanedKey)
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

    private fun convertUriToJpegBase64(
        context: Context,
        uriString: String,
        quality: Int = 80,
    ): String? {
        try {
            val inputStream = context.contentResolver.openInputStream(uriString.toUri())
            val bitMap = BitmapFactory.decodeStream(inputStream)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitMap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
            val jpegBytes = byteArrayOutputStream.toByteArray()
            return Base64.encodeToString(jpegBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}