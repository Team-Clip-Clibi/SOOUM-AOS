package com.phew.domain.usecase

import com.phew.core_common.DataResult
import com.phew.domain.BuildConfig
import com.phew.domain.model.NotifyToggle
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.network.MembersRepository
import java.io.IOException
import javax.inject.Inject

class ToggleNotification @Inject constructor(
    private val membersRepository: MembersRepository,
    private val deviceRepository: DeviceRepository
) {
    suspend operator fun invoke(): DataResult<NotifyToggle> {
        return try {
            val userInfo = deviceRepository.getUserInfo(BuildConfig.USER_INFO_KEY)
                ?: return DataResult.Fail(throwable = IOException("User info not found"))

            val result = membersRepository.toggleNotification()
            if (result.isSuccess) {
                val notifyToggle = result.getOrNull()!!
                val updatedUserInfo = userInfo.copy(isNotifyAgree = notifyToggle.isAllowNotify)
                deviceRepository.saveUserInfo(
                    key = BuildConfig.USER_INFO_KEY,
                    nickName = updatedUserInfo.nickName,
                    isNotifyAgree = updatedUserInfo.isNotifyAgree,
                    agreedToLocationTerms = updatedUserInfo.agreedToLocationTerms,
                    agreedToPrivacyPolicy = updatedUserInfo.agreedToPrivacyPolicy,
                    agreedToTermsOfService = updatedUserInfo.agreedToTermsOfService
                )
                DataResult.Success(notifyToggle)
            } else {
                DataResult.Fail(throwable = result.exceptionOrNull())
            }
        } catch (e: Exception) {
            DataResult.Fail(throwable = e)
        }
    }
}