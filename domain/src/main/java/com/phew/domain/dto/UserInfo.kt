package com.phew.domain.dto

data class UserInfo(
    val nickName: String,
    val isNotifyAgree: Boolean,
    val agreedToTermsOfService: Boolean,
    val agreedToLocationTerms: Boolean,
    val agreedToPrivacyPolicy: Boolean,
)