package com.phew.domain.usecase

import com.phew.core_common.DataResult
import com.phew.core_common.ERROR_NO_DATA
import com.phew.domain.BuildConfig
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.network.ProfileRepository
import javax.inject.Inject

class AutoLogin @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(): Boolean {
        val token = deviceRepository.requestToken(BuildConfig.TOKEN_KEY)
        if (token.first == ERROR_NO_DATA || token.second == ERROR_NO_DATA) return false
        when (val profile = profileRepository.requestMyProfile()) {
            is DataResult.Fail -> {
                deviceRepository.deleteAll()
                return false
            }

            is DataResult.Success -> {
                val data = profile.data
                val saveProfileResult = deviceRepository.saveProfileInfo(
                    profileKey = BuildConfig.PROFILE_KEY,
                    nickName = data.nickname,
                    profileImageUrl = data.profileImageUrl,
                    profileImageName = data.profileImgName
                )
                if (!saveProfileResult) {
                    deviceRepository.deleteAll()
                    return false
                }
                return true
            }
        }
    }
}