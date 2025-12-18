package com.phew.domain.repository.event

interface EventRepository {
    //feedViewEvent
    suspend fun logFeedMoveToTop()
    suspend fun logFeedBottomAddCard()
    suspend fun logFeedMoveToDetail()
    suspend fun logFeedClickEventCard()

    //writeEvent
    suspend fun logWriteTagWriteFinishWithEnter()
    suspend fun logWriteCardClickFinishButton()
    suspend fun logWriteCountBackgroundChange()
    suspend fun logWriteDistanceSharedOff()
    suspend fun logWriteCommentCardBackgroundChange()
    suspend fun logWriteBackToFeedCard()
    suspend fun logWriteBackCommentCard()

    //Detail
    suspend fun logDetailWriteCommentCard()
    suspend fun logDetailWriteCommentCardImage()
    suspend fun logDetailWriteCommentCardFloatButton()
    suspend fun logDetailTagClick()
    suspend fun logDetailWriteCardWhenBackgroundEventCard()

    //SignUpSetting
    suspend fun logSuccessTransfer()

    //tag
    suspend fun logTagRegisterTag()
    suspend fun logTagClickSearchView()
    suspend fun logTagClickPopularTag()

    //Common
    suspend fun traceWhereComeFromCardDetail(view: String)
}