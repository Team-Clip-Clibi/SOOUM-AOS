package com.phew.core.ui.model.navigation

import kotlinx.serialization.Serializable

@Serializable
data class TagViewArgs(
    val tagName: String,
    val tagId: Long
): java.io.Serializable