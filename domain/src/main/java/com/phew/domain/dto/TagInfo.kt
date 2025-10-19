package com.phew.domain.dto

data class TagCountDisplay(
    val value: String,
    val unit: String
) {
    val fullText: String get() = "$value$unit"
}

data class TagInfo(
    val id: String,
    val name: String,
    val usageCnt: Int
) {
    val tag = usageCnt.toViewFormat().fullText
    val countDisplay = usageCnt.toViewFormat()

    private fun Int.toViewFormat(): TagCountDisplay {
        return when {
            this < 1000 -> TagCountDisplay(this.toString(), "")
            this < 10000 -> {
                val thousands = this / 1000.0
                TagCountDisplay(String.format("%.1f", thousands), "천")
            }
            this < 100000 -> {
                val tenThousands = this / 10000.0
                TagCountDisplay(String.format("%.1f", tenThousands), "만")
            }
            this < 1000000 -> {
                TagCountDisplay((this / 10000).toString(), "만")
            }
            this < 10000000 -> {
                TagCountDisplay((this / 10000).toString(), "만+")
            }
            else -> {
                val tenThousands = this / 10000
                if (tenThousands >= 1000) {
                    TagCountDisplay((tenThousands / 1000).toString(), "천만+")
                } else {
                    TagCountDisplay(tenThousands.toString(), "만+")
                }
            }
        }
    }
}