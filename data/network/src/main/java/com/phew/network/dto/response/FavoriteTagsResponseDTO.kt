package com.phew.network.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class FavoriteTagsResponseDTO(
    val favoriteTags: List<FavoriteTagItemDTO>
)

@Serializable
data class FavoriteTagItemDTO(
    val id: Long,
    val name: String
)