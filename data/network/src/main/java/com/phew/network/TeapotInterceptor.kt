package com.phew.network

import com.phew.core_common.HTTP_TOKEN_ERROR
import com.phew.core_common.WITHDRAWAL_USER
import com.phew.core_common.di.ApplicationScope
import com.phew.domain.interceptor.GlobalEvent
import com.phew.domain.interceptor.GlobalEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import okio.IOException
import javax.inject.Inject
import com.phew.domain.interceptor.InterceptorManger
import kotlinx.coroutines.runBlocking
import okhttp3.Request

class TeapotInterceptor @Inject constructor(
    private val globalEventBus: GlobalEventBus,
    private val interceptorManger: InterceptorManger,
    @ApplicationScope private val scope: CoroutineScope,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        when (response.code) {
            WITHDRAWAL_USER -> {
                response.close()
                scope.launch {
                    globalEventBus.emitEvent(GlobalEvent.TeapotEvent)
                    val deleteToken = interceptorManger.deleteAll()
                    if (!deleteToken) throw RuntimeException("Fail to delete data")
                }
                throw IOException("Force Logout by 418")
            }

            HTTP_TOKEN_ERROR -> {
                val newResponse = runBlocking {
                    val newAccessToken = interceptorManger.autoLogin()
                    if (newAccessToken.isEmpty()) {
                        globalEventBus.emitEvent(GlobalEvent.TeapotEvent)
                        val deleteToken = interceptorManger.deleteAll()
                        if (!deleteToken) throw RuntimeException("Fail to delete data")
                        return@runBlocking null
                    }
                    response.close()
                    val newRequest = newRequestWithToken(response.request, newAccessToken)
                    return@runBlocking chain.proceed(newRequest)
                }
                if (newResponse != null) {
                    return newResponse
                }
            }
        }
        return response
    }

    private fun newRequestWithToken(request: Request, token: String): Request {
        return request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
    }
}