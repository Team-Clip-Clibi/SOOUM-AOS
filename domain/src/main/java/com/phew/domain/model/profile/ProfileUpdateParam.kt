package com.phew.domain.model.profile

/**
 * ProfileUseCaseOrchestrator에서 프로필 수정 요청을 구성할 때 사용하는 입력 모델입니다.
 */
data class ProfileUpdateParam(
    val nickName: String?,
    val imgName: String?,
    val profileBio: String?,
    val profileImage: String?,
    val isImageChange: Boolean,
)
