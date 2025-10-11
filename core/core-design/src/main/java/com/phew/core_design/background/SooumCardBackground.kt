package com.phew.core_design.background

import androidx.annotation.DrawableRes
import com.phew.core_design.R

enum class SooumCardBackground(
    val value: String,
    @DrawableRes val resId: Int
) {
    // Abstract 배경
    Abstract1("abstract1", R.drawable.bg_abstract_1),
    Abstract2("abstract2", R.drawable.bg_abstract_2),
    Abstract3("abstract3", R.drawable.bg_abstract_3),
    Abstract4("abstract4", R.drawable.bg_abstract_4),
    Abstract5("abstract5", R.drawable.bg_abstract_5),
    Abstract6("abstract6", R.drawable.bg_abstract_6),
    Abstract7("abstract7", R.drawable.bg_abstract_7),

    // Color 배경
    ColorRed("colorRed", R.drawable.bg_color_red),
    ColorYellow("colorYellow", R.drawable.bg_color_yellow),
    ColorOrange("colorOrange", R.drawable.bg_color_orange),
    ColorGreen("colorGreen", R.drawable.bg_color_green),
    ColorBlue("colorBlue", R.drawable.bg_color_blue),
    ColorPink("colorPink", R.drawable.bg_color_pink),
    ColorPurple("colorPurple", R.drawable.bg_color_purple),

    // Emotion 배경
    EmotionCat("emotionCat", R.drawable.bg_emotion_cat),
    EmotionAirplane("emotionAirplane", R.drawable.bg_emotion_airplane),
    EmotionBook("emotionBook", R.drawable.bg_emotion_book),
    EmotionWindow("emotionWindow", R.drawable.bg_emotion_window),
    EmotionLight("emotionLight", R.drawable.bg_emotion_light),
    EmotionShadow("emotionShadow", R.drawable.bg_emotion_shadow),
    EmotionBed("emotionBed", R.drawable.bg_emotion_bed),

    // Food 배경
    FoodLemon("foodLemon", R.drawable.bg_food_lemon),
    FoodIcecream("foodIcecream", R.drawable.bg_food_icecream),
    FoodCoffee("foodCoffee", R.drawable.bg_food_coffee),
    FoodBeer("foodBeer", R.drawable.bg_food_beer),
    FoodCupcake("foodCupcake", R.drawable.bg_food_cupcake),
    FoodCake("foodCake", R.drawable.bg_food_cake),
    FoodCandy("foodCandy", R.drawable.bg_food_candy),

    // Memo 배경
    Memo1("memo1", R.drawable.bg_memo_1),
    Memo2("memo2", R.drawable.bg_memo_2),
    Memo3("memo3", R.drawable.bg_memo_3),
    Memo4("memo4", R.drawable.bg_memo_4),
    Memo5("memo5", R.drawable.bg_memo_5),
    Memo6("memo6", R.drawable.bg_memo_6),
    Memo7("memo7", R.drawable.bg_memo_7),

    // Neutral 배경
    NeutralSnow("neutralSnow", R.drawable.bg_netural_snow),
    NeutralSea("neutralSea", R.drawable.bg_netural_sea),
    NeutralSand("neutralSand", R.drawable.bg_netural_sand),
    NeutralLeaf("neutralLeaf", R.drawable.bg_netural_leaf),
    NeutralCloud("neutralCloud", R.drawable.bg_netural_cloud),
    NeutralMoon("neutralMoon", R.drawable.bg_netural_moon),
    NeutralFlower("neutralFlower", R.drawable.bg_netural_flower);

    companion object {
        /**
         * 카테고리별 배경 그룹 반환
         */
        fun getAbstractBackgrounds() = listOf(
            Abstract1, Abstract2, Abstract3, Abstract4, 
            Abstract5, Abstract6, Abstract7
        )

        fun getColorBackgrounds() = listOf(
            ColorRed, ColorYellow, ColorOrange, ColorGreen,
            ColorBlue, ColorPink, ColorPurple
        )

        fun getEmotionBackgrounds() = listOf(
            EmotionCat, EmotionAirplane, EmotionBook, EmotionWindow,
            EmotionLight, EmotionShadow, EmotionBed
        )

        fun getFoodBackgrounds() = listOf(
            FoodLemon, FoodIcecream, FoodCoffee, FoodBeer,
            FoodCupcake, FoodCake, FoodCandy
        )

        fun getMemoBackgrounds() = listOf(
            Memo1, Memo2, Memo3, Memo4, Memo5, Memo6, Memo7
        )

        fun getNeutralBackgrounds() = listOf(
            NeutralSnow, NeutralSea, NeutralSand, NeutralLeaf,
            NeutralCloud, NeutralMoon, NeutralFlower
        )

        /**
         * 모든 배경 반환
         */
        fun getAllBackgrounds() = values().toList()

        /**
         * value로 배경 찾기
         */
        fun findByValue(value: String): SooumCardBackground? {
            return values().find { it.value == value }
        }
    }
}