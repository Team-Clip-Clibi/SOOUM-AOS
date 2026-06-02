package com.phew.sign_up.view

import android.content.Context
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import com.phew.sign_up.SignUpError

internal suspend fun SnackbarHostState.showSignUpError(
    context: Context,
    error: SignUpError
) {
    val message = when (error) {
        SignUpError.Network -> context.getString(com.phew.core_design.R.string.error_network)
        SignUpError.InvalidAuthCode -> context.getString(com.phew.core_design.R.string.error_auth_code_invalid)
        SignUpError.App -> context.getString(com.phew.core_design.R.string.error_app)
    }

    showSnackbar(
        message = message,
        duration = SnackbarDuration.Short
    )
}
