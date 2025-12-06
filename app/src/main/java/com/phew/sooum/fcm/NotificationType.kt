package com.phew.sooum.fcm

enum class NotificationType(val value: String) {
    FEED_LIKE("FEED_LIKE"),
    COMMENT_LIKE("COMMENT_LIKE"),
    COMMENT_WRITE("COMMENT_WRITE"),
    FOLLOW("FOLLOW"),
    TAG_USAGE("TAG_USAGE"),
    TRANSFER_SUCCESS("TRANSFER_SUCCESS");

    companion object {
        fun fromString(value: String?): NotificationType? {
            return values().find { it.value == value }
        }
    }
}