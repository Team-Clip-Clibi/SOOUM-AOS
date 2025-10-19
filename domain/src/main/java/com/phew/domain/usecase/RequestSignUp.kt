package com.phew.domain.usecase

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import androidx.core.net.toUri
import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_FAIL_PACKAGE_IMAGE
import com.phew.core_common.ERROR_NETWORK
import com.phew.domain.BuildConfig
import com.phew.domain.dto.Token
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.network.SignUpRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.io.ByteArrayOutputStream
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

class RequestSignUp @Inject constructor(
    private val repository: SignUpRepository,
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
        val fcmToken = deviceRepository.requestGetSaveFirebaseToken(BuildConfig.FCM_TOKEN_KEY)
        val notifyStatus = deviceRepository.requestGetNotify(BuildConfig.NOTIFY_KEY)
        if (fcmToken == ERROR) {
            return DomainResult.Failure(ERROR_FAIL_JOB)
        }
        val deviceId = deviceRepository.requestDeviceId()
        val deviceModel = deviceRepository.requestDeviceModel()
        val androidOs = deviceRepository.requestDeviceOS()
        val requestKey = repository.requestSecurityKey()
        if (requestKey is DataResult.Fail) {
            return DomainResult.Failure(ERROR_NETWORK)
        }
        val makeKey = makeSecurityKey((requestKey as DataResult.Success).data)
        val encryptedDeviceId = encrypt(data = deviceId, key = makeKey)
        val fileName: String?
        if (data.profileImage.isNotEmpty()) {
            val requestImageUploadUrl = repository.requestUploadImageUrl()
            if (requestImageUploadUrl is DataResult.Fail) return DomainResult.Failure(ERROR_NETWORK)
            fileName = (requestImageUploadUrl as DataResult.Success).data.imgName
            val uploadImageUrl = requestImageUploadUrl.data.imgUrl
            val file = try{
                context.contentResolver.readAsCompressedJpegRequestBody(uri = data.profileImage.toUri())
            }catch (e: IOException){
                return DomainResult.Failure(ERROR_FAIL_PACKAGE_IMAGE)
            }catch (e : OutOfMemoryError){
                return DomainResult.Failure(ERROR_FAIL_JOB)
            }
            val requestImageUpload = repository.requestUploadImage(
                data = file,
                url = uploadImageUrl
            )
            if (requestImageUpload is DataResult.Fail) return DomainResult.Failure(ERROR_NETWORK)
        } else {
            fileName = null
        }
        val request = repository.requestSignUp(
            encryptedDeviceId = encryptedDeviceId,
            fcmToken = fcmToken,
            isNotificationAgreed = notifyStatus,
            nickname = data.nickName,
            profileImage = fileName,
            agreedToTermsOfService = data.agreedToTermsOfService,
            agreedToLocationTerms = data.agreedToLocationTerms,
            agreedToPrivacyPolicy = data.agreedToPrivacyPolicy,
            deviceModel = deviceModel,
            deviceOs = androidOs
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
                val saveToken = deviceRepository.saveToken(
                    key = BuildConfig.TOKEN_KEY,
                    data = Token(
                        refreshToken = request.data.refreshToken,
                        accessToken = request.data.accessToken
                    )
                )
                if (!saveToken) return DomainResult.Failure(ERROR_FAIL_JOB)
                return DomainResult.Success(Unit)
            }
        }
    }

    private fun makeSecurityKey(key: String): PublicKey {
        val cleanedKey = key.replace("\\s".toRegex(), "")
        val keyBytes = java.util.Base64.getDecoder().decode(cleanedKey)
        val spec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance(BuildConfig.DECODE_ALGORITHM)
        return keyFactory.generatePublic(spec)
    }

    private fun encrypt(data: String, key: PublicKey): String {
        val cipher = Cipher.getInstance(BuildConfig.ENCRYPT_ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        return java.util.Base64.getEncoder().encodeToString(encryptedBytes)
    }

    private fun ContentResolver.readAsCompressedJpegRequestBody(
        uri: Uri,
    ): RequestBody {
        val inputStream = openInputStream(uri)
            ?: throw IOException("Failed to open InputStream for URI: $uri")

        inputStream.use { stream ->
            val bitmap = BitmapFactory.decodeStream(stream)
                ?: throw IOException("Failed to decode bitmap from URI: $uri")

            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            return byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
        }
    }
}