package com.phew.repository

import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.DataResult
import com.phew.domain.dto.CheckSignUp
import com.phew.domain.repository.NetworkRepository
import com.phew.network.Http
import com.phew.network.dto.FCMToken
import com.phew.network.dto.InfoDTO
import com.phew.network.dto.MemberInfoDTO
import com.phew.network.dto.NickNameDTO
import com.phew.network.dto.PolicyDTO
import com.phew.network.dto.SignUpRequest
import javax.inject.Inject

class NetworkRepositoryImpl @Inject constructor(private val http: Http) : NetworkRepository {
    override suspend fun requestAppVersion(type: String, appVersion: String): DataResult<String> {
        try {
            val result = http.getVersion(
                type = type,
                data = appVersion
            )
            if (!result.isSuccessful) {
                return DataResult.Fail(code = result.code(), message = result.message())
            }
            if (result.body() == null) {
                return DataResult.Fail(code = result.code(), message = result.message())
            }
            return DataResult.Success(result.body()!!.status)
        } catch (e: Exception) {
            e.printStackTrace()
            return DataResult.Fail(
                code = APP_ERROR_CODE,
                message = e.message,
                throwable = e
            )
        }
    }

    override suspend fun requestSecurityKey(): DataResult<String> {
        try {
            val result = http.getSecurityKey()
            if (!result.isSuccessful) {
                return DataResult.Fail(code = result.code(), message = result.message())
            }
            if (result.body() == null) {
                return DataResult.Fail(code = result.code(), message = result.message())
            }
            return DataResult.Success(result.body()!!.publicKey)
        } catch (e: Exception) {
            e.printStackTrace()
            return DataResult.Fail(
                code = APP_ERROR_CODE,
                message = e.message,
                throwable = e
            )
        }
    }

    override suspend fun requestCheckSignUp(info: String): DataResult<CheckSignUp> {
        try {
            val result = http.requestCheckSignUp(InfoDTO(info))
            if (!result.isSuccessful || result.body() == null) {
                return DataResult.Fail(code = result.code(), message = result.message())
            }
            return DataResult.Success(
                CheckSignUp(
                    time = result.body()!!.rejoinAvailableAt,
                    banned = result.body()!!.banned,
                    registered = result.body()!!.registered,
                    withdrawn = result.body()!!.withdrawn
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return DataResult.Fail(
                code = APP_ERROR_CODE,
                message = e.message,
                throwable = e
            )
        }
    }

    override suspend fun requestLogin(info: String): DataResult<Pair<String, String>> {
        try {
            val result = http.requestLogin(InfoDTO(encryptedDeviceId = info))
            if (!result.isSuccessful || result.body() == null) {
                return DataResult.Fail(code = result.code(), message = result.message())
            }
            return DataResult.Success(
                Pair(
                    result.body()!!.refreshToken,
                    result.body()!!.accessToken
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return DataResult.Fail(
                code = APP_ERROR_CODE,
                message = e.message,
                throwable = e
            )
        }
    }

    override suspend fun requestUpdateFcm(token: String, fcmToken: String): DataResult<Unit> {
        try {
            val request = http.requestUpdateFcm(
                bearerToken = token,
                body = FCMToken(fcmToken)
            )
            if (!request.isSuccessful) {
                return DataResult.Fail(code = request.code(), message = request.message())
            }
            return DataResult.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            return DataResult.Fail(
                code = APP_ERROR_CODE,
                message = e.message,
                throwable = e
            )
        }
    }

    override suspend fun requestSignUp(
        encryptedDeviceId: String,
        fcmToken: String,
        isNotificationAgreed: Boolean,
        nickname: String,
        profileImage: String,
        agreedToTermsOfService: Boolean,
        agreedToLocationTerms: Boolean,
        agreedToPrivacyPolicy: Boolean,
    ): DataResult<Pair<String, String>> {
        try {
            val request = http.requestSignUp(
                SignUpRequest(
                    memberInfo = MemberInfoDTO(
                        encryptedDeviceId = encryptedDeviceId,
                        fcmToken = fcmToken,
                        isNotificationAgreed = isNotificationAgreed,
                        profileImage = profileImage,
                        nickname = nickname
                    ),
                    policy = PolicyDTO(
                        agreedToLocationTerms = agreedToLocationTerms,
                        agreedToPrivacyPolicy = agreedToPrivacyPolicy,
                        agreedToTermsOfService = agreedToTermsOfService
                    )
                )
            )
            if (!request.isSuccessful || request.body() == null) {
                return DataResult.Fail(code = request.code(), message = request.message())
            }
            return DataResult.Success(
                Pair(
                    request.body()!!.refreshToken,
                    request.body()!!.accessToken
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return DataResult.Fail(
                code = APP_ERROR_CODE,
                message = e.message,
                throwable = e
            )
        }
    }

    override suspend fun requestNickName(): DataResult<String> {
        try {
            val request = http.requestNickNameGenerator()
            if (!request.isSuccessful || request.body() == null) {
                return DataResult.Fail(code = request.code(), message = request.message())
            }
            return DataResult.Success(request.body()!!.nickname)
        } catch (e: Exception) {
            e.printStackTrace()
            return DataResult.Fail(
                code = APP_ERROR_CODE,
                message = e.message,
                throwable = e
            )
        }
    }

    override suspend fun requestCheckNickName(nickname: String): DataResult<Boolean> {
        try {
            val request = http.requestCheckNickName(
                NickNameDTO(
                    nickname
                )
            )
            if (!request.isSuccessful || request.body() == null) {
                return DataResult.Fail(code = request.code(), message = request.message())
            }
            return DataResult.Success(request.body()!!.isAvailable)
        } catch (e: Exception) {
            e.printStackTrace()
            return DataResult.Fail(
                code = APP_ERROR_CODE,
                message = e.message,
                throwable = e
            )
        }
    }
}