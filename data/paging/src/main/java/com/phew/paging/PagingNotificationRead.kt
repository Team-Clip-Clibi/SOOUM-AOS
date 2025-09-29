package com.phew.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.phew.core_common.DataResult
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.HTTP_NO_MORE_CONTENT
import com.phew.core_common.TOKEN_FORM
import com.phew.domain.dto.Notification
import com.phew.domain.dto.Token
import com.phew.domain.repository.DeviceRepository
import com.phew.domain.repository.NetworkRepository
import com.phew.domain.token.TokenManger
import javax.inject.Inject

class PagingNotificationRead  @Inject constructor(
    private val networkRepository: NetworkRepository,
    private val deviceRepository: DeviceRepository,
    private val tokenManger: TokenManger
) : PagingSource<Long, Notification>() {

    override fun getRefreshKey(state: PagingState<Long, Notification>): Long? {
        return -1
    }

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Notification> {
        try {
            val token = deviceRepository.requestToken(BuildConfig.TOKEN_KEY)
            val key = params.key ?: -1
            val result = if (key == -1L) {
                networkRepository.requestNotificationRead(
                    accessToken = TOKEN_FORM +token.second,
                )
            } else {
                networkRepository.requestNotificationReadPatch(
                    accessToken = TOKEN_FORM +token.second,
                    lastId = key
                )
            }
            when (result) {
                is DataResult.Fail -> {
                    if (result.code != HTTP_INVALID_TOKEN) {
                        return LoadResult.Error(Throwable(ERROR_NETWORK))
                    }
                    val refreshToken = tokenManger.requestUpdateToken(
                        Token(
                            refreshToken = token.first,
                            accessToken = token.second
                        )
                    )
                    if (!refreshToken) {
                        return LoadResult.Error(Throwable(ERROR_LOGOUT))
                    }
                    val newToken = deviceRepository.requestToken(BuildConfig.TOKEN_KEY)
                    val reRequest = if (params.key == -1L) {
                        networkRepository.requestNotificationRead(accessToken = TOKEN_FORM +newToken.second)
                    } else {
                        networkRepository.requestNotificationReadPatch(
                            accessToken = TOKEN_FORM +newToken.second,
                            lastId = params.key!!
                        )
                    }
                    if (reRequest is DataResult.Fail) {
                        return LoadResult.Error(Throwable(reRequest.throwable))
                    }
                    return LoadResult.Page(
                        data = reRequest.let { result -> (result as DataResult.Success).data.second },
                        prevKey = null,
                        nextKey = reRequest.let { result -> (result as DataResult.Success).data.second.last().notificationId },
                    )
                }

                is DataResult.Success -> {
                    val data = result.data
                    if (data.second.isEmpty() && data.first == HTTP_NO_MORE_CONTENT) {
                        return LoadResult.Page(data = emptyList(), prevKey = null, nextKey = null)
                    }
                    return LoadResult.Page(
                        data = data.second,
                        prevKey = null,
                        nextKey = data.second.last().notificationId
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return LoadResult.Error(e)
        }
    }
}