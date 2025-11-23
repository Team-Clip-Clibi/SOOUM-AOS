package com.phew.network.retrofit

import com.phew.network.BuildConfig
import com.phew.network.dto.response.BlockMemberResponseDTO
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface BlockHttp {
    @GET(BuildConfig.API_URL_BLOCKS)
    suspend fun getBlockList(): Response<List<BlockMemberResponseDTO>>
    
    @GET(BuildConfig.API_URL_BLOCKS_PAGING)
    suspend fun getBlockListPaging(@Path("lastBlockId") lastBlockId: Long): Response<List<BlockMemberResponseDTO>>
}