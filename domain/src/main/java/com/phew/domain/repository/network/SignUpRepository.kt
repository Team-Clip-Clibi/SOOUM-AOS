package com.phew.domain.repository.network

import com.phew.domain.dto.CheckSignUp
import com.phew.domain.dto.Token
import com.phew.domain.dto.UploadImageUrl
import okhttp3.RequestBody

interface SignUpRepository {
    suspend fun requestCheckSignUp(info: String, osVersion: String, modelName: String): Result<CheckSignUp>
    suspend fun requestSecurityKey(): Result<String>
    suspend fun requestLogin(info: String, osVersion: String, modelName: String): Result<Token>
    suspend fun requestNickName(): Result<String>
    suspend fun requestUploadImageUrl(): Result<UploadImageUrl>
    suspend fun requestUploadImage(data: RequestBody, url: String): Result<Unit>
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
    ): Result<Token>

    suspend fun requestCheckNickName(nickname: String): Result<Boolean>
}