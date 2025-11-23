package com.phew.domain.dto

data class FavoriteTag(
    val id: Long,
    val name: String
)

data class FavoriteTagList(
    val favoriteTags: List<FavoriteTag>
)