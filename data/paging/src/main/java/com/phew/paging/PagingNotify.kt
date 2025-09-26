package com.phew.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.phew.core_common.DataResult
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.HTTP_NO_MORE_CONTENT
import com.phew.domain.BuildConfig
import com.phew.domain.dto.Notice
import com.phew.domain.dto.Token
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.NetworkRepository
import com.phew.domain.token.TokenManger
import javax.inject.Inject

class PagingNotify @Inject constructor(
    private val networkRepository: NetworkRepository,
    private val deviceRepository: DeviceRepository,
    private val tokenManger: TokenManger,
) : PagingSource<Int, Notice>() {
    override fun getRefreshKey(state: PagingState<Int, Notice>): Int {
        return -1
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Notice> {
        return try {
            val token = deviceRepository.requestToken(BuildConfig.TOKEN_KEY)
            val key = params.key ?: -1
            val result = if (key == -1) {
                networkRepository.requestNotice(accessToken = token.second)
            } else {
                networkRepository.requestNoticePatch(
                    accessToken = token.second,
                    lastId = key
                )
            }
            when (result) {
                is DataResult.Success -> {
                    val responseCode = result.data.first
                    if (responseCode == HTTP_NO_MORE_CONTENT || result.data.second.isEmpty()) {
                        return LoadResult.Page(
                            data = emptyList(),
                            prevKey = null,
                            nextKey = null
                        )
                    }
                    LoadResult.Page(
                        data = result.data.second,
                        prevKey = null,
                        nextKey = result.data.second.last().id
                    )
                }

                is DataResult.Fail -> {
                    if (result.code != HTTP_INVALID_TOKEN) {
                        return LoadResult.Error(Throwable(result.throwable))
                    }
                    val requestRefreshToken = tokenManger.requestUpdateToken(
                        Token(
                            refreshToken = token.first,
                            accessToken = token.second
                        )
                    )
                    if (!requestRefreshToken) {
                        return LoadResult.Error(Throwable(ERROR_NETWORK))
                    }
                    val newToken = deviceRepository.requestToken(BuildConfig.TOKEN_KEY)
                    val reRequest = if (params.key == -1) {
                        networkRepository.requestNotice(accessToken = newToken.second)
                    } else {
                        networkRepository.requestNoticePatch(
                            accessToken = newToken.second,
                            lastId = params.key!!
                        )
                    }
                    if (reRequest is DataResult.Fail) {
                        return LoadResult.Error(Throwable(reRequest.throwable))
                    }
                    LoadResult.Page(
                        data = reRequest.let { (it as DataResult.Success).data.second },
                        prevKey = null,
                        nextKey = reRequest.let { (it as DataResult.Success).data.second.last().id }
                    )
                }
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}