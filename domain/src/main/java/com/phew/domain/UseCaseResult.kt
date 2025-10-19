package com.phew.domain

import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_INVALID_TOKEN

suspend fun <T, R> safeUseCase(
    mapper: (T) -> R,
    apiCall: suspend () -> DataResult<T>
): DomainResult<R, String> {
    return when (val result = apiCall()) {
        is DataResult.Fail -> {
            when (result.code) {
                HTTP_INVALID_TOKEN -> DomainResult.Failure(ERROR_LOGOUT)
                APP_ERROR_CODE -> DomainResult.Failure(ERROR_FAIL_JOB)
                else -> DomainResult.Failure(ERROR_NETWORK)
            }
        }

        is DataResult.Success -> {
            DomainResult.Success(mapper(result.data))
        }
    }
}