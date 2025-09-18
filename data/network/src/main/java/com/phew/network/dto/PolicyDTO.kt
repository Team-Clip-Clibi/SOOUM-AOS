package com.phew.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class PolicyDTO(
    val agreedToTermsOfService: Boolean,
    val agreedToLocationTerms: Boolean,
    val agreedToPrivacyPolicy: Boolean
)