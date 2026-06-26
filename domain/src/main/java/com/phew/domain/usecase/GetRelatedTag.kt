package com.phew.domain.usecase

import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.mapFailureMessage
import com.phew.domain.dto.TagInfo
import com.phew.domain.repository.network.CardFeedRepository
import javax.inject.Inject

class GetRelatedTag @Inject constructor(private val repository: CardFeedRepository) {
    data class Param(
        val tag: String,
        val resultCnt : Int
    )

    suspend operator fun invoke(data : Param): Result<List<TagInfo>> {
        return repository.requestRelatedTag(resultCnt = data.resultCnt, tag = data.tag)
            .mapFailureMessage { code, _ ->
                when (code) {
                    APP_ERROR_CODE -> ERROR_FAIL_JOB
                    HTTP_INVALID_TOKEN -> ERROR_LOGOUT
                    else -> ERROR_NETWORK
                }
            }
    }
}
