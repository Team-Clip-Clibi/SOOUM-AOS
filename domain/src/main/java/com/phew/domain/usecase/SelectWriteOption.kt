package com.phew.domain.usecase

import javax.inject.Inject

class SelectWriteOption @Inject constructor() {
    operator fun invoke(param: Param): Result {
        if (param.optionId == param.pollOptionId) {
            if (param.hasPoll) {
                return Result(
                    selectedOptionIds = param.selectedOptionIds,
                    shouldOpenPollCreate = false,
                    shouldConfirmPollReplacement = true,
                    notice = null
                )
            }

            val shouldReleaseStory = param.selectedOptionIds.contains(param.storyOptionId)
            return Result(
                selectedOptionIds = if (shouldReleaseStory) {
                    param.selectedOptionIds.filter { it != param.storyOptionId }
                } else {
                    param.selectedOptionIds
                },
                shouldOpenPollCreate = true,
                shouldConfirmPollReplacement = false,
                notice = if (shouldReleaseStory) Notice.PollReleasedStory else null
            )
        }

        if (param.optionId == param.storyOptionId && param.hasPoll) {
            return Result(
                selectedOptionIds = param.selectedOptionIds.filter { it != param.storyOptionId },
                shouldOpenPollCreate = false,
                shouldConfirmPollReplacement = false,
                notice = Notice.PollBlocksStory
            )
        }

        if (param.optionId == param.distanceOptionId && !param.hasDistancePermission) {
            return Result(
                selectedOptionIds = param.selectedOptionIds,
                shouldOpenPollCreate = false,
                shouldConfirmPollReplacement = false,
                notice = null
            )
        }

        val selectedOptionIds = if (param.selectedOptionIds.contains(param.optionId)) {
            param.selectedOptionIds.filter { it != param.optionId }
        } else {
            param.selectedOptionIds + param.optionId
        }

        return Result(
            selectedOptionIds = selectedOptionIds,
            shouldOpenPollCreate = false,
            shouldConfirmPollReplacement = false,
            notice = null
        )
    }

    data class Param(
        val optionId: String,
        val selectedOptionIds: List<String>,
        val hasPoll: Boolean,
        val hasDistancePermission: Boolean,
        val distanceOptionId: String,
        val storyOptionId: String,
        val pollOptionId: String,
    )

    data class Result(
        val selectedOptionIds: List<String>,
        val shouldOpenPollCreate: Boolean,
        val shouldConfirmPollReplacement: Boolean,
        val notice: Notice?,
    )

    enum class Notice {
        PollReleasedStory,
        PollBlocksStory,
    }
}
