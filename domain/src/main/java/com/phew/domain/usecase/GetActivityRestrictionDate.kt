package com.phew.domain.usecase

import com.phew.domain.repository.network.MembersRepository
import javax.inject.Inject

class GetActivityRestrictionDate @Inject constructor(
    private val repository: MembersRepository
) {
    suspend operator fun invoke(): Result<String?> {
        return try {
            val result = repository.getActivityRestrictionDate()
            result.fold(
                onSuccess = { data ->
                    Result.success(data)
                },
                onFailure = {
                    com.phew.core_common.resultFailure(Unit)
                }
            )
        } catch (e: Exception) {
            com.phew.core_common.resultFailure(Unit)
        }
    }
}