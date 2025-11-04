package com.phew.domain.repository.network

import com.phew.core_common.DataResult
import com.phew.domain.dto.MyProfileInfo

interface ProfileRepository {
    suspend fun requestMyProfile(): DataResult<MyProfileInfo>
}