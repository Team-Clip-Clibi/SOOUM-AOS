package com.phew.domain.repository

import com.phew.core_common.DataResult
import com.phew.domain.dto.CheckSignUp
import com.phew.domain.dto.Notice
import com.phew.domain.dto.Notification
import com.phew.domain.dto.Token
import com.phew.domain.dto.UploadImageUrl
import okhttp3.RequestBody

interface NetworkRepository {
    suspend fun requestAppVersion(type: String, appVersion: String): DataResult<String>
    suspend fun requestSecurityKey(): DataResult<String>
    suspend fun requestCheckSignUp(info: String): DataResult<CheckSignUp>
    suspend fun requestLogin(info: String): DataResult<Pair<String, String>>
    suspend fun requestUpdateFcm(token: String, fcmToken: String): DataResult<Unit>
    suspend fun requestSignUp(
        encryptedDeviceId: String,
        fcmToken: String,
        isNotificationAgreed: Boolean,
        nickname: String,
        profileImage: String?,
        agreedToTermsOfService: Boolean,
        agreedToLocationTerms: Boolean,
        agreedToPrivacyPolicy: Boolean,
    ): DataResult<Pair<String, String>>

    suspend fun requestNickName(): DataResult<String>
    suspend fun requestCheckNickName(nickname: String): DataResult<Boolean>
    suspend fun requestUploadImageUrl(): DataResult<UploadImageUrl>
    suspend fun requestUploadImage(data: RequestBody, url: String): DataResult<Unit>
    suspend fun requestRefreshToken(data: Token): DataResult<Token>
    suspend fun requestNotice(accessToken: String): DataResult<Pair<Int , List<Notice>>>
    suspend fun requestNoticePatch(accessToken: String , lastId : Int): DataResult<Pair<Int , List<Notice>>>
    suspend fun requestNotificationUnRead(accessToken : String) : DataResult<Pair<Int,List<Notification>>>
    suspend fun requestNotificationUnReadPatch(accessToken: String , lastId: Long) : DataResult<Pair<Int,List<Notification>>>
    suspend fun requestNotificationRead(accessToken: String) :DataResult<Pair<Int,List<Notification>>>
    suspend fun requestNotificationReadPatch(accessToken: String , lastId: Long) : DataResult<Pair<Int,List<Notification>>>
}