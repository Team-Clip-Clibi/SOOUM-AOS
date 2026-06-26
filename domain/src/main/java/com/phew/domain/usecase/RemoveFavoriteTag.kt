package com.phew.domain.usecase

import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.mapFailureMessage
import com.phew.domain.repository.network.TagRepository
import javax.inject.Inject

class RemoveFavoriteTag @Inject constructor(
    private val repository: TagRepository
) {
    data class Param(
        val tagId: Long
    )

    suspend operator fun invoke(param: Param): Result<Unit> {
        return repository.removeFavoriteTag(param.tagId).mapFailureMessage { code, _ ->
            when (code) {
                HTTP_INVALID_TOKEN -> ERROR_LOGOUT
                else -> ERROR_NETWORK
            }
        }
    }
}
