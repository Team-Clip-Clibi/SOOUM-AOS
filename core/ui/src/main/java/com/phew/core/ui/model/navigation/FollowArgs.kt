package com.phew.core.ui.model.navigation

import kotlinx.serialization.Serializable

@Serializable
data class FollowArgs(
    val isMyProfile: Boolean,
    val selectTab: Int,
) : java.io.Serializable