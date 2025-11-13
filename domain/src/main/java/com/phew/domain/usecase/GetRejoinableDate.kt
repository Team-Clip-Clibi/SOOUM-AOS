package com.phew.domain.usecase

import com.phew.domain.model.RejoinableDate
import com.phew.domain.repository.network.MembersRepository
import javax.inject.Inject

class GetRejoinableDate @Inject constructor(
    private val membersRepository: MembersRepository
) {
    suspend operator fun invoke(): Result<RejoinableDate> {
        return membersRepository.getRejoinableDate()
    }
}