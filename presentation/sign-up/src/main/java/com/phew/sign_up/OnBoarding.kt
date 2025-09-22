package com.phew.sign_up

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.phew.core_design.NeutralColor
import com.phew.core_design.TextComponent
import androidx.compose.ui.unit.dp
import com.phew.core_design.DialogComponent
import com.phew.core_design.LargeButton
import com.phew.domain.SIGN_UP_BANNED
import com.phew.domain.SIGN_UP_OKAY
import com.phew.domain.SIGN_UP_REGISTERED
import com.phew.domain.SIGN_UP_WITHDRAWN
import com.phew.sign_up.dto.SignUpResult

@Composable
fun OnBoarding(
    viewModel: SignUpViewModel,
    signUp: () -> Unit,
    alreadySignUp: () -> Unit,
    back: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    var dialogShow = remember { mutableStateOf(false) }
    val context = LocalContext.current
    BackHandler {
        back()
    }
    LaunchedEffect(uiState) {
        when (val result = uiState.checkSignUp) {
            is UiState.Fail -> {
                snackBarHostState.showSnackbar(
                    message = context.getString(com.phew.core_design.R.string.error_network),
                    duration = SnackbarDuration.Short
                )
            }

            is UiState.Success -> {
                when (result.data.result) {
                    SIGN_UP_OKAY -> {
                        signUp()
                    }

                    SIGN_UP_REGISTERED -> {
                        viewModel.login()
                    }
                }
            }

            else -> Unit
        }
    }
    LaunchedEffect(uiState) {
        when (uiState.login) {
            is UiState.Fail -> {
                snackBarHostState.showSnackbar(
                    message = context.getString(com.phew.core_design.R.string.error_network),
                    duration = SnackbarDuration.Short
                )
            }

            is UiState.Success -> {
                //TODO HOME 화면 포팅
            }

            else -> Unit
        }
    }
    Scaffold(
        bottomBar = {
            BottomView(
                onClickStart = {
                    when (val checkSignUpResult = uiState.checkSignUp) {
                        is UiState.Success -> {
                            if (checkSignUpResult.data.result == SIGN_UP_OKAY) {
                                signUp()
                            } else {
                                dialogShow.value = true
                            }
                        }

                        else -> UInt
                    }
                },
                onClickAlreadySignUp = {
                    alreadySignUp()
                }
            )
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
                    top = paddingValues.calculateTopPadding() + 60.dp,
                    bottom = paddingValues.calculateBottomPadding(),
                    start = 16.dp,
                    end = 16.dp
                )
                .verticalScroll(rememberScrollState())
        ) {
            TitleView()
            ContentView()
            if (dialogShow.value) {
                DialogView((uiState.checkSignUp as UiState.Success<SignUpResult>).data, onclick = {
                    dialogShow.value = false
                })
            }
        }
    }
}

@Composable
private fun TitleView() {
    Text(
        text = stringResource(R.string.onBoarding_txt_title),
        style = TextComponent.HEAD1_B_28,
        color = NeutralColor.BLACK,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
    )
    Text(
        text = stringResource(R.string.onBoarding_txt_sub_title),
        style = TextComponent.TITLE_2_SB_16,
        color = NeutralColor.GRAY_500,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 60.dp)
    )
}

@Composable
private fun ContentView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 50.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(com.phew.core_design.R.drawable.ic_app_character_onboarding),
            contentDescription = "onBoarding character",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .width(239.dp)
                .height(218.dp)
                .padding(bottom = 48.dp)
        )
        ExplainView(stringResource(R.string.onBoarding_explain_no_personal))
        ExplainView(stringResource(R.string.onBoarding_explain_nick_name))
        ExplainView(stringResource(R.string.onBoarding_explain_annoy))
    }
}

@Composable
private fun ExplainView(data: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(com.phew.core_design.R.drawable.ic_check_filled_blue),
            contentDescription = "data",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(24.dp)
        )
        Text(
            text = data,
            style = TextComponent.BODY_1_M_14,
            color = NeutralColor.GRAY_400
        )
    }
}

@Composable
private fun BottomView(
    onClickStart: () -> Unit,
    onClickAlreadySignUp: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = NeutralColor.WHITE)
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
    ) {
        LargeButton.NoIconPrimary(
            buttonText = stringResource(R.string.onBoarding_btn_start),
            onClick = onClickStart
        )

        Text(
            text = stringResource(R.string.onBoarding_btn_already_sign_up),
            style = TextComponent.BODY_1_M_14,
            color = NeutralColor.GRAY_500,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .padding(top = 8.dp)
                .clickable { onClickAlreadySignUp() }
        )
    }
}

@Composable
private fun DialogView(data: SignUpResult, onclick: () -> Unit) {
    when (data.result) {
        SIGN_UP_BANNED -> {
            DialogComponent.DefaultButtonOne(
                title = stringResource(R.string.onBoarding_dialog_banned_title),
                description = stringResource(R.string.onBoarding_dialog_banned_content),
                onClick = {
                    onclick()
                },
                onDismiss = {
                    onclick()
                },
                buttonText = stringResource(com.phew.core_design.R.string.common_okay)
            )
        }

        SIGN_UP_WITHDRAWN -> {
            DialogComponent.DefaultButtonOne(
                title = stringResource(R.string.onBoarding_dialog_withdraw_title),
                description = stringResource(R.string.onBoarding_dialog_withdraw_content),
                onClick = {
                    onclick()
                },
                onDismiss = {
                    onclick()
                },
                buttonText = stringResource(com.phew.core_design.R.string.common_okay)
            )
        }
    }
}

