package com.phew.core_common.exception

class ServerException(val code: Int, message: String?) : Exception(message)
