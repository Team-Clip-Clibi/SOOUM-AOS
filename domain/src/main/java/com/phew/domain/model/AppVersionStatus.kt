package com.phew.domain.model

data class AppVersionStatus(
    val status: AppVersionStatusType,
    val latestVersion: String
)

enum class AppVersionStatusType {
    UPDATE,    // 업데이트 필요
    PENDING,   // 대기 중
    OK;         // 정상

    companion object {
        fun from(value: String?): AppVersionStatusType {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: OK
        }
    }
}