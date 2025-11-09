package com.phew.domain.repository.network

import com.phew.domain.model.TransferCode

interface MembersRepository {
    suspend fun getActivityRestrictionDate(): Result<String?>
    suspend fun getTransferCode(): Result<TransferCode>
    suspend fun refreshTransferCode(): Result<TransferCode>
}