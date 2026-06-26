package com.phew.domain.usecase

import com.phew.core_common.HTTP_BAD_REQUEST
import com.phew.core_common.HTTP_CONFLICT
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.ERROR_TAG_FAVORITE_MAX_EXCEEDED
import com.phew.core_common.ERROR_TAG_FAVORITE_ALREADY_EXISTS
import com.phew.core_common.mapFailureMessage
import com.phew.domain.repository.event.EventRepository
import com.phew.domain.repository.network.TagRepository
import javax.inject.Inject

class AddFavoriteTag @Inject constructor(
    private val repository: TagRepository,
    private val eventRepository: EventRepository
) {
    data class Param(
        val tagId: Long
    )

    suspend operator fun invoke(param: Param): Result<Unit> {
        eventRepository.logTagRegisterTag()
        return repository.addFavoriteTag(param.tagId).mapFailureMessage { code, _ ->
            when (code) {
                HTTP_BAD_REQUEST -> ERROR_TAG_FAVORITE_MAX_EXCEEDED
                HTTP_CONFLICT -> ERROR_TAG_FAVORITE_ALREADY_EXISTS
                HTTP_INVALID_TOKEN -> ERROR_LOGOUT
                else -> ERROR_NETWORK
            }
        }
    }
}
