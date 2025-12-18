package com.phew.core.ui.model.navigation

import com.phew.core_common.CardDetailTrace
import kotlinx.serialization.Serializable


@Serializable
data class CardDetailArgs(
    val cardId: Long,
    val previousView: CardDetailTrace = CardDetailTrace.FEED
) : java.io.Serializable
