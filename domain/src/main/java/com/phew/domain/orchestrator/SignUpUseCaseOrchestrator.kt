package com.phew.domain.orchestrator

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toUri
import com.phew.core_common.ERROR
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_FAIL_PACKAGE_IMAGE
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.ERROR_TRANSFER_CODE_INVALID
import com.phew.core_common.ERROR_UN_GOOD_IMAGE
import com.phew.core_common.HTTP_BAD_REQUEST
import com.phew.core_common.HTTP_NOT_FOUND
import com.phew.core_common.HTTP_UN_GOOD_IMAGE
import com.phew.core_common.TimeUtils
import com.phew.core_common.exception.asSooumException
import com.phew.core_common.mapFailureMessage
import com.phew.core_common.resultFailure
import com.phew.domain.BuildConfig
import com.phew.domain.CROP_FILE
import com.phew.domain.SIGN_UP_ALREADY_SIGN_UP
import com.phew.domain.SIGN_UP_BANNED
import com.phew.domain.SIGN_UP_OKAY
import com.phew.domain.SIGN_UP_WITHDRAWN
import com.phew.domain.dto.Token
import com.phew.domain.interceptor.InterceptorManger
import com.phew.domain.model.signup.SignUpRequestParam
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.event.EventRepository
import com.phew.domain.repository.network.MembersRepository
import com.phew.domain.repository.network.ProfileRepository
import com.phew.domain.repository.network.SignUpRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.io.ByteArrayOutputStream
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.inject.Inject

/**
 * 회원가입 화면의 이미지 처리, 가입 가능 여부 확인, 로그인, 계정 복구 흐름을 조율합니다.
 */
