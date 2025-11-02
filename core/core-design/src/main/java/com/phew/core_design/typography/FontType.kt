package com.phew.core_design.typography

enum class FontType(val serverName: String) {
    RIDIBATANG("RIDI"),
    YOON("YOONWOO"),
    KKOKKO("KKOOKKKOOK"),
    PRETENDARD("PRETENDARD");
    
    companion object {
        fun fromServerName(serverName: String): FontType? {
            return entries.find { it.serverName == serverName }
        }
    }
}