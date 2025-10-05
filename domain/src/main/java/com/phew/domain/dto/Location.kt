package com.phew.domain.dto

data class Location(
    val latitude: Double? = null,
    val longitude: Double? = null,
) {
    companion object {
        val EMPTY = Location(latitude = null, longitude = null)
    }
}