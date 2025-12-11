package com.phew.presentation.write.model

import com.phew.presentation.write.R as WriteR

enum class BackgroundFilterType {
    COLOR,
    NATURE,
    EMOTION,
    FOOD,
    ABSTRACT,
    MEMO,
    EVENT;

    fun getStringRes(): Int = when (this) {
        COLOR -> WriteR.string.background_filter_color
        NATURE -> WriteR.string.background_filter_nature
        EMOTION -> WriteR.string.background_filter_emotion
        FOOD -> WriteR.string.background_filter_food
        ABSTRACT -> WriteR.string.background_filter_abstract
        MEMO -> WriteR.string.background_filter_memo
        EVENT -> WriteR.string.background_filter_event
    }

    companion object {
        fun fromServerKey(serverKey: String): BackgroundFilterType? {
            return when (serverKey) {
                "COLOR" -> COLOR
                "NATURE" -> NATURE
                "SENSITIVITY" -> EMOTION
                "FOOD" -> FOOD
                "ABSTRACT" -> ABSTRACT
                "MEMO" -> MEMO
                "EVENT" -> EVENT
                else -> null
            }
        }
    }
}

// BackgroundFilterType만 제공하는 간소화된 Config
object BackgroundConfig {
    val filterTypes: List<BackgroundFilterType> = BackgroundFilterType.entries
}