package com.phew.domain.model

data class TagInfo(
    val id: String,
    val name: String,
    val usageCnt: Int
)

data class TagInfoList(
    val tagInfos: List<TagInfo>
)