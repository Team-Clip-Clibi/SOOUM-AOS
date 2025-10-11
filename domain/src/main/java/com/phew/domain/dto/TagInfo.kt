package com.phew.domain.dto

data class TagInfo(
    val id: String,
    val name: String,
    val usageCnt: Int
) {
    val tag = usageCnt.toViewFormat()

    private fun Int.toViewFormat(): String {
        return when {
            this < 100 -> this.toString()
            this < 500 -> "100+"
            this < 1000 -> "500+"
            this < 5000 -> "1000+"
            this < 10000 -> "5000+"
            else -> {
                val million = this / 10000
                val thousand = (this % 10000) / 1000
                if (thousand == 0) {
                    "$million"
                } else {
                    "${million}.${thousand}"
                }
            }
        }
    }
}