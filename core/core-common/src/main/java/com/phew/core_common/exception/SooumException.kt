package com.phew.core_common.exception

import com.phew.core_common.APP_ERROR_CODE
import com.phew.core_common.ERROR_ACCOUNT_SUSPENDED
import com.phew.core_common.ERROR_ALREADY_CARD_DELETE
import com.phew.core_common.ERROR_FAIL_JOB
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_common.ERROR_NO_DATA
import com.phew.core_common.HTTP_CARD_ALREADY_DELETE
import com.phew.core_common.HTTP_GLOBAL_MESSAGE
import com.phew.core_common.HTTP_INVALID_TOKEN
import com.phew.core_common.HTTP_NO_MORE_CONTENT
import com.phew.core_common.HTTP_TOKEN_ERROR
import com.phew.core_common.WITHDRAWAL_USER

class SooumException(
    val code: Int = APP_ERROR_CODE,
    override val message: String = ERROR_NETWORK,
    override val cause: Throwable? = null,
) : Exception(message, cause)

fun sooumExceptionOf(
    code: Int = APP_ERROR_CODE,
    message: String? = null,
    cause: Throwable? = null,
): SooumException {
    val resolvedMessage = message ?: when (code) {
        HTTP_INVALID_TOKEN, HTTP_TOKEN_ERROR -> ERROR_LOGOUT
        WITHDRAWAL_USER -> ERROR_ACCOUNT_SUSPENDED
        HTTP_CARD_ALREADY_DELETE -> ERROR_ALREADY_CARD_DELETE
        HTTP_NO_MORE_CONTENT -> ERROR_NO_DATA
        HTTP_GLOBAL_MESSAGE -> ERROR_NETWORK
        APP_ERROR_CODE -> ERROR_FAIL_JOB
        else -> ERROR_NETWORK
    }
    return SooumException(code = code, message = resolvedMessage, cause = cause)
}

fun Throwable.asSooumException(defaultCode: Int = APP_ERROR_CODE): SooumException {
    return when (this) {
        is SooumException -> this
        is ServerException -> sooumExceptionOf(code = code, message = message, cause = this)
        else -> sooumExceptionOf(code = defaultCode, message = message ?: ERROR_NETWORK, cause = this)
    }
}

fun Throwable.sooumCode(): Int = asSooumException().code

fun Throwable.sooumMessage(default: String = ERROR_NETWORK): String {
    return asSooumException().message.ifBlank { default }
}

fun Throwable.isLogoutException(): Boolean {
    return asSooumException().code in setOf(HTTP_INVALID_TOKEN, HTTP_TOKEN_ERROR)
}
