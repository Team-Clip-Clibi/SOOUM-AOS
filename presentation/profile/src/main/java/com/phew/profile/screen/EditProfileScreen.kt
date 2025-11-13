package com.phew.profile.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phew.core_design.AppBar
import com.phew.core_design.AvatarComponent
import com.phew.core_design.DialogComponent.SnackBar
import com.phew.core_design.LargeButton
import com.phew.core_design.LoadingAnimation
import com.phew.core_design.NeutralColor
import com.phew.core_design.TextFiledComponent
import com.phew.domain.dto.ProfileInfo
import com.phew.profile.ProfileViewModel
import com.phew.profile.R
import com.phew.profile.UiState

@Composable
internal fun EditProfileScreen(viewModel: ProfileViewModel, onBackPress: () -> Unit) {
    val snackBarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            AppBar.IconLeftAppBar(
                onClick = remember(onBackPress) { { onBackPress() } },
                appBarText = stringResource(R.string.edit_profile_top_bar)
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackBarHostState,
                snackbar = { snackBarData ->
                    SnackBar(data = snackBarData)
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(color = NeutralColor.WHITE)
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                LargeButton.NoIconPrimary(
                    isEnable = uiState.changeNickName.isNotEmpty(),
                    buttonText = stringResource(com.phew.core_design.R.string.btn_save),
                    onClick = {}
                )
            }
        }) { paddingValues ->
        when (val result = uiState.profileInfo) {
            is UiState.Fail -> {
                ErrorView(result.errorMessage)
            }

            UiState.Loading -> LoadingAnimation.LoadingView()
            is UiState.Success -> {
                ChangeProfileView(
                    paddingValues = paddingValues,
                    profile = result.data,
                    onAvatarClick = {},
                    onValueChange = {},
                    hint = "",
                    showError = true
                )
            }
        }
    }
}

@Composable
private fun ChangeProfileView(
    paddingValues: PaddingValues,
    profile: ProfileInfo,
    onAvatarClick: () -> Unit,
    onValueChange: (String) -> Unit,
    hint: String,
    showError: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = NeutralColor.WHITE)
            .padding(
                top = paddingValues.calculateTopPadding() + 24.dp,
                bottom = paddingValues.calculateBottomPadding() + 24.dp,
                start = 16.dp,
                end = 16.dp
            )
            .verticalScroll(rememberScrollState())
    ) {
        AvatarComponent.LargeAvatar(
            url = profile.profileImageUrl.toUri(),
            onClick = onAvatarClick
        )
        Spacer(modifier = Modifier.height(40.dp))
        TextFiledComponent.RightIcon(
            rightImageClick = {
                onValueChange("")
            },
            value = profile.nickname,
            onValueChange = { input ->
                onValueChange(input)
            },
            placeHolder = "",
            helperUse = true,
            helperText = hint,
            showError = showError
        )
    }
}

@Composable
private fun ErrorView(errorMessage: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = NeutralColor.WHITE)
    ) {
        Image(
            painter = painterResource(com.phew.core_design.R.drawable.ic_deleted_card),
            contentDescription = errorMessage,
            modifier = Modifier
                .height(130.dp)
                .width(220.dp)
                .align(Alignment.Center)
        )
    }
}