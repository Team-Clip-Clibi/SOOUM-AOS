package com.phew.repository.network

import com.phew.core_common.DataResult
import com.phew.domain.dto.CheckSignUp
import com.phew.domain.dto.Token
import com.phew.domain.dto.UploadImageUrl
import com.phew.domain.repository.network.SignUpRepository
import com.phew.network.dto.InfoDTO
import com.phew.network.dto.MemberInfoDTO
import com.phew.network.dto.NickNameDTO
import com.phew.network.dto.PolicyDTO
import com.phew.network.dto.SignUpRequest
import com.phew.network.retrofit.SignUpHttp
import com.phew.repository.mapper.apiCall
import com.phew.repository.mapper.toDomain
import okhttp3.RequestBody
import javax.inject.Inject

class SignUpRepositoryImpl @Inject constructor(private val signUpHttp : SignUpHttp) : SignUpRepository {
    override suspend fun requestCheckSignUp(info: String, osVersion: String, modelName: String): DataResult<CheckSignUp> {
        return apiCall(
            apiCall = { signUpHttp.requestCheckSignUp(InfoDTO(
                encryptedDeviceId = info,
                deviceType = "ANDROID",
                deviceOsVersion = osVersion,
                deviceModel = modelName
            )) },
            mapper = { result -> result.toDomain() }
        )
    }

    override suspend fun requestSecurityKey(): DataResult<String> {
        return apiCall(
            apiCall = { signUpHttp.getSecurityKey() },
            mapper = { result -> result.publicKey }
        )
    }

    override suspend fun requestLogin(
        info: String,
        osVersion: String,
        modelName: String
    ): DataResult<Token> {
        return apiCall(
            apiCall = { signUpHttp.requestLogin(InfoDTO(
                encryptedDeviceId = info,
                deviceType = "ANDROID",
                deviceOsVersion = osVersion,
                deviceModel = modelName
            )) },
            mapper = { result -> result.toDomain() }
        )
    }

    override suspend fun requestNickName(): DataResult<String> {
        return apiCall(
            apiCall = { signUpHttp.requestNickNameGenerator() },
            mapper = { result -> result.nickname }
        )
    }

    override suspend fun requestUploadImageUrl(): DataResult<UploadImageUrl> {
        return apiCall(
            apiCall = { signUpHttp.requestUploadImageUrl() },
            mapper = { result -> result.toDomain() }
        )
    }

    override suspend fun requestUploadImage(
        data: RequestBody,
        url: String
    ): DataResult<Unit> {
        return apiCall(
            apiCall = { signUpHttp.requestUploadImage(url = url, body = data) },
            mapper = { result -> result }
        )
    }

    override suspend fun requestSignUp(
        encryptedDeviceId: String,
        fcmToken: String,
        isNotificationAgreed: Boolean,
        nickname: String,
        profileImage: String?,
        agreedToTermsOfService: Boolean,
        agreedToLocationTerms: Boolean,
        agreedToPrivacyPolicy: Boolean
    ): DataResult<Token> {
        val signUpRequest = SignUpRequest(
            memberInfo = MemberInfoDTO(
                encryptedDeviceId = encryptedDeviceId,
                fcmToken = fcmToken,
                isNotificationAgreed = isNotificationAgreed,
                profileImage = profileImage,
                nickname = nickname,
                deviceType = "ANDROID"
            ),
            policy = PolicyDTO(
                agreedToLocationTerms = agreedToLocationTerms,
                agreedToPrivacyPolicy = agreedToPrivacyPolicy,
                agreedToTermsOfService = agreedToTermsOfService
            )
        )
        return apiCall(
            apiCall = { signUpHttp.requestSignUp(signUpRequest) },
            mapper = { result -> result.toDomain() }
        )
    }

    override suspend fun requestCheckNickName(nickname: String): DataResult<Boolean> {
        return apiCall(
            apiCall = { signUpHttp.requestCheckNickName(NickNameDTO(nickname)) },
            mapper = { result -> result.isAvailable }
        )
    }
}