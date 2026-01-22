package com.phew.network.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class NotifyToggleRequestDTO(
    val isAllowNotify: Boolean
)