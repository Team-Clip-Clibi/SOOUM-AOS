package com.phew.network

import android.util.Log
import com.phew.core_common.ERROR_FAIL_JOB
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
    private val tag = "TeapotInterceptor"
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        when (response.code) {
            WITHDRAWAL_USER -> {
                response.close()
                runBlocking {
                    val deleteToken = interceptorManger.deleteAll()
                    if (!deleteToken) {
                        Log.e(tag, "Failed to delete tokens for withdrawn user.")
                        globalEventBus.emitEvent(GlobalEvent.Error(ERROR_FAIL_JOB))
                        return@runBlocking
                    }
                }
                scope.launch {
                    globalEventBus.emitEvent(GlobalEvent.TeapotEvent)
                }
                throw IOException("Force Logout by 418")
            }

            HTTP_TOKEN_ERROR -> {
                val failedToken = response.request.header("Authorization")?.removePrefix("Bearer ")
                val newResponse = runBlocking {
                    val currentToken = interceptorManger.getAccessToken()
                    if (failedToken != null && failedToken != currentToken) {
                        val newRequest = newRequestWithToken(response.request, currentToken)
                        return@runBlocking chain.proceed(newRequest)
                    }
                    var newAccessToken = interceptorManger.refreshAndGetNewToken()
                    if (newAccessToken.isEmpty()) {
                        newAccessToken = interceptorManger.autoLogin()
                    }
                    if (newAccessToken.isEmpty()) {
                        Log.e(tag, "Failed to refresh and auto login.")
                        val deleteToken = interceptorManger.deleteAll()
                        if (!deleteToken) {
                            Log.e(tag, "Failed to delete tokens for withdrawn user.")
                            globalEventBus.emitEvent(GlobalEvent.Error(ERROR_FAIL_JOB))
                            return@runBlocking null
                        }
                        globalEventBus.emitEvent(GlobalEvent.TeapotEvent)
                        return@runBlocking null
                    }
                    if (newAccessToken == failedToken) {
                        globalEventBus.emitEvent(GlobalEvent.Error(ERROR_FAIL_JOB))
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