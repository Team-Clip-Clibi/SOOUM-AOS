package com.phew.domain.usecase

import com.phew.core_common.DomainResult
import com.phew.domain.repository.network.MembersRepository
import javax.inject.Inject

class GetActivityRestrictionDate @Inject constructor(
    private val repository: MembersRepository
) {
    suspend operator fun invoke(): DomainResult<String?, Unit> {
        return try {
            val result = repository.getActivityRestrictionDate()
            result.fold(
                onSuccess = { data ->
                    DomainResult.Success(data)
                },
                onFailure = {
                    DomainResult.Failure(Unit)
                }
            )
        } catch (e: Exception) {
            DomainResult.Failure(Unit)
        }
    }
}