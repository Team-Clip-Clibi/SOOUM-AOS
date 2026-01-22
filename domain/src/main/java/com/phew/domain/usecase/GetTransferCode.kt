package com.phew.domain.usecase

import com.phew.core_common.DomainResult
import com.phew.domain.model.TransferCode
import com.phew.domain.repository.network.MembersRepository
import javax.inject.Inject

import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.exception.ServerException

class GetTransferCode @Inject constructor(
    private val repository: MembersRepository
) {
    suspend operator fun invoke(): DomainResult<TransferCode, Int?> {
        return try {
            val result = repository.getTransferCode()
            result.fold(
                onSuccess = { transferCode ->
                    DomainResult.Success(transferCode)
                },
                onFailure = { e ->
                    if (e is ServerException) {
                        DomainResult.Failure(e.code)
                    } else {
                        DomainResult.Failure(APP_ERROR_CODE)
                    }
                }
            )
        } catch (e: Exception) {
            DomainResult.Failure(APP_ERROR_CODE)
        }
    }
}