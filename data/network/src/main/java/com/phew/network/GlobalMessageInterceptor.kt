package com.phew.network

import com.phew.core_common.HTTP_GLOBAL_MESSAGE
import com.phew.core_common.di.ApplicationScope
import com.phew.domain.interceptor.GlobalEvent
import com.phew.domain.interceptor.GlobalEventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class GlobalMessageInterceptor @Inject constructor(
    private val json: Json,
    private val globalEventBus: GlobalEventBus,
    @ApplicationScope private val scope: CoroutineScope,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        if (response.code == HTTP_GLOBAL_MESSAGE) {
            parseMessage(response)?.let { message ->
                scope.launch {
                    globalEventBus.emitEvent(GlobalEvent.ServerMessage(message))
                }
            }
        }

        return response
    }

    private fun parseMessage(response: Response): String? = runCatching {
        val responseBody = response.peekBody(MAX_ERROR_BODY_BYTES).string()
        parseGlobalMessage(json, responseBody)
    }.getOrNull()

    private companion object {
        const val MAX_ERROR_BODY_BYTES = 1_048_576L
    }
}

internal fun parseGlobalMessage(json: Json, responseBody: String): String? =
    json.parseToJsonElement(responseBody)
        .jsonObject["message"]
        ?.jsonPrimitive
        ?.contentOrNull
        ?.takeIf(String::isNotBlank)
