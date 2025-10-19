package com.phew.presentation.write.model

import com.phew.core_design.R

data class BackgroundFilter(
    val name: String,
    val images: List<Int>
)

object BackgroundConfig {
    val filters = listOf(
        BackgroundFilter(
            name = "컬러",
            images = listOf(
                R.drawable.bg_color_blue, R.drawable.bg_color_green, R.drawable.bg_color_yellow,
                R.drawable.bg_color_orange, R.drawable.bg_color_red, R.drawable.bg_color_purple, R.drawable.bg_color_pink
            )
        ),
        BackgroundFilter(
            name = "자연",
            images = listOf(
                R.drawable.bg_netural_leaf, R.drawable.bg_netural_sea, R.drawable.bg_netural_sand,
                R.drawable.bg_netural_cloud, R.drawable.bg_netural_snow, R.drawable.bg_netural_flower, R.drawable.bg_netural_moon
            )
        ),
        BackgroundFilter(
            name = "감성",
            images = listOf(
                R.drawable.bg_emotion_bed, R.drawable.bg_emotion_shadow, R.drawable.bg_emotion_airplane,
                R.drawable.bg_emotion_cat, R.drawable.bg_emotion_window, R.drawable.bg_emotion_light, R.drawable.bg_emotion_book
            )
        ),
        BackgroundFilter(
            name = "푸드",
            images = listOf(
                R.drawable.bg_food_coffee, R.drawable.bg_food_icecream, R.drawable.bg_food_cake,
                R.drawable.bg_food_lemon, R.drawable.bg_food_candy, R.drawable.bg_food_cupcake, R.drawable.bg_food_beer
            )
        ),
        BackgroundFilter(
            name = "추상",
            images = listOf(
                R.drawable.bg_abstract_1, R.drawable.bg_abstract_2, R.drawable.bg_abstract_3,
                R.drawable.bg_abstract_4, R.drawable.bg_abstract_5, R.drawable.bg_abstract_6, R.drawable.bg_abstract_7
            )
        ),
        BackgroundFilter(
            name = "메모",
            images = listOf(
                R.drawable.bg_memo_1, R.drawable.bg_memo_2, R.drawable.bg_memo_3,
                R.drawable.bg_memo_4, R.drawable.bg_memo_5, R.drawable.bg_memo_6, R.drawable.bg_memo_7
            )
        )
    )

    val imagesByFilter: Map<String, List<Int>> = filters.associate { it.name to it.images }
    
    val filterNames: List<String> = filters.map { it.name }
}