package com.phew.domain.usecase

import com.phew.core_common.DataResult
import com.phew.core_common.ERROR_NO_DATA
import com.phew.domain.BuildConfig
import com.phew.domain.model.UserRole
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.network.MembersRepository
import javax.inject.Inject

class GetUserRole @Inject constructor(
    private val repository: MembersRepository,
    private val deviceRepository: DeviceRepository
) {
    suspend operator fun invoke(): DataResult<UserRole> {
        val token = deviceRepository.requestToken(BuildConfig.TOKEN_KEY)
        if (!token.hasValidToken()) {
            return DataResult.Fail(message = "Token is empty")
        }
        return repository.getUserRole()
    }

    private fun Pair<String, String>.hasValidToken(): Boolean {
        return first.isNotBlank() &&
            second.isNotBlank() &&
            first != ERROR_NO_DATA &&
            second != ERROR_NO_DATA
    }
}
