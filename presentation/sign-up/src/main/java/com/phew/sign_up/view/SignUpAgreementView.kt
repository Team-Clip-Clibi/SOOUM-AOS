package com.phew.sign_up.view

import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
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
import com.phew.core_common.ERROR
import com.phew.core_design.AppBar
import com.phew.core_design.DialogComponent
import com.phew.core_design.LargeButton
import com.phew.core_design.NeutralColor
import com.phew.core_design.Primary
import com.phew.core_design.SignUpAgreeButton
import com.phew.core_design.TextComponent
import com.phew.sign_up.AGREEMENT_ALL
import com.phew.sign_up.AGREEMENT_LOCATION
import com.phew.sign_up.AGREEMENT_PERSONAL
import com.phew.sign_up.AGREEMENT_SERVICE
import com.phew.sign_up.Component
import com.phew.sign_up.R
import com.phew.sign_up.SignUp
import com.phew.sign_up.SignUpViewModel

@Composable
fun SignUpAgreementView(
    viewModel: SignUpViewModel,
    nextPage: () -> Unit,
    back: () -> Unit,
    onClickService: () -> Unit,
    onClickLocation: () -> Unit,
    onClickPrivate: () -> Unit,
) {
    val snackBarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    BackHandler {
        viewModel.initAgreement()
        back()
    }
    LaunchedEffect(uiState.nickName) {
        if (uiState.nickName.isNotEmpty() && uiState.nickName != ERROR) nextPage()
        if (uiState.nickName == ERROR) {
            snackBarHostState.showSnackbar(
                message = context.getString(com.phew.core_design.R.string.error_network),
                duration = SnackbarDuration.Short
            )
        }
    }
    Scaffold(
        topBar = {
            AppBar.IconLeftAppBar(
                onClick = {
                    viewModel.initAgreement()
                    back()
                },
                appBarText = stringResource(R.string.signUp_app_bar)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = NeutralColor.WHITE)
                    .navigationBarsPadding()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 12.dp
                    )
            ) {
                LargeButton.NoIconPrimary(
                    buttonText = stringResource(com.phew.core_design.R.string.common_next),
                    onClick = viewModel::generateNickName,
                    isEnable = uiState.agreementAll || (uiState.agreedToTermsOfService && uiState.agreedToLocationTerms && uiState.agreedToPrivacyPolicy)
                )
            }
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
                    top = paddingValues.calculateTopPadding(),
                    start = 16.dp,
                    end = 16.dp
                )
        ) {
            PageNumberView()
            ContentView(
                uiState = uiState,
                onClick = remember(viewModel) {
                    { agreement: String -> viewModel.agreement(agreement) }
                },
                onClickService = onClickService,
                onClickLocation = onClickLocation,
                onClickPrivate = onClickPrivate
            )
        }
    }
}

@Composable
private fun PageNumberView() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(top = 16.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Component.PageNumber("1", isSelect = true)
        Component.PageNumber("2")
        Component.PageNumber("3")
    }
}

@Composable
private fun ContentView(
    uiState: SignUp, onClick: (String) -> Unit,
    onClickService: () -> Unit,
    onClickLocation: () -> Unit,
    onClickPrivate: () -> Unit,
) {
    Text(
        text = stringResource(R.string.signUp_agree_txt_title),
        style = TextComponent.HEAD_2_B_24,
        color = NeutralColor.BLACK,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    )
    SignUpAgreeButton.AgreeAllButton(
        text = stringResource(R.string.signUp_agree_txt_agree_all),
        onClick = {
            onClick(AGREEMENT_ALL)
        },
        isSelected = uiState.agreementAll,
        selectColor = Primary.DARK
    )
    Spacer(modifier = Modifier.height(8.dp))
    SignUpAgreeButton.AgreeButton(
        text = stringResource(R.string.signUp_agree_txt_service),
        onClick = {
            onClick(AGREEMENT_SERVICE)
        },
        isSelected = uiState.agreedToTermsOfService || uiState.agreementAll,
        endClick = onClickService
    )
    SignUpAgreeButton.AgreeButton(
        text = stringResource(R.string.signUp_agree_txt_location),
        onClick = {
            onClick(AGREEMENT_LOCATION)
        },
        isSelected = uiState.agreedToLocationTerms || uiState.agreementAll,
        endClick = onClickLocation
    )
    SignUpAgreeButton.AgreeButton(
        text = stringResource(R.string.signUp_agree_personal),
        onClick = {
            onClick(AGREEMENT_PERSONAL)
        },
        isSelected = uiState.agreedToPrivacyPolicy || uiState.agreementAll,
        endClick = onClickPrivate
    )
}
