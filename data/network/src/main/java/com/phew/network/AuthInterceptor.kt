package com.phew.network


import com.phew.domain.token.TokenManger
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Invocation
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManger: TokenManger
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val invocation = chain.request().tag(Invocation::class.java)
        val noAuth = invocation?.method()?.getAnnotation(NoAuth::class.java)
        if (noAuth != null) {
            return chain.proceed(chain.request())
        }
        val accessToken = runBlocking { tokenManger.getAccessToken() }
        val request = chain.request().newBuilder()
            .apply {
                if (accessToken.isNotEmpty()) {
                    header("Authorization", "Bearer $accessToken")
                }
            }.build()
        return chain.proceed(request)
    }
}