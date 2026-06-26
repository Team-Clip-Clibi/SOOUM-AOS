package com.phew.domain

import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.mapResult

suspend fun <T, R> safeUseCase(
    mapper: (T) -> R,
    apiCall: suspend () -> Result<T>
): Result<R> {
    return apiCall().mapResult(
        success = mapper,
    ) { code, _ ->
        when (code) {
            HTTP_INVALID_TOKEN -> ERROR_LOGOUT
            APP_ERROR_CODE -> ERROR_FAIL_JOB
            else -> ERROR_NETWORK
        }
    }
}
