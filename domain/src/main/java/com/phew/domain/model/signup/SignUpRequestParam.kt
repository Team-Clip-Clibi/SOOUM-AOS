package com.phew.domain.model.signup

/**
 * SignUpUseCaseOrchestrator에서 신규 회원 가입 요청을 구성할 때 사용하는 입력 모델입니다.
 */
data class SignUpRequestParam(
    val nickName: String,
    val profileImage: String,
    val agreedToTermsOfService: Boolean,
    val agreedToLocationTerms: Boolean,
    val agreedToPrivacyPolicy: Boolean,
)
