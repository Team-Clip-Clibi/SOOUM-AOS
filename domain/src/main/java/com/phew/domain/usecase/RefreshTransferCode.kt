package com.phew.domain.usecase

import com.phew.core_common.DomainResult
import com.phew.domain.model.TransferCode
import com.phew.domain.repository.network.MembersRepository
import javax.inject.Inject

import com.phew.core_common.exception.ServerException

class RefreshTransferCode @Inject constructor(
    private val repository: MembersRepository
) {
    suspend operator fun invoke(): DomainResult<TransferCode, Int?> {
        return try {
            val result = repository.refreshTransferCode()
            result.fold(
                onSuccess = { transferCode ->
                    DomainResult.Success(transferCode)
                },
                onFailure = { e ->
                    if (e is ServerException) {
                        DomainResult.Failure(e.code)
                    } else {
                        DomainResult.Failure(null)
                    }
                }
            )
        } catch (e: Exception) {
            DomainResult.Failure(null)
        }
    }
}