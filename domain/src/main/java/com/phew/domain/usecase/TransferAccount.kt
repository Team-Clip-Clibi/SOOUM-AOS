package com.phew.domain.usecase

import com.phew.domain.repository.network.MembersRepository
import javax.inject.Inject

class TransferAccount @Inject constructor(
    private val membersRepository: MembersRepository
) {
    data class Param(
        val transferCode: String
    )
    
    suspend operator fun invoke(param: Param): Result<Unit> {
        return membersRepository.transferAccount(param.transferCode)
    }
}