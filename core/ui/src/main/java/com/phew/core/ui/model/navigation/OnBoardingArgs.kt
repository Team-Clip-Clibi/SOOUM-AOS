package com.phew.core.ui.model.navigation

import kotlinx.serialization.Serializable

@Serializable
data class OnBoardingArgs(
    val showWithdrawalDialog: Boolean = false
): java.io.Serializable