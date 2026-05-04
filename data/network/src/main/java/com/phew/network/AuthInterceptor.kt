package com.phew.network

import com.phew.core_common.AppVersion
import com.phew.domain.interceptor.InterceptorManger
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Invocation
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val interceptorManger: InterceptorManger,
    @param:AppVersion private val appVersion: String,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
            .header("version", appVersion)
        val invocation = chain.request().tag(Invocation::class.java)
        val noAuth = invocation?.method()?.getAnnotation(NoAuth::class.java)
        if (noAuth != null) {
            return chain.proceed(requestBuilder.build())
        }
        val accessToken = runBlocking { interceptorManger.getAccessToken() }
        if (accessToken.isNotEmpty()) {
            requestBuilder.header("Authorization", "Bearer $accessToken")
        }
        return chain.proceed(requestBuilder.build())
    }
}
