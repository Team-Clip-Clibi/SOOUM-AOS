package com.phew.network

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GlobalMessageParserTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun parseGlobalMessage_returnsMessage() {
        val message = parseGlobalMessage(
            json = json,
            responseBody = """{"code":526,"message":"서비스 점검 중입니다."}""",
        )

        assertEquals("서비스 점검 중입니다.", message)
    }

    @Test
    fun parseGlobalMessage_returnsNullWhenMessageIsBlank() {
        val message = parseGlobalMessage(
            json = json,
            responseBody = """{"code":526,"message":""}""",
        )

        assertNull(message)
    }
}
