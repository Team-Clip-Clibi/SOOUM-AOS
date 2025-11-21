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
        val failedToken = response.request.header("Authorization")?.removePrefix("Bearer ")
        return runBlocking {
            val currentToken = tokenManger.getAccessToken()
            if (failedToken != null && failedToken != currentToken) {
                return@runBlocking newRequestWithToken(response.request, currentToken)
            }
            var newAccessToken = tokenManger.refreshAndGetNewToken()
            if (newAccessToken.isEmpty()) {
                newAccessToken = tokenManger.autoLogin()
            }
            if (newAccessToken.isEmpty()) {
                return@runBlocking null
            }
            if (newAccessToken == failedToken) {
                return@runBlocking null
            }
            newRequestWithToken(response.request, newAccessToken)
        }
    }
    private fun newRequestWithToken(request: Request, token: String): Request {
        return request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
    }
}