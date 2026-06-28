package com.phew.domain.orchestrator

import androidx.paging.PagingData
import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.ERROR_NO_DATA
import com.phew.core_common.exception.ServerException
import com.phew.core_common.resultFailure
import com.phew.domain.BuildConfig
import com.phew.domain.dto.Alarm
import com.phew.domain.dto.Notice
import com.phew.domain.dto.NoticeSource
import com.phew.domain.interceptor.InterceptorManger
import com.phew.domain.model.AppVersionStatus
import com.phew.domain.model.AppVersionStatusType
import com.phew.domain.model.BlockMember
import com.phew.domain.model.RejoinableDate
import com.phew.domain.model.TransferCode
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.PagerRepository
import com.phew.domain.repository.event.EventRepository
import com.phew.domain.repository.network.AppVersionRepository
import com.phew.domain.repository.network.CardDetailRepository
import com.phew.domain.repository.network.MembersRepository
import com.phew.domain.repository.network.NotifyRepository
import com.phew.domain.repository.network.SignUpRepository
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.exception.asSooumException
import com.phew.core_common.mapFailureMessage
import com.phew.domain.dto.Token
import kotlinx.coroutines.flow.Flow
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.inject.Inject

/**
 * 설정 화면에서 앱 버전, 알림 설정, 계정 이전/탈퇴, 차단 사용자, 공지/알림 조회 흐름을 조율합니다.
 */
