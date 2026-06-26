package com.phew.domain.usecase


import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_BAD_REQUEST
import com.phew.core_common.HTTP_TOKEN_ERROR
import com.phew.core_common.WITHDRAWAL_USER
import com.phew.core_common.mapResult
import com.phew.domain.model.TagInfo
import com.phew.domain.repository.network.TagRepository
import javax.inject.Inject

class GetTagRank @Inject constructor(
    private val repository: TagRepository,
) {
    suspend operator fun invoke(): Result<List<TagInfo>> {
        return repository.getTagRank().mapResult(
            success = { result ->
                result.tagInfos.filter { data -> data.usageCnt > 0 }
                .sortedByDescending { data -> data.usageCnt }
            },
        ) { code, _ ->
            when (code) {
                HTTP_INVALID_TOKEN, WITHDRAWAL_USER, HTTP_TOKEN_ERROR -> ERROR_LOGOUT
                HTTP_BAD_REQUEST -> ERROR_NETWORK
                else -> ERROR_FAIL_JOB
            }
        }
    }
}
