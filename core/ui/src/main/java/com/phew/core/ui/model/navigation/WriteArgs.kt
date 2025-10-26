package com.phew.core.ui.model.navigation

import kotlinx.serialization.Serializable

@Serializable
data class WriteArgs(
    val parentCardId: Long? = null
): java.io.Serializable