class SettingsUseCaseOrchestrator @Inject constructor(
    private val membersRepository: MembersRepository,
    private val deviceRepository: DeviceRepository,
    private val appVersionRepository: AppVersionRepository,
    private val pagerRepository: PagerRepository,
    private val notifyRepository: NotifyRepository,
    private val cardDetailRepository: CardDetailRepository,
    private val signUpRepository: SignUpRepository,
    private val interceptorManger: InterceptorManger,
    private val eventRepository: EventRepository,
) {
    suspend fun activityRestrictionDate(): Result<String?> =
        try {
            membersRepository.getActivityRestrictionDate().fold(
                onSuccess = { Result.success(it) },
                onFailure = { resultFailure(Unit) },
            )
        } catch (e: Exception) {
            resultFailure(Unit)
        }

    suspend fun checkAppVersion(type: String, isDebugMode: Boolean): Result<AppVersionStatus> {
        return try {
            if (isDebugMode) {
                return Result.success(AppVersionStatus(status = AppVersionStatusType.OK, latestVersion = "1.0.0"))
            }
            val appVersion = deviceRepository.getAppVersion()
            appVersionRepository.checkAppVersion(type, appVersion).fold(
                onSuccess = { Result.success(it) },
                onFailure = { resultFailure(Unit) },
            )
        } catch (e: Exception) {
            resultFailure(Unit)
        }
    }

    suspend fun rejoinableDate(): Result<RejoinableDate> =
        membersRepository.getRejoinableDate()

    suspend fun refreshToken(): String {
        val token = deviceRepository.requestToken(BuildConfig.TOKEN_KEY)
        return token.first.takeUnless { it == ERROR_NO_DATA } ?: ""
    }

    suspend fun setToggleNotification(data: Alarm): Result<Unit> =
        membersRepository.toggleNotification(data).mapFailureMessage { _, _ -> ERROR_NETWORK }

    suspend fun getToggleNotification(): Result<Alarm> =
        membersRepository.getToggleNotification().mapFailureMessage { _, _ -> ERROR_NETWORK }

    fun runHaptic() {
        deviceRepository.requestHaptic()
    }

    suspend fun transferCode(): Result<TransferCode> =
        mapTransferCodeFailure { membersRepository.getTransferCode() }

    suspend fun refreshTransferCode(): Result<TransferCode> =
        mapTransferCodeFailure { membersRepository.refreshTransferCode() }

    fun blockUsers(): Flow<PagingData<BlockMember>> =
        pagerRepository.getBlockUserPaging()

    suspend fun unblockMember(toMemberId: Long): Result<Unit> =
        cardDetailRepository.unblockMember(toMemberId).mapFailureMessage { code, message ->
            when (code) {
                HTTP_INVALID_TOKEN -> ERROR_LOGOUT
                APP_ERROR_CODE -> message.ifBlank { ERROR_FAIL_JOB }
                else -> ERROR_NETWORK
            }
        }

    fun noticePage(source: NoticeSource = NoticeSource.SETTINGS): Flow<PagingData<Notice>> =
        pagerRepository.noticePageStream(source)

    suspend fun feedNotification(source: NoticeSource = NoticeSource.NOTIFICATION): Result<List<Notice>> =
        notifyRepository.requestNotice(pageSize = NOTICE_PREVIEW_SIZE, source = source).fold(
            onSuccess = { request ->
                Result.success(request.second.sortedByDescending { data -> data.id }.take(NOTICE_PREVIEW_SIZE))
            },
            onFailure = { throwable ->
                val exception = throwable.asSooumException()
                resultFailure(
                    code = exception.code,
                    message = if (exception.code == HTTP_INVALID_TOKEN) ERROR_LOGOUT else ERROR_NETWORK,
                    throwable = throwable,
                )
            },
        )

    suspend fun withdrawalAccount(reason: String): Result<Unit> =
        membersRepository.withdrawalAccount(reason)

    suspend fun transferAccount(transferCode: String): Result<Unit> {
        val transferKey = signUpRepository.requestSecurityKey()
            .getOrElse { return resultFailure("Failed to get transfer key") }
        eventRepository.logSuccessTransfer()
        val deviceId = deviceRepository.requestDeviceId()
        val transferEncryptInfo = makeDeviceInfo(key = transferKey, deviceInfo = deviceId)
        val codeResult = membersRepository.transferAccount(
            transferCode = transferCode,
            deviceId = transferEncryptInfo,
        )
        if (codeResult.isFailure) return codeResult
        val loginKey = signUpRepository.requestSecurityKey()
            .getOrElse { return resultFailure("Failed to get login key") }
        val loginEncryptInfo = makeDeviceInfo(key = loginKey, deviceInfo = deviceId)
        val modelName = deviceRepository.requestDeviceModel()
        val osVersion = deviceRepository.requestDeviceOS()
        return signUpRepository.requestLogin(
            info = loginEncryptInfo,
            osVersion = osVersion,
            modelName = modelName,
        ).fold(
            onSuccess = { token ->
                val deleteAll = interceptorManger.deleteAll()
                if (!deleteAll) return resultFailure(ERROR_FAIL_JOB)
                interceptorManger.resetToken()
                val saveToken = deviceRepository.saveToken(
                    key = BuildConfig.TOKEN_KEY,
                    data = Token(refreshToken = token.refreshToken, accessToken = token.accessToken),
                )
                if (!saveToken) return resultFailure("Failed to save token")
                Result.success(Unit)
            },
            onFailure = { Result.failure(it) },
        )
    }

    private suspend fun mapTransferCodeFailure(request: suspend () -> Result<TransferCode>): Result<TransferCode> =
        try {
            request().fold(
                onSuccess = { Result.success(it) },
                onFailure = { e ->
                    if (e is ServerException) {
                        resultFailure(code = e.code)
                    } else {
                        resultFailure(code = APP_ERROR_CODE)
                    }
                },
            )
        } catch (e: Exception) {
            resultFailure(code = APP_ERROR_CODE)
        }

    private fun makeDeviceInfo(key: String, deviceInfo: String): String =
        encrypt(deviceInfo, makeSecurityKey(key))

    private fun makeSecurityKey(key: String): PublicKey {
        val cleanedKey = key.replace("\\s".toRegex(), "")
        val keyBytes = java.util.Base64.getDecoder().decode(cleanedKey)
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

    private companion object {
        private const val NOTICE_PREVIEW_SIZE = 3
    }
}
