package com.phew.core.ui.model.navigation

import kotlinx.serialization.Serializable

/**
 *  TODO 요기 어덯게 할지 논의 필요 to.성일님
 */
@Serializable
data class CardDetailArgs(
    val cardId: Long
): java.io.Serializable
