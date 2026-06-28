package com.phew.domain.model.write

/**
 * WriteUseCaseOrchestrator에서 새 카드 또는 답변 카드 작성 요청을 구성할 때 사용하는 입력 모델입니다.
 */
data class WriteCardParam(
    val isFromDevice: Boolean,
    val answerCard: Boolean,
    val cardId: Long?,
    val imageUrl: String?,
    val content: String,
    val font: String,
    var imgName: String?,
    val isStory: Boolean?,
    val tags: List<String>,
    val isDistanceShared: Boolean,
    val pollContents: List<String>,
)
