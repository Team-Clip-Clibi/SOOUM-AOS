package com.phew.domain.usecase

import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.domain.dto.TagInfo
import com.phew.domain.repository.network.CardFeedRepository
import javax.inject.Inject

class GetRelatedTag @Inject constructor(private val repository: CardFeedRepository) {
    data class Param(
        val tag: String,
        val resultCnt : Int
    )

    suspend operator fun invoke(data : Param): DomainResult<List<TagInfo>, String> {
        val request = repository.requestRelatedTag(resultCnt = data.resultCnt , tag = data.tag)
        when(request){
            is DataResult.Fail -> {
                when(request.code){
                    APP_ERROR_CODE -> {
                        return DomainResult.Failure(ERROR_FAIL_JOB)
                    }
                    HTTP_INVALID_TOKEN -> {
                        return DomainResult.Failure(ERROR_LOGOUT)
                    }
                    else -> return DomainResult.Failure(ERROR_NETWORK)
                }
            }
            is DataResult.Success -> {
                return DomainResult.Success(request.data)
            }
        }
    }
}