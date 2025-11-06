package com.phew.domain.usecase

import com.phew.core_common.DomainResult
import com.phew.domain.model.TransferCode
import com.phew.domain.repository.network.MembersRepository
import javax.inject.Inject

class GetTransferCode @Inject constructor(
    private val repository: MembersRepository
) {
    suspend operator fun invoke(): DomainResult<TransferCode, Unit> {
        return try {
            val result = repository.getTransferCode()
            result.fold(
                onSuccess = { transferCode ->
                    DomainResult.Success(transferCode)
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