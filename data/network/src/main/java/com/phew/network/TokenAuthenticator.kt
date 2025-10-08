package com.phew.network


import com.phew.domain.token.TokenManger
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val tokenManger: TokenManger
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val newAccessToken = runBlocking { tokenManger.refreshAndGetNewToken() }
        if (newAccessToken == null) return null
        return response.request
            .newBuilder()
            .header("Authorization", "Bearer $newAccessToken")
            .build()
    }
}