class SignUpUseCaseOrchestrator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val signUpRepository: SignUpRepository,
    private val deviceRepository: DeviceRepository,
    private val membersRepository: MembersRepository,
    private val interceptorManger: InterceptorManger,
    private val profileRepository: ProfileRepository,
    private val eventRepository: EventRepository,
) {
    fun createImageFile(): Result<Uri> {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpeg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/YourApp")
        }
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = context.contentResolver.insert(collection, values) ?: return resultFailure(ERROR_FAIL_JOB)
        return Result.success(uri)
    }

    fun finishTakePicture(uri: Uri): Result<Uri> {
        if (uri.scheme == CROP_FILE) {
            return Result.success(uri)
        }
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }
            context.contentResolver.update(uri, contentValues, null, null)
            Result.success(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            resultFailure(ERROR_FAIL_JOB)
        }
    }

    suspend fun generatedNickName(): Result<String> =
        signUpRepository.requestNickName().mapFailureMessage { _, _ -> ERROR_NETWORK }

    suspend fun checkNickName(nickName: String): Result<Boolean> =
        signUpRepository.requestCheckNickName(nickName)
            .mapFailureMessage { _, _ -> ERROR_NETWORK }

    suspend fun checkSignUp(): Result<Pair<String, String>> = coroutineScope {
        val securityKey = signUpRepository.requestSecurityKey()
            .getOrElse { return@coroutineScope resultFailure(ERROR_NETWORK) }
        val key = makeSecurityKey(securityKey, algorithm = "RSA")
        val deviceIdDeferred = async { deviceRepository.requestDeviceId() }
        val osVersionDeferred = async { deviceRepository.requestDeviceOS() }
        val modelNameDeferred = async { deviceRepository.requestDeviceModel() }
        val encryptedInfo = encrypt(data = deviceIdDeferred.await(), key = key, algorithm = "RSA/ECB/PKCS1Padding")
        signUpRepository.requestCheckSignUp(
            info = encryptedInfo,
            osVersion = osVersionDeferred.await(),
            modelName = modelNameDeferred.await(),
        ).fold(
            onSuccess = { data ->
                val resultType = when {
                    data.registered -> SIGN_UP_ALREADY_SIGN_UP
                    data.banned -> SIGN_UP_BANNED
                    data.withdrawn -> SIGN_UP_WITHDRAWN
                    else -> SIGN_UP_OKAY
                }
                Result.success(
                    resultType to if (data.time.trim().isEmpty()) "" else TimeUtils.convertIsoToDateString(data.time),
                )
            },
            onFailure = { resultFailure(ERROR_NETWORK) },
        )
    }

    suspend fun login(): Result<Unit> = coroutineScope {
        val deviceIdDeferred = async { deviceRepository.requestDeviceId() }
        val osVersionDeferred = async { deviceRepository.requestDeviceOS() }
        val modelNameDeferred = async { deviceRepository.requestDeviceModel() }
        val requestKeyDeferred = async { signUpRepository.requestSecurityKey() }
        val securityKey = requestKeyDeferred.await().getOrElse { return@coroutineScope resultFailure(ERROR_NETWORK) }
        val key = makeSecurityKey(securityKey, algorithm = "RSA")
        val encryptedInfo = encrypt(data = deviceIdDeferred.await(), key = key, algorithm = "RSA/ECB/PKCS1Padding")
        signUpRepository.requestLogin(
            info = encryptedInfo,
            osVersion = osVersionDeferred.await(),
            modelName = modelNameDeferred.await(),
        ).fold(
            onSuccess = { token ->
                val saveToken = deviceRepository.saveToken(
                    key = BuildConfig.TOKEN_KEY,
                    data = Token(refreshToken = token.refreshToken, accessToken = token.accessToken),
                )
                if (!saveToken) {
                    return@coroutineScope resultFailure(ERROR_FAIL_JOB)
                }
                Result.success(Unit)
            },
            onFailure = { resultFailure(ERROR_NETWORK) },
        )
    }

    suspend fun requestSignUp(data: SignUpRequestParam): Result<Unit> {
        val fcmToken = deviceRepository.requestGetSaveFirebaseToken(BuildConfig.FCM_TOKEN_KEY)
        val notifyStatus = deviceRepository.requestGetNotify(BuildConfig.NOTIFY_KEY)
        if (fcmToken == ERROR) {
            return resultFailure(ERROR_FAIL_JOB)
        }
        val deviceId = deviceRepository.requestDeviceId()
        val deviceModel = deviceRepository.requestDeviceModel()
        val androidOs = deviceRepository.requestDeviceOS()
        val securityKey = signUpRepository.requestSecurityKey()
            .getOrElse { return resultFailure(ERROR_NETWORK) }
        val encryptedDeviceId = encrypt(
            data = deviceId,
            key = makeSecurityKey(securityKey, algorithm = BuildConfig.DECODE_ALGORITHM),
            algorithm = BuildConfig.ENCRYPT_ALGORITHM,
        )
        val fileName = if (data.profileImage.isNotEmpty()) {
            uploadProfileImage(data.profileImage).getOrElse { return Result.failure(it) }
        } else {
            null
        }
        val request = signUpRepository.requestSignUp(
            encryptedDeviceId = encryptedDeviceId,
            fcmToken = fcmToken,
            isNotificationAgreed = notifyStatus,
            nickname = data.nickName,
            profileImage = fileName,
            agreedToTermsOfService = data.agreedToTermsOfService,
            agreedToLocationTerms = data.agreedToLocationTerms,
            agreedToPrivacyPolicy = data.agreedToPrivacyPolicy,
            deviceModel = deviceModel,
            deviceOs = androidOs,
        )
        return request.fold(
            onSuccess = { token ->
                val saveUserInfo = deviceRepository.saveUserInfo(
                    key = BuildConfig.USER_INFO_KEY,
                    nickName = data.nickName,
                    agreedToPrivacyPolicy = data.agreedToPrivacyPolicy,
                    agreedToLocationTerms = data.agreedToLocationTerms,
                    agreedToTermsOfService = data.agreedToTermsOfService,
                    isNotifyAgree = notifyStatus,
                )
                if (!saveUserInfo) return resultFailure(ERROR_FAIL_JOB)
                val saveToken = deviceRepository.saveToken(
                    key = BuildConfig.TOKEN_KEY,
                    data = Token(refreshToken = token.refreshToken, accessToken = token.accessToken),
                )
                if (!saveToken) return resultFailure(ERROR_FAIL_JOB)
                val saveProfile = deviceRepository.saveProfileInfo(
                    profileKey = BuildConfig.PROFILE_KEY,
                    nickName = data.nickName,
                )
                if (!saveProfile) return resultFailure(ERROR_FAIL_JOB)
                Result.success(Unit)
            },
            onFailure = { throwable ->
                when (throwable.asSooumException().code) {
                    HTTP_NOT_FOUND -> resultFailure(ERROR_NETWORK)
                    HTTP_UN_GOOD_IMAGE -> resultFailure(ERROR_UN_GOOD_IMAGE)
                    else -> resultFailure(ERROR_FAIL_JOB)
                }
            },
        )
    }

    suspend fun restoreAccount(transferCode: String): Result<Unit> {
        val transferRsaKey = signUpRepository.requestSecurityKey()
            .getOrElse { return resultFailure(ERROR_NETWORK) }
        val deviceId = deviceRepository.requestDeviceId()
        val transferEncryptedInfo = makeDeviceInfo(key = transferRsaKey, deviceInfo = deviceId)
        val codeResult = membersRepository.transferAccount(
            transferCode = transferCode,
            deviceId = transferEncryptedInfo,
        )
        if (codeResult.isFailure) return resultFailure(ERROR_TRANSFER_CODE_INVALID)
        eventRepository.logSuccessTransfer()
        val loginKey = signUpRepository.requestSecurityKey()
            .getOrElse { return resultFailure(ERROR_NETWORK) }
        val loginEncryptedInfo = makeDeviceInfo(key = loginKey, deviceInfo = deviceId)
        val modelName = deviceRepository.requestDeviceModel()
        val osVersion = deviceRepository.requestDeviceOS()
        return signUpRepository.requestLogin(
            info = loginEncryptedInfo,
            osVersion = osVersion,
            modelName = modelName,
        ).fold(
            onSuccess = { token ->
                deviceRepository.deleteAll()
                interceptorManger.resetToken()
                val saveToken = deviceRepository.saveToken(
                    key = BuildConfig.TOKEN_KEY,
                    data = Token(refreshToken = token.refreshToken, accessToken = token.accessToken),
                )
                if (!saveToken) return resultFailure(ERROR_FAIL_JOB)
                profileRepository.requestMyProfile().fold(
                    onSuccess = { data ->
                        val saveProfileResult = deviceRepository.saveProfileInfo(
                            profileKey = BuildConfig.PROFILE_KEY,
                            nickName = data.nickname,
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

    private suspend fun uploadProfileImage(profileImage: String): Result<String> {
        val imageUploadUrl = signUpRepository.requestUploadImageUrl()
            .getOrElse { return resultFailure(ERROR_NETWORK) }
        val file = try {
            context.contentResolver.readAsCompressedJpegRequestBody(uri = profileImage.toUri())
        } catch (e: IOException) {
            return resultFailure(ERROR_FAIL_PACKAGE_IMAGE)
        } catch (e: OutOfMemoryError) {
            return resultFailure(ERROR_FAIL_JOB)
        }
        signUpRepository.requestUploadImage(data = file, url = imageUploadUrl.imgUrl)
            .exceptionOrNull()
            ?.asSooumException()
            ?.let { exception ->
                return when (exception.code) {
                    HTTP_NOT_FOUND -> resultFailure(ERROR_NETWORK)
                    HTTP_UN_GOOD_IMAGE -> resultFailure(ERROR_UN_GOOD_IMAGE)
                    else -> resultFailure(ERROR_FAIL_JOB)
                }
            }
        return Result.success(imageUploadUrl.imgName)
    }

    private fun makeDeviceInfo(key: String, deviceInfo: String): String =
        encrypt(deviceInfo, makeSecurityKey(key, algorithm = "RSA"), algorithm = "RSA/ECB/PKCS1Padding")

    private fun makeSecurityKey(key: String, algorithm: String): PublicKey {
        val cleanedKey = key.replace("\\s".toRegex(), "")
        val keyBytes = java.util.Base64.getDecoder().decode(cleanedKey)
        val spec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance(algorithm)
        return keyFactory.generatePublic(spec)
    }

    private fun encrypt(data: String, key: PublicKey, algorithm: String): String {
        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        return java.util.Base64.getEncoder().encodeToString(encryptedBytes)
    }

    private fun ContentResolver.readAsCompressedJpegRequestBody(uri: Uri): RequestBody {
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
