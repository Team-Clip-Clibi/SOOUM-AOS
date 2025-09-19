package com.phew.sign_up

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.phew.core_design.AppBar
import com.phew.core_design.DialogComponent
import com.phew.core_design.LargeButton
import com.phew.core_design.NeutralColor
import com.phew.core_design.TextComponent
import com.phew.core_design.TextFiledComponent

@Composable
fun NickNameView(viewModel: SignUpViewModel, onBack: () -> Unit, nextPage: () -> Unit) {
    BackHandler {
        onBack()
    }

    val uiState by viewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(uiState) {
        when (val result = uiState.nickNameHint) {
            is UiState.Fail -> {
                snackBarHostState.showSnackbar(
                    message = context.getString(com.phew.core_design.R.string.error_network),
                    duration = SnackbarDuration.Short
                )
            }

            is UiState.Success -> {
                viewModel.nickName(result.data)
            }

            else -> Unit
        }
        when (val result = uiState.checkNickName) {
            is UiState.Fail -> {
                snackBarHostState.showSnackbar(
                    message = context.getString(com.phew.core_design.R.string.error_network),
                    duration = SnackbarDuration.Short
                )
            }

            is UiState.Success -> {
                if (result.data) nextPage()
            }

            else -> Unit
        }
    }
    Scaffold(
        topBar = {
            AppBar.IconLeftAppBar(
                onClick = onBack,
                appBarText = stringResource(R.string.signUp_app_bar)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = NeutralColor.WHITE)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                LargeButton.NoIconPrimary(
                    buttonText = stringResource(com.phew.core_design.R.string.common_next),
                    onClick = viewModel::checkName,
                    isEnable = uiState.nickName.trim()
                        .isNotEmpty() && uiState.nickName.trim().length > 2
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState) { data ->
                DialogComponent.SnackBar(data)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = NeutralColor.WHITE)
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    start = 16.dp,
                    end = 16.dp,
                    bottom = paddingValues.calculateBottomPadding()
                )
                .verticalScroll(rememberScrollState())
        ) {
            TitleView()
            InPutNickNameView(
                nickName = uiState.nickName,
                onValueChange = { input ->
                    viewModel.nickName(input)
                },
                showError = if (uiState.checkNickName is UiState.Success) !(uiState.checkNickName as UiState.Success<Boolean>).data else false,
                hint = when {
                    uiState.nickName.length < 2 -> stringResource(R.string.signUp_nickName_helper_one_more)
                    uiState.checkNickName is UiState.Success -> {
                        if (!(uiState.checkNickName as UiState.Success<Boolean>).data) {
                            stringResource(R.string.signUp_nickName_helper_error)
                        } else {
                            stringResource(R.string.signUp_nickName_helper)
                        }
                    }

                    else -> stringResource(R.string.signUp_nickName_helper)
                }
            )
        }
    }
}

@Composable
private fun TitleView() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(top = 16.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Component.PageNumber("1", isSelect = true)
        Component.PageNumber("2", isSelect = true)
        Component.PageNumber("3")
    }
    Text(
        text = stringResource(R.string.signUp_nickName_title),
        style = TextComponent.HEAD_2_B_24,
        color = NeutralColor.BLACK,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    )
}

@Composable
private fun InPutNickNameView(
    nickName: String,
    onValueChange: (String) -> Unit,
    showError: Boolean,
    hint: String,
) {
    TextFiledComponent.RightIcon(
        rightImageClick = {
            onValueChange("")
        },
        value = nickName,
        onValueChange = { input ->
            onValueChange(input)
        },
        placeHolder = stringResource(R.string.signUp_nickName_hint_debug),
        helperUse = true,
        helperText = hint,
        showError = showError
    )
}

