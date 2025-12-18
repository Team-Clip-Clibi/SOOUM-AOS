package com.phew.repository.event

import com.phew.analytics.AppEventLog
import com.phew.core_common.CardDetailTrace
import com.phew.core_common.Detail.LOG_DETAIL_CARD_TAG_CLICK
import com.phew.core_common.Detail.LOG_DETAIL_WRITE_COMMENT_CARD_BUTTON_ALL
import com.phew.core_common.Detail.LOG_DETAIL_WRITE_COMMENT_CARD_BUTTON_FLOAT
import com.phew.core_common.Detail.LOG_DETAIL_WRITE_COMMENT_CARD_BUTTON_IMAGE
import com.phew.core_common.Detail.LOG_DETAIL_WRITE_COMMENT_WHEN_BACKGROUND_EVENT_CARD
import com.phew.core_common.EventCommon.LOG_TRACE_CARD_DETAIL_VIEW
import com.phew.core_common.Feed.LOG_FEED_BOTTOM_ADD_CARD_CLICK
import com.phew.core_common.Feed.LOG_FEED_CLICK_CARD_DETAIL
import com.phew.core_common.Feed.LOG_FEED_CLICK_EVENT_CARD
import com.phew.core_common.Feed.LOG_FEED_MOVE_TOP_HOME
import com.phew.core_common.SignUpSetting.LOG_ACCOUNT_TRANSFER_SUCCESS
import com.phew.core_common.Tag.LOG_TAG_POPULAR_TAG_CLICK
import com.phew.core_common.Tag.LOG_TAG_REGISTER_TAG
import com.phew.core_common.Tag.LOG_TAG_SEARCH_VIEW_CLICK
import com.phew.core_common.Write.LOG_WRITE_BACK_BUTTON_WRITE_FEED_CARD
import com.phew.core_common.Write.LOG_WRITE_CARD_BACKGROUND_CHANGE
import com.phew.core_common.Write.LOG_WRITE_CARD_DISTANCE_OFF
import com.phew.core_common.Write.LOG_WRITE_CARD_ENTER
import com.phew.core_common.Write.LOG_WRITE_CARD_FINISH
import com.phew.core_common.Write.LOG_WRITE_COMMENT_CARD_BACKGROUND_CHANGE
import com.phew.core_common.Write.LOG_WRITE_COMMENT_CARD_BACK_HANDLER
import com.phew.domain.repository.event.EventRepository
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(private val appEventLog: AppEventLog) :
    EventRepository {
    //버튼을 이용해 Feed view Top 으로 이동
    override suspend fun logFeedMoveToTop() {
        appEventLog.logEvent(LOG_FEED_MOVE_TOP_HOME)
    }

    //Feed view -> card detail view 이동
    override suspend fun logFeedMoveToDetail() {
        appEventLog.logEvent(LOG_FEED_CLICK_CARD_DETAIL)
    }

    //Feed view -> card detail view 이동 when event background 일때만
    override suspend fun logFeedClickEventCard() {
        appEventLog.logEvent(LOG_FEED_CLICK_EVENT_CARD)
    }

    //bottom navigation 을 통해 카드 작성 view 로 이동
    override suspend fun logWriteBottomAddCard() {
        appEventLog.logEvent(LOG_FEED_BOTTOM_ADD_CARD_CLICK)
    }

    //태그 작성 후 Enter 선택 시
    override suspend fun logWriteTagWriteFinishWithEnter() {
        appEventLog.logEvent(LOG_WRITE_CARD_ENTER)
    }

    //카드 작성 완료 버튼
    override suspend fun logWriteCardClickFinishButton() {
        appEventLog.logEvent(LOG_WRITE_CARD_FINISH)
    }

    //카드 작성 중 배경 이미지 Category 변경 기록
    override suspend fun logWriteCountBackgroundChange() {
        appEventLog.logEvent(LOG_WRITE_CARD_BACKGROUND_CHANGE)
    }

    //거리 공유 안하고 작성 완료 기록
    override suspend fun logWriteDistanceSharedOff() {
        appEventLog.logEvent(LOG_WRITE_CARD_DISTANCE_OFF)
    }

    //피드 카드 작성 중 뒤로 가기 선택
    override suspend fun logWriteBackToFeedCard() {
        appEventLog.logEvent(LOG_WRITE_BACK_BUTTON_WRITE_FEED_CARD)
    }

    //댓글 카드 작성 시 배경 이미지 Category 변경 기록
    override suspend fun logWriteCommentCardBackgroundChange() {
        appEventLog.logEvent(LOG_WRITE_COMMENT_CARD_BACKGROUND_CHANGE)
    }

    //댓글 카드 작성 중 뒤로 가기 기록
    override suspend fun logWriteBackCommentCard() {
        appEventLog.logEvent(LOG_WRITE_COMMENT_CARD_BACK_HANDLER)
    }

    //댓글 카드 작성 화면 이동 기록
    override suspend fun logDetailWriteCommentCard() {
        appEventLog.logEvent(LOG_DETAIL_WRITE_COMMENT_CARD_BUTTON_ALL)
    }

    //댓글 카드 작성 화면 이동(댓글 이미지 선택) 기록
    override suspend fun logDetailWriteCommentCardImage() {
        appEventLog.logEvent(LOG_DETAIL_WRITE_COMMENT_CARD_BUTTON_IMAGE)
    }

    //댓글 카드 작성 화면 이동(Float button 선택) 기록
    override suspend fun logDetailWriteCommentCardFloatButton() {
        appEventLog.logEvent(LOG_DETAIL_WRITE_COMMENT_CARD_BUTTON_FLOAT)
    }

    //카드 상세 보기의 테그 클릭 기록
    override suspend fun logDetailTagClick() {
        appEventLog.logEvent(LOG_DETAIL_CARD_TAG_CLICK)
    }

    //이벤트 카드 일때 플로팅 버튼 클릭 기록
    override suspend fun logDetailWriteCardWhenBackgroundEventCard() {
        appEventLog.logEvent(LOG_DETAIL_WRITE_COMMENT_WHEN_BACKGROUND_EVENT_CARD)
    }

    //계정 이관 코드 입력 성공 기록
    override suspend fun logSuccessTransfer() {
        appEventLog.logEvent(LOG_ACCOUNT_TRANSFER_SUCCESS)
    }

    //테그 즐겨 찾기 선택 기록
    override suspend fun logTagRegisterTag() {
        appEventLog.logEvent(LOG_TAG_REGISTER_TAG)
    }

    //테그 검색 view 선택 기록
    override suspend fun logTagClickSearchView() {
        appEventLog.logEvent(LOG_TAG_SEARCH_VIEW_CLICK)
    }

    //인기 테그 선택 기록
    override suspend fun logTagClickPopularTag() {
        appEventLog.logEvent(LOG_TAG_POPULAR_TAG_CLICK)
    }
    // 카드 상세조회 이전 화면 추적
    override suspend fun traceWhereComeFromCardDetail(view: String) {
        appEventLog.logEvent(LOG_TRACE_CARD_DETAIL_VIEW ,mapOf(CardDetailTrace.KEY.value to view) )
    }
}