package com.phew.core_common

import com.phew.core_common.exception.SooumException
import com.phew.core_common.exception.asSooumException
import com.phew.core_common.exception.sooumExceptionOf
import com.phew.core_common.exception.sooumMessage

fun <T> resultFailure(
    code: Int = APP_ERROR_CODE,
    message: String? = null,
    throwable: Throwable? = null,
): Result<T> = Result.failure(sooumExceptionOf(code = code, message = message, cause = throwable))

fun <T> resultFailure(
    message: String,
    code: Int = APP_ERROR_CODE,
    throwable: Throwable? = null,
): Result<T> = Result.failure(sooumExceptionOf(code = code, message = message, cause = throwable))

fun <T> resultFailure(
    error: Unit,
    code: Int = APP_ERROR_CODE,
    throwable: Throwable? = null,
): Result<T> = Result.failure(sooumExceptionOf(code = code, message = ERROR_FAIL_JOB, cause = throwable))

fun <T> Result<T>.mapSooumFailure(): Result<T> {
    return fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(it.asSooumException()) },
    )
}

fun <T> Result<T>.errorMessage(default: String = ERROR_NETWORK): String {
    return exceptionOrNull()?.sooumMessage(default) ?: default
}

fun <T> Result<T>.errorCode(default: Int = APP_ERROR_CODE): Int {
    return (exceptionOrNull()?.asSooumException() as? SooumException)?.code ?: default
}

inline fun <T, R> Result<T>.mapSuccess(transform: (T) -> R): Result<R> {
    return fold(
        onSuccess = { Result.success(transform(it)) },
        onFailure = { Result.failure(it) },
    )
}

inline fun <T> Result<T>.mapFailureMessage(
    transform: (code: Int, message: String) -> String,
): Result<T> {
    return fold(
        onSuccess = { Result.success(it) },
        onFailure = { throwable ->
            val exception = throwable.asSooumException()
            resultFailure(
                code = exception.code,
                message = transform(exception.code, exception.message),
                throwable = throwable,
            )
        },
    )
}

inline fun <T, R> Result<T>.mapResult(
    success: (T) -> R,
    failure: (code: Int, message: String) -> String,
): Result<R> {
    return fold(
        onSuccess = { Result.success(success(it)) },
        onFailure = { throwable ->
            val exception = throwable.asSooumException()
            resultFailure(
                code = exception.code,
                message = failure(exception.code, exception.message),
                throwable = throwable,
            )
        },
    )
}
