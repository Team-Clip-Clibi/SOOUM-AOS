package com.phew.domain.usecase

import com.phew.domain.model.TransferCode
import com.phew.domain.repository.network.MembersRepository
import javax.inject.Inject

import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.exception.ServerException

class RefreshTransferCode @Inject constructor(
    private val repository: MembersRepository
) {
    suspend operator fun invoke(): Result<TransferCode> {
        return try {
            val result = repository.refreshTransferCode()
            result.fold(
                onSuccess = { transferCode ->
                    Result.success(transferCode)
                },
                onFailure = { e ->
                    if (e is ServerException) {
                        com.phew.core_common.resultFailure(e.code)
                    } else {
                        com.phew.core_common.resultFailure(APP_ERROR_CODE)
                    }
                }
            )
        } catch (e: Exception) {
            com.phew.core_common.resultFailure(APP_ERROR_CODE)
        }
    }
}