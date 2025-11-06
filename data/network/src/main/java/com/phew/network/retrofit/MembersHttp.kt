package com.phew.network.retrofit

import com.phew.network.dto.ActivityRestrictionDateDTO
import com.phew.network.dto.TransferCodeDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PATCH

interface MembersHttp {
    @GET("/api/members/activity-restriction-date")
    suspend fun getActivityRestrictionDate(): Response<ActivityRestrictionDateDTO>
    
    @GET("/api/members/account/transfer-code")
    suspend fun getTransferCode(): Response<TransferCodeDTO>
    
    @PATCH("/api/members/account/transfer-code")
    suspend fun refreshTransferCode(): Response<TransferCodeDTO>
}