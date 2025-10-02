package com.phew.repository

import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.DataResult
import com.phew.core_common.HTTP_NO_MORE_CONTENT
import com.phew.domain.dto.CheckSignUp
import com.phew.domain.dto.Notice
import com.phew.domain.dto.Notification
import com.phew.domain.dto.Token
import com.phew.domain.dto.UploadImageUrl
import com.phew.domain.repository.NetworkRepository
import com.phew.network.Http
import com.phew.network.dto.FCMToken
import com.phew.network.dto.InfoDTO
import com.phew.network.dto.MemberInfoDTO
import com.phew.network.dto.NickNameDTO
import com.phew.network.dto.PolicyDTO
import com.phew.network.dto.SignUpRequest
import com.phew.network.dto.TokenDTO
import com.phew.repository.mapper.toDomain
import okhttp3.RequestBody
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
                    time = result.body()!!.rejoinAvailableAt ?: "",
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
        profileImage: String?,
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
                        nickname = nickname,
                        deviceType = "ANDROID"
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

    override suspend fun requestUploadImageUrl(): DataResult<UploadImageUrl> {
        try {
            val request = http.requestUploadImageUrl()
            if (!request.isSuccessful || request.body() == null) return DataResult.Fail(
                code = request.code(),
                message = request.message()
            )
            return DataResult.Success(
                UploadImageUrl(
                    imgUrl = request.body()!!.imgUrl,
                    imgName = request.body()!!.imgName
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

    override suspend fun requestUploadImage(data: RequestBody, url: String): DataResult<Unit> {
        try{
            val request = http.requestUploadImage(
                url = url,
                body = data
            )
            if(!request.isSuccessful) return DataResult.Fail(code = request.code(), message = request.message())
            return DataResult.Success(Unit)
        }catch (e: Exception) {
            e.printStackTrace()
            return DataResult.Fail(
                code = APP_ERROR_CODE,
                message = e.message,
                throwable = e
            )
        }
    }

    override suspend fun requestRefreshToken(data: Token): DataResult<Token> {
        try {
            val request = http.requestRefreshToken(
                body = TokenDTO(
                    refreshToken = data.refreshToken,
                    accessToken = data.accessToken
                )
            )
            if (!request.isSuccessful || request.body() == null) return DataResult.Fail(
                code = request.code(),
                message = request.message()
            )
            return DataResult.Success(
                Token(
                    refreshToken = data.refreshToken,
                    accessToken = data.accessToken
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

    override suspend fun requestNotice(accessToken: String): DataResult<Pair<Int, List<Notice>>> {
        try {
            val request = http.requestNotice(
                bearerToken = accessToken
            )
            if (!request.isSuccessful) return DataResult.Fail(
                code = request.code(),
                message = request.message()
            )
            if (request.body() == null && request.code() == HTTP_NO_MORE_CONTENT) {
                return DataResult.Success(Pair(request.code(), emptyList()))
            }
            val body = request.body()!!
            if (body.notices.isEmpty()) {
                return DataResult.Success(Pair(request.code(), emptyList()))
            }
            return DataResult.Success(
                Pair(request.code(), body.notices.map { data ->
                    Notice(
                        title = data.title,
                        url = data.url,
                        createdAt = data.createdAt,
                        id = data.id
                    )
                })
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

    override suspend fun requestNoticePatch(
        accessToken: String,
        lastId: Int,
    ): DataResult<Pair<Int, List<Notice>>> {
        try {
            val request = http.requestNoticePatch(
                bearerToken = accessToken,
                lastId = lastId
            )
            if (!request.isSuccessful) return DataResult.Fail(
                code = request.code(),
                message = request.message()
            )
            if (request.body() == null && request.code() == HTTP_NO_MORE_CONTENT) {
                return DataResult.Success(Pair(request.code(), emptyList()))
            }
            val body = request.body()!!
            if (body.notices.isEmpty()) {
                return DataResult.Success(Pair(request.code(), emptyList()))
            }
            return DataResult.Success(
                Pair(request.code(), body.notices.map { data ->
                    Notice(
                        title = data.title,
                        url = data.url,
                        createdAt = data.createdAt,
                        id = data.id
                    )
                })
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

    override suspend fun requestNotificationUnRead(accessToken: String): DataResult<Pair<Int, List<Notification>>> {
        try {
            val request = http.requestNotificationUnRead(bearerToken = accessToken)
            if (!request.isSuccessful) {
                return DataResult.Fail(code = request.code(), message = request.message())
            }
            if (request.body() == null && request.code() == HTTP_NO_MORE_CONTENT) {
                return DataResult.Success(Pair(request.code(), emptyList()))
            }
            val data = request.body()!!
            if (data.isEmpty()) {
                return DataResult.Success(Pair(request.code(), emptyList()))
            }
            val domainBody = data.map { data ->
                data.toDomain()
            }
            return DataResult.Success(Pair(request.code(), domainBody))
        } catch (e: Exception) {
            e.printStackTrace()
            return DataResult.Fail(
                code = APP_ERROR_CODE,
                message = e.message,
                throwable = e
            )
        }
    }

    override suspend fun requestNotificationUnReadPatch(
        accessToken: String,
        lastId: Long
    ): DataResult<Pair<Int, List<Notification>>> {
        try {
            val request =
                http.requestNotificationUnReadPatch(bearerToken = accessToken, lastId = lastId)
            if (!request.isSuccessful) {
                return DataResult.Fail(code = request.code(), message = request.message())
            }
            if (request.body() == null && request.code() == HTTP_NO_MORE_CONTENT) {
                return DataResult.Success(Pair(request.code(), emptyList()))
            }
            val data = request.body()!!
            if (data.isEmpty()) {
                return DataResult.Success(Pair(request.code(), emptyList()))
            }
            val domainBody = data.map { data ->
                data.toDomain()
            }
            return DataResult.Success(Pair(request.code(), domainBody))
        } catch (e: Exception) {
            e.printStackTrace()
            return DataResult.Fail(
                code = APP_ERROR_CODE,
                message = e.message,
                throwable = e
            )
        }
    }

    override suspend fun requestNotificationRead(accessToken: String): DataResult<Pair<Int, List<Notification>>> {
        try {
            val request =
                http.requestNotificationRead(bearerToken = accessToken)
            if (!request.isSuccessful) {
                return DataResult.Fail(code = request.code(), message = request.message())
            }
            if (request.body() == null && request.code() == HTTP_NO_MORE_CONTENT) {
                return DataResult.Success(Pair(request.code(), emptyList()))
            }
            val data = request.body()!!
            if (data.isEmpty()) {
                return DataResult.Success(Pair(request.code(), emptyList()))
            }
            val domainBody = data.map { data ->
                data.toDomain()
            }
            return DataResult.Success(Pair(request.code(), domainBody))
        } catch (e: Exception) {
            e.printStackTrace()
            return DataResult.Fail(
                code = APP_ERROR_CODE,
                message = e.message,
                throwable = e
            )
        }
    }

    override suspend fun requestNotificationReadPatch(
        accessToken: String,
        lastId: Long
    ): DataResult<Pair<Int, List<Notification>>> {
        try {
            val request =
                http.requestNotificationReadPatch(bearerToken = accessToken, lastId = lastId)
            if (!request.isSuccessful) {
                return DataResult.Fail(code = request.code(), message = request.message())
            }
            if (request.body() == null && request.code() == HTTP_NO_MORE_CONTENT) {
                return DataResult.Success(Pair(request.code(), emptyList()))
            }
            val data = request.body()!!
            if (data.isEmpty()) {
                return DataResult.Success(Pair(request.code(), emptyList()))
            }
            val domainBody = data.map { data ->
                data.toDomain()
            }
            return DataResult.Success(Pair(request.code(), domainBody))
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