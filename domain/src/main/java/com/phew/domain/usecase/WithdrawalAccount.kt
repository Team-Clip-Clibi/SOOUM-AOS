package com.phew.domain.usecase

import com.phew.domain.repository.network.MembersRepository
import javax.inject.Inject

class WithdrawalAccount @Inject constructor(
    private val membersRepository: MembersRepository
) {
    suspend operator fun invoke(reason: String): Result<Unit> {
        return membersRepository.withdrawalAccount(reason)
    }
}