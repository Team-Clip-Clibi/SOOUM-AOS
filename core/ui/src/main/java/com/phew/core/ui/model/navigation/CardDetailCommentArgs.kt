package com.phew.core.ui.model.navigation

import com.phew.core_common.CardDetailTrace
import kotlinx.serialization.Serializable

/**
 *  TODO 요기 어덯게 할지 논의 필요 to.성일님
 */
@Serializable
data class CardDetailCommentArgs(
    val cardId: Long,
    val parentId: Long,
    val backTo: String? = null,
    val isComment: Boolean = true,
    val previousView : CardDetailTrace
): java.io.Serializable
