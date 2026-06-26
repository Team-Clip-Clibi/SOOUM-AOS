package com.phew.presentation.write.viewmodel

sealed interface WriteUiEffect {
    data class RequestPermission(val permissions: Array<String>) : WriteUiEffect
    data class NavigateToWrittenCard(val cardId: Long) : WriteUiEffect
    data class ShowError(val refreshToken: String) : WriteUiEffect
    data object ShowRestricted : WriteUiEffect
    data object ShowDeleted : WriteUiEffect
    data object ShowBadImage : WriteUiEffect
}
