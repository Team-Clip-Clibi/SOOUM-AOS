package com.phew.core_common

//domain error code
const val ERROR_FAIL_JOB = "Fail job"
const val ERROR_NO_DATA = "No Data"
const val ERROR_NETWORK = "error_network"
const val ERROR = "error"
const val ERROR_LOGOUT = "errorLogout"
const val ERROR_UN_GOOD_IMAGE = "unGoodImage"
const val ERROR_FAIL_PACKAGE_IMAGE = "errorPackageImage"
const val ERROR_TRANSFER_CODE_INVALID = "transfer_code_invalid"

//server api error code
const val HTTP_NO_MORE_CONTENT = 204
const val HTTP_BAD_REQUEST = 400
const val HTTP_INVALID_TOKEN = 401
const val HTTP_NOT_FOUND = 404
const val HTTP_CONFLICT = 409
const val APP_ERROR_CODE = 505
const val HTTP_UN_GOOD_IMAGE = 422
const val WITHDRAWAL_USER = 418
const val HTTP_TOKEN_ERROR = 403
const val HTTP_CARD_ALREADY_DELETE = 410

//error messages
const val ERROR_ACCOUNT_SUSPENDED = "계정이 정지되었습니다."
const val ERROR_TAG_FAVORITE_MAX_EXCEEDED = "error_tag_favorite_max_exceeded"
const val ERROR_TAG_FAVORITE_ALREADY_EXISTS = "error_tag_favorite_already_exists"
const val ERROR_ALREADY_CARD_DELETE = "error_already_card_delete"

//server api success code
const val HTTP_SUCCESS = 200

//banner category
const val BANNER_NEWS = "news"
const val BANNER_SERVICE = "service"

//bottom navigation height
const val BOTTOM_NAVIGATION_HEIGHT = 62

//Nick name length
const val INPUT_NICK_NAME = 8

//eventCard
const val EVENT_CARD = "event"

//event Log
object Feed {
    const val LOG_FEED_MOVE_TOP_HOME = "feedMoveToTop_home_btn_click"
    const val LOG_FEED_BOTTOM_ADD_CARD_CLICK = "moveToCreateFeedCardView_btn_click"
    const val LOG_FEED_CLICK_CARD_DETAIL = "feedToCardDetailView_card_click"
    const val LOG_FEED_CLICK_EVENT_CARD = "feedToCardDetailView_cardWithEventImg_click"
}

object Write {
    const val LOG_WRITE_CARD_ENTER = "multipleFeedTagCreation_enter_btn_click"
    const val LOG_WRITE_CARD_FINISH = "createFeedCard_btn_click"
    const val LOG_WRITE_CARD_BACKGROUND_CHANGE = "feedBackgroundCategory_tab_click"
    const val LOG_WRITE_CARD_DISTANCE_OFF = "createFeedCardWithoutDistanceSharedOpt_btn_click"
    const val LOG_WRITE_COMMENT_CARD_BACKGROUND_CHANGE = "commentBackgroundCategory_tab_click"
    const val LOG_WRITE_BACK_BUTTON_WRITE_FEED_CARD = "moveToCreateFeedCardView_cancel_btn_click"
    const val LOG_WRITE_COMMENT_CARD_BACK_HANDLER = "moveToCreateCommentCardView_cancel_btn_click"
}

object Detail {
    const val LOG_DETAIL_WRITE_COMMENT_CARD_BUTTON_ALL = "moveToCreateCommentCardView_btn_click"
    const val LOG_DETAIL_WRITE_COMMENT_CARD_BUTTON_IMAGE =
        "moveToCreateCommentCardView_icon_btn_click"
    const val LOG_DETAIL_WRITE_COMMENT_CARD_BUTTON_FLOAT =
        "moveToCreateCommentCardView_floating_btn_click"
    const val LOG_DETAIL_CARD_TAG_CLICK = "cardDetailTag_btn_click"
    const val LOG_DETAIL_WRITE_COMMENT_WHEN_BACKGROUND_EVENT_CARD =
        "moveToCreateCommentCardView_withEventImg_floating_btn_click"
}

object SignUpSetting {
    const val LOG_ACCOUNT_TRANSFER_SUCCESS = "accountTransferSuccess"
}

object Tag {
    const val LOG_TAG_REGISTER_TAG = "favoriteTagRegister_btn_click"
    const val LOG_TAG_SEARCH_VIEW_CLICK = "tagMenuSearchBar_click"
    const val LOG_TAG_POPULAR_TAG_CLICK = "popularTag_item_click"
}

object EventCommon {
    const val LOG_TRACE_CARD_DETAIL_VIEW = "cardDetailView_tracePath_click"
}

enum class CardDetailTrace(val value: String) {
    FEED("feed"),
    COMMENT("comment"),
    PROFILE("profile")
}

enum class MoveDetail {
    FLOAT,
    IMAGE
}

object CheckEventCard {
    fun String.isEventCard(): Boolean = this.contains(EVENT_CARD)
}