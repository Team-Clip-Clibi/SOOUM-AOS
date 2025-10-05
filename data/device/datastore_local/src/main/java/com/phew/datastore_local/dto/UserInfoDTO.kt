package com.phew.datastore_local.dto

data class UserInfoDTO(
    val nickName: String,
    val isNotifyAgree: Boolean,
    val agreedToTermsOfService: Boolean,
    val agreedToLocationTerms: Boolean,
    val agreedToPrivacyPolicy: Boolean,
)