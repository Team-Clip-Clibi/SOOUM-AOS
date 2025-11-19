package com.phew.domain.model

import java.util.Locale

data class TagInfo(
    val id: String,
    val name: String,
    val usageCnt: Int
){
    val useCount = usageCnt.toUserCount()
    
    private fun Int.toUserCount() : String {
       return when {
           this < 1000 -> this.toString()
           this <= 1099 -> "1000+"
           this < 10000 -> {
               val thousands = this / 1000.0
               String.format(Locale.getDefault(), "%.1f천", thousands)
           }
           else -> {
               val tensOfThousands = this / 10000.0
               String.format(Locale.getDefault(), "%.1f만", tensOfThousands)
           }
       }
    }
}

data class TagInfoList(
    val tagInfos: List<TagInfo>
)