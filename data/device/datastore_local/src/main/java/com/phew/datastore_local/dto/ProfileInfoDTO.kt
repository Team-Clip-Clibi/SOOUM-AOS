package com.phew.datastore_local.dto

data class ProfileInfoDTO(
    val nickName: String,
    val profileImageUrl: String,
    val profileImageName: String,
) {
    override fun equals(other: Any?): Boolean {
        if (other !is ProfileInfoDTO) return false
        if (other.nickName != nickName) return false
        if (other.profileImageUrl != profileImageUrl) return false
        if (other.profileImageName != profileImageName) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}