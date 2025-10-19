package com.phew.domain.repository.network

import com.phew.core_common.DataResult
import com.phew.domain.dto.CheckSignUp
import com.phew.domain.dto.Token
import com.phew.domain.dto.UploadImageUrl
import okhttp3.RequestBody

interface SignUpRepository {
    suspend fun requestCheckSignUp(info: String, osVersion: String, modelName: String): DataResult<CheckSignUp>
    suspend fun requestSecurityKey(): DataResult<String>
    suspend fun requestLogin(info: String, osVersion: String, modelName: String): DataResult<Token>
    suspend fun requestNickName(): DataResult<String>
    suspend fun requestUploadImageUrl(): DataResult<UploadImageUrl>
    suspend fun requestUploadImage(data: RequestBody, url: String): DataResult<Unit>
    suspend fun requestSignUp(
        encryptedDeviceId: String,
        fcmToken: String,
        isNotificationAgreed: Boolean,
        nickname: String,
        profileImage: String?,
        agreedToTermsOfService: Boolean,
        agreedToLocationTerms: Boolean,
        agreedToPrivacyPolicy: Boolean,
        deviceModel: String,
        deviceOs: String
    ): DataResult<Token>

    suspend fun requestCheckNickName(nickname: String): DataResult<Boolean>
}