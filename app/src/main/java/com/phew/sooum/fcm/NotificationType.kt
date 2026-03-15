package com.phew.sooum.fcm

enum class NotificationType(val value: String) {
    FEED_LIKE("FEED_LIKE"),
    COMMENT_LIKE("COMMENT_LIKE"),
    COMMENT_WRITE("COMMENT_WRITE"),
    FOLLOW("FOLLOW"),
    TAG_USAGE("TAG_USAGE"),
    TRANSFER_SUCCESS("TRANSFER_SUCCESS"),
    VIEW_FEED_COMMENT_WRITE("VIEWED_FEED_COMMENT_WRITE"),
    FOLLOW_CARD_UPLOAD("FOLLOWER_CARD_UPLOAD"),
    ARTICLE_CARD_UPLOAD("ARTICLE_CARD_UPLOAD");


    companion object {
        fun fromString(value: String?): NotificationType? {
            return entries.find { it.value == value }
        }
    }
}