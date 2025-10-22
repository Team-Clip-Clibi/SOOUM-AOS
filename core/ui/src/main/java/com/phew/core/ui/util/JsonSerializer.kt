package com.phew.core.ui.util

import kotlinx.serialization.json.Json

object JsonSerializer {
    fun createNav(): Json {
        return Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = false
        }
    }
}