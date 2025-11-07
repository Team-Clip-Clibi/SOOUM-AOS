package com.phew.core.ui.model.navigation

import kotlinx.serialization.Serializable

@Serializable
data class ProfileArgs(
    val userId: Long,
) : java.io.Serializable