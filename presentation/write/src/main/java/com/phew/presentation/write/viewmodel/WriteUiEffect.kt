package com.phew.presentation.write.viewmodel

import androidx.annotation.StringRes

sealed class WriteUiEffect {
    data class ShowError(val refreshToken: String) : WriteUiEffect()
    data class ShowSnackBar(@StringRes val messageResId: Int) : WriteUiEffect()
    object ShowPollReplacementDialog : WriteUiEffect()
    object ShowRestricted : WriteUiEffect()
    object ShowDeleted : WriteUiEffect()
    object ShowBadImage : WriteUiEffect()
}
