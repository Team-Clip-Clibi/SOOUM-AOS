package com.phew.presentation.write.viewmodel

sealed class WriteUiEffect {
    data class ShowError(val refreshToken: String) : WriteUiEffect()
    object ShowRestricted : WriteUiEffect()
    object ShowDeleted : WriteUiEffect()
    object ShowBadImage : WriteUiEffect()
}
