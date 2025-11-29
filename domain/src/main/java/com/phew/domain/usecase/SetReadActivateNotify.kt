package com.phew.domain.usecase

import android.util.Log
import com.phew.core_common.DomainResult
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_SUCCESS
import com.phew.domain.repository.network.NotifyRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

class SetReadActivateNotify @Inject constructor(private val repository: NotifyRepository) {
    data class Param(
        val notifyId: List<Long>
    )
    private val tag = "SetReadActivateNotify"
    suspend operator fun invoke(param: Param): DomainResult<Unit, String> {
        if (param.notifyId.isEmpty()) {
            Log.e(tag , "notify data is Empty")
            return DomainResult.Success(Unit)
        }
        return supervisorScope {
            val deferredJobs = param.notifyId.map { id ->
                async {
                    val result = repository.requestReadNotify(notifyId = id)
                    result == HTTP_SUCCESS
                }
            }
            val results: List<Boolean> = deferredJobs.awaitAll()
            if (results.contains(true)) {
                DomainResult.Success(Unit)
            } else {
                DomainResult.Failure(ERROR_NETWORK)
            }
        }
    }
}