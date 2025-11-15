package com.phew.domain.repository.network

import com.phew.domain.model.RejoinableDate
import com.phew.domain.model.TransferCode

interface MembersRepository {
    suspend fun getActivityRestrictionDate(): Result<String?>
    suspend fun getTransferCode(): Result<TransferCode>
    suspend fun refreshTransferCode(): Result<TransferCode>
    suspend fun transferAccount(transferCode: String): Result<Unit>
    suspend fun withdrawalAccount(reason: String): Result<Unit>
    suspend fun getRejoinableDate(): Result<RejoinableDate>
}