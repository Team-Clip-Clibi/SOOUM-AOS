package com.phew.network.dto

import kotlinx.serialization.Serializable

@OptIn(kotlinx.serialization.InternalSerializationApi::class)
@Serializable
data class PolicyDTO(
    val agreedToTermsOfService: Boolean,
    val agreedToLocationTerms: Boolean,
    val agreedToPrivacyPolicy: Boolean
)