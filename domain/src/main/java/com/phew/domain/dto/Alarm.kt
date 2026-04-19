package com.phew.domain.dto

data class Alarm(
    val commentCardNotify: Boolean,
    val cardLikeNotify: Boolean,
    val followUserCardNotify: Boolean,
    val newFollowerNotify: Boolean,
    val cardNewCommentNotify: Boolean,
    val recommendedContentNotify: Boolean,
    val favoriteTagNotify: Boolean,
    val serviceUpdateNotify: Boolean,
    val policyViolationNotify: Boolean
)