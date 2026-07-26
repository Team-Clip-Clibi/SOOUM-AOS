package com.phew.presentation.write.model

data class WriteOption(
    val id: String,
    val displayName: String
)

object WriteOptions {
    const val DISTANCE_OPTION_ID = "distance_sharing"
    const val TWENTY_FOUR_HOURS_OPTION_ID = "twenty_four_hours"
    const val POLL_OPTION_ID = "poll"
    const val DEFAULT_OPTION_ID = DISTANCE_OPTION_ID

    val availableOptions = listOf(
        WriteOption(
            id = DISTANCE_OPTION_ID,
            displayName = "거리공유"
        ),
        WriteOption(
            id = TWENTY_FOUR_HOURS_OPTION_ID,
            displayName = "24시간"
        ),
        WriteOption(
            id = POLL_OPTION_ID,
            displayName = "투표"
        )
    )

    val defaultOption: WriteOption =
        availableOptions.first { it.id == DEFAULT_OPTION_ID }

    fun findById(id: String): WriteOption? =
        availableOptions.firstOrNull { it.id == id }
}
