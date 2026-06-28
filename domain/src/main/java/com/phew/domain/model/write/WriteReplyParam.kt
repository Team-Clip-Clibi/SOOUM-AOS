package com.phew.domain.model.write

/**
 * WriteUseCaseOrchestrator에서 댓글 카드 작성 요청을 구성할 때 사용하는 입력 모델입니다.
 */
data class WriteReplyParam(
    val cardId: Long,
    val content: String,
    val font: String,
    val imgType: String,
    val imgName: String,
    val imageUrl: String?,
    val tags: List<String>,
    val isDistanceShared: Boolean,
)
