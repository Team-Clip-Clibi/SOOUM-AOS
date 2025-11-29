package com.phew.sign_up

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import com.phew.core_design.DialogComponent
import com.phew.core_design.LargeButton
import com.phew.core_design.NeutralColor
import com.phew.core_design.SmallButton
import com.phew.core_design.TextComponent
import com.phew.domain.SIGN_UP_ALREADY_SIGN_UP
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
    home: () -> Unit,
    showWithdrawalDialog: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val dialogShow = remember { mutableStateOf(false) }
    val withdrawalDialogShow = remember { mutableStateOf(showWithdrawalDialog) }
    BackHandler(onBack = remember(back) { { back() } })
    HandleCheckSignUp(
        checkSignUpState = uiState.checkSignUp,
        loginState = uiState.login,
        snackBarHostState = snackBarHostState,
        signUp = signUp,
        dialogShow = {
            dialogShow.value = true
        },
        login = viewModel::login,
        onHome = remember(home) { { home() } }
    )
    Scaffold(
        bottomBar = {
            BottomView(
                onClickStart = viewModel::checkRegister,
                onClickAlreadySignUp = remember(alreadySignUp) { alreadySignUp }
            )
        },
        snackbarHost = {
            DialogComponent.CustomAnimationSnackBarHost(hostState = snackBarHostState)
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
                DialogView(
                    (uiState.checkSignUp as UiState.Success<SignUpResult>).data,
                    onclick = remember { { dialogShow.value = false } }
                )
            }
            
            if (withdrawalDialogShow.value) {
                WithdrawalCompleteDialog(
                    onDismiss = remember { { withdrawalDialogShow.value = false } }
                )
            }
        }
    }
}

@Composable
private fun HandleCheckSignUp(
    checkSignUpState: UiState<SignUpResult>,
    loginState: UiState<Unit>,
    snackBarHostState: SnackbarHostState,
    signUp: () -> Unit,
    login: () -> Unit,
    dialogShow: () -> Unit,
    onHome: () -> Unit,
) {
    val context = LocalContext.current
    LaunchedEffect(checkSignUpState, loginState, snackBarHostState) {
        when (checkSignUpState) {
            is UiState.Fail -> {
                snackBarHostState.showSnackbar(
                    message = context.getString(com.phew.core_design.R.string.error_network),
                    duration = SnackbarDuration.Short
                )
            }

            is UiState.Success -> {
                when (checkSignUpState.data.result) {
                    SIGN_UP_OKAY -> {
                        signUp()
                    }

                    SIGN_UP_REGISTERED, SIGN_UP_ALREADY_SIGN_UP -> login()
                    else -> dialogShow()
                }
            }

            else -> Unit
        }
        when (loginState) {
            is UiState.Fail -> {
                snackBarHostState.showSnackbar(
                    message = context.getString(com.phew.core_design.R.string.error_network),
                    duration = SnackbarDuration.Short
                )
            }

            is UiState.Success -> onHome()
            else -> Unit
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
            .padding(bottom = 8.dp)
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
            painter = painterResource(com.phew.core_design.R.drawable.img_onboarding_down),
            contentDescription = "onBoarding character",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .width(239.dp)
                .height(218.dp)
        )
        Spacer(modifier = Modifier.height(48.dp))
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
            .height(32.dp)
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
            .padding(start = 16.dp, end = 16.dp, bottom = 15.dp)
    ) {
        LargeButton.NoIconPrimary(
            buttonText = stringResource(R.string.onBoarding_btn_start),
            onClick = onClickStart
        )
        Spacer(modifier = Modifier.height(8.dp))
        SmallButton.NoIconTertiary(
            buttonText = stringResource(R.string.onBoarding_btn_already_sign_up),
            onClick = onClickAlreadySignUp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DialogView(data: SignUpResult, onclick: () -> Unit) {
    when (data.result) {
        SIGN_UP_BANNED -> {
            DialogComponent.DefaultButtonOne(
                title = stringResource(R.string.onBoarding_dialog_banned_title),
                description = stringResource(R.string.onBoarding_dialog_banned_content, data.time),
                onClick = onclick,
                onDismiss = onclick,
                buttonText = stringResource(com.phew.core_design.R.string.common_okay)
            )
        }

        SIGN_UP_WITHDRAWN -> {
            DialogComponent.DefaultButtonOne(
                title = stringResource(R.string.onBoarding_dialog_withdraw_title),
                description = stringResource(R.string.onBoarding_dialog_withdraw_content, data.time),
                onClick = onclick,
                onDismiss = onclick,
                buttonText = stringResource(com.phew.core_design.R.string.common_okay)
            )
        }
    }
}

@Composable
private fun WithdrawalCompleteDialog(onDismiss: () -> Unit) {
    DialogComponent.DefaultButtonOne(
        title = stringResource(R.string.onBoarding_withdrawal_complete_title),
        description = stringResource(R.string.onBoarding_withdrawal_complete_content),
        onClick = onDismiss,
        onDismiss = onDismiss,
        buttonText = stringResource(com.phew.core_design.R.string.common_okay)
    )
}

