package com.phew.network.retrofit

import com.phew.network.dto.ActivityRestrictionDateDTO
import com.phew.network.dto.TransferCodeDTO
import com.phew.network.dto.request.account.TransferAccountRequestDTO
import com.phew.network.dto.request.account.WithdrawalRequestDTO
import com.phew.network.dto.response.RejoinableDateResponseDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface MembersHttp {
    @GET("/api/members/activity-restriction-date")
    suspend fun getActivityRestrictionDate(): Response<ActivityRestrictionDateDTO>
    
    @GET("/api/members/account/transfer-code")
    suspend fun getTransferCode(): Response<TransferCodeDTO>
    
    @PATCH("/api/members/account/transfer-code")
    suspend fun refreshTransferCode(): Response<TransferCodeDTO>
    
    @POST("/api/members/account/transfer")
    suspend fun transferAccount(@Body request: TransferAccountRequestDTO): Response<Unit>
    
    @DELETE("/api/auth/withdrawal")
    suspend fun withdrawalAccount(@Body request: WithdrawalRequestDTO): Response<Unit>
    
    @GET("/api/members/rejoinable-date")
    suspend fun getRejoinableDate(): Response<RejoinableDateResponseDTO>
}