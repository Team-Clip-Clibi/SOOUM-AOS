package com.phew.network.retrofit

import com.phew.network.BuildConfig
import com.phew.network.dto.ActivityRestrictionDateDTO
import com.phew.network.dto.TransferCodeDTO
import com.phew.network.dto.request.account.TransferAccountRequestDTO
import com.phew.network.dto.request.account.WithdrawalRequestDTO
import com.phew.network.dto.response.RejoinableDateResponseDTO
import com.phew.network.dto.request.NotifyToggleRequestDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MembersHttp {
    @GET(BuildConfig.API_URL_ACTIVITY_RESTRICTION)
    suspend fun getActivityRestrictionDate(): Response<ActivityRestrictionDateDTO>
    
    @GET(BuildConfig.API_URL_TRANSFER_CODE)
    suspend fun getTransferCode(): Response<TransferCodeDTO>
    
    @PATCH(BuildConfig.API_URL_REFRESH_TRANSFER_CODE)
    suspend fun refreshTransferCode(): Response<TransferCodeDTO>
    
    @POST(BuildConfig.API_URL_TRANSFER_ACCOUNT)
    suspend fun transferAccount(@Body request: TransferAccountRequestDTO): Response<Unit>
    
    @HTTP(method = "DELETE", path = BuildConfig.API_URL_WITHDRAWAL_ACCOUNT, hasBody = true)
    suspend fun withdrawalAccount(@Body request: WithdrawalRequestDTO): Response<Unit>
    
    @GET(BuildConfig.API_URL_REJOINABLE_DATE)
    suspend fun getRejoinableDate(): Response<RejoinableDateResponseDTO>
    
    @PATCH(BuildConfig.API_URL_NOTIFY_TOGGLE)
    suspend fun toggleNotification(@Body request: NotifyToggleRequestDTO): Response<Unit>
}