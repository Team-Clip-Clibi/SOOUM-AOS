package com.phew.network.dto

import kotlinx.serialization.Serializable

@Serializable
enum class AppVersionStatus {
    UPDATE,    // 업데이트 필요
    PENDING,   // 대기 중
    OK         // 정상
}