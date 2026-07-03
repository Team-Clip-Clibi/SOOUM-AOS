package com.phew.domain.usecase

import com.phew.core_common.DataResult
import com.phew.core_common.clarity.ClarityInterface
import javax.inject.Inject

class SyncClarityRecording @Inject constructor(
    private val getUserRole: GetUserRole,
    private val clarityInterface: ClarityInterface
) {
    suspend operator fun invoke() {
        when (val result = getUserRole()) {
            is DataResult.Success -> {
                clarityInterface.setEnabled(!result.data.isTester)
            }

            is DataResult.Fail -> Unit
        }
    }
}
