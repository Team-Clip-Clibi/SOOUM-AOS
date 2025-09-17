package com.phew.core_common

sealed class DataResult<out T> {
    data class Success<out T>(val data: T) : DataResult<T>()
    data class Fail(
        val code: Int = 0,
        val message: String? = null,
        val throwable: Throwable? = null,
    ) : DataResult<Nothing>()
}