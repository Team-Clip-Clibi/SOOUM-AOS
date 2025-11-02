package com.phew.presentation.write.model

import com.phew.core_design.R

enum class BackgroundFilterType(val displayName: String) {
    COLOR("컬러"),
    NATURE("자연"),
    EMOTION("감성"),
    FOOD("푸드"),
    ABSTRACT("추상"),
    MEMO("메모");

    companion object {
        fun fromDisplayName(displayName: String): BackgroundFilterType? {
            return values().find { it.displayName == displayName }
        }

        fun fromServerKey(serverKey: String): BackgroundFilterType? {
            return when (serverKey) {
                "COLOR" -> COLOR
                "NATURE" -> NATURE
                "SENSITIVITY" -> EMOTION
                "FOOD" -> FOOD
                "ABSTRACT" -> ABSTRACT
                "MEMO" -> MEMO
                else -> null
            }
        }
    }
}

data class BackgroundFilter(
    val name: BackgroundFilterType,
    val images: List<Int>
)

object BackgroundConfig {
    val filters = listOf(
        BackgroundFilter(
            name = BackgroundFilterType.COLOR,
            images = listOf(
                R.drawable.bg_color_blue, R.drawable.bg_color_green, R.drawable.bg_color_yellow,
                R.drawable.bg_color_orange, R.drawable.bg_color_red, R.drawable.bg_color_purple, R.drawable.bg_color_pink
            )
        ),
        BackgroundFilter(
            name = BackgroundFilterType.NATURE,
            images = listOf(
                R.drawable.bg_netural_leaf, R.drawable.bg_netural_sea, R.drawable.bg_netural_sand,
                R.drawable.bg_netural_cloud, R.drawable.bg_netural_snow, R.drawable.bg_netural_flower, R.drawable.bg_netural_moon
            )
        ),
        BackgroundFilter(
            name = BackgroundFilterType.EMOTION,
            images = listOf(
                R.drawable.bg_emotion_bed, R.drawable.bg_emotion_shadow, R.drawable.bg_emotion_airplane,
                R.drawable.bg_emotion_cat, R.drawable.bg_emotion_window, R.drawable.bg_emotion_light, R.drawable.bg_emotion_book
            )
        ),
        BackgroundFilter(
            name = BackgroundFilterType.FOOD,
            images = listOf(
                R.drawable.bg_food_coffee, R.drawable.bg_food_icecream, R.drawable.bg_food_cake,
                R.drawable.bg_food_lemon, R.drawable.bg_food_candy, R.drawable.bg_food_cupcake, R.drawable.bg_food_beer
            )
        ),
        BackgroundFilter(
            name = BackgroundFilterType.ABSTRACT,
            images = listOf(
                R.drawable.bg_abstract_1, R.drawable.bg_abstract_2, R.drawable.bg_abstract_3,
                R.drawable.bg_abstract_4, R.drawable.bg_abstract_5, R.drawable.bg_abstract_6, R.drawable.bg_abstract_7
            )
        ),
        BackgroundFilter(
            name = BackgroundFilterType.MEMO,
            images = listOf(
                R.drawable.bg_memo_1, R.drawable.bg_memo_2, R.drawable.bg_memo_3,
                R.drawable.bg_memo_4, R.drawable.bg_memo_5, R.drawable.bg_memo_6, R.drawable.bg_memo_7
            )
        )
    )

    val imagesByFilter: Map<BackgroundFilterType, List<Int>> = filters.associate { it.name to it.images }
    
    val filterNames: List<BackgroundFilterType> = filters.map { it.name }
}