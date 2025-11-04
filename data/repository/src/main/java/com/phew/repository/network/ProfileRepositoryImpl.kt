package com.phew.repository.network

import com.phew.core_common.DataResult
import com.phew.domain.dto.MyProfileInfo
import com.phew.domain.repository.network.ProfileRepository
import com.phew.network.retrofit.ProfileHttp
import com.phew.repository.mapper.apiCall
import com.phew.repository.mapper.toDomain
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(private val http: ProfileHttp) :
    ProfileRepository {
    override suspend fun requestMyProfile(): DataResult<MyProfileInfo> {
        return apiCall(
            apiCall = { http.requestMyProfile() },
            mapper = { data -> data.toDomain() }
        )
    }
}