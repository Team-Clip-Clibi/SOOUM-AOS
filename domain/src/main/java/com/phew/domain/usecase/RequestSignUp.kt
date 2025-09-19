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

class RequestSignUp @Inject constructor(
    private val networkRepository: NetworkRepository,
    private val deviceRepository: DeviceRepository,
    @ApplicationContext private val context: Context,
) {
    data class Param(
        val encryptedDeviceId: String,
        val nickName: String,
        val profileImage: String,
        val agreedToTermsOfService: Boolean,
        val agreedToLocationTerms: Boolean,
        val agreedToPrivacyPolicy: Boolean,
    )

    suspend operator fun invoke(data: Param): DomainResult<Unit, String> {
        if (data.encryptedDeviceId == "") {
            return DomainResult.Failure(ERROR_FAIL_JOB)
        }
        val image = if (data.profileImage == "") "" else convertUriToJpegBase64(
            context = context,
            uriString = data.profileImage
        ) ?: return DomainResult.Failure(ERROR_FAIL_JOB)
        val fcmToken = deviceRepository.requestGetSaveFirebaseToken(BuildConfig.FCM_TOKEN_KEY)
        val notifyStatus = deviceRepository.requestGetNotify(BuildConfig.NOTIFY_KEY)
        if (fcmToken == ERROR) {
            return DomainResult.Failure(ERROR_FAIL_JOB)
        }
        val request = networkRepository.requestSignUp(
            encryptedDeviceId = data.encryptedDeviceId,
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