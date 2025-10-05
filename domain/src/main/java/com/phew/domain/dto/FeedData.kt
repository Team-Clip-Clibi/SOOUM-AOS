package com.phew.domain.dto

import android.net.Uri

data class FeedData(
    val location : String,
    val writeTime : String,
    val commentValue : String,
    val likeValue : String,
    val uri : Uri,
    val content : String
)