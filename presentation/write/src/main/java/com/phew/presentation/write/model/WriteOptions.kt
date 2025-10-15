package com.phew.presentation.write.model

data class WriteOption(
    val id: String,
    val displayName: String
)

object WriteOptions {
    val availableOptions = listOf(
        WriteOption(
            id = "distance_sharing",
            displayName = "거리공유"
        ),
        WriteOption(
            id = "twenty_four_hours", 
            displayName = "24시간"
        )
    )
    
    val defaultOption = availableOptions.first()
}