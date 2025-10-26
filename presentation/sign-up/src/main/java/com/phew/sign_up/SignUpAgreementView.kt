package com.phew.sign_up

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.phew.core_design.AppBar
import com.phew.core_design.LargeButton
import com.phew.core_design.NeutralColor
import com.phew.core_design.Primary
import com.phew.core_design.SignUpAgreeButton
import com.phew.core_design.TextComponent

@Composable
fun SignUpAgreementView(
    viewModel: SignUpViewModel,
    nextPage: () -> Unit,
    back: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    BackHandler {
        back()
    }
    Scaffold(
        topBar = {
            AppBar.IconLeftAppBar(
                onClick = back,
                appBarText = stringResource(R.string.signUp_app_bar)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = NeutralColor.WHITE)
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding() + 12.dp
                    )
            ) {
                LargeButton.NoIconPrimary(
                    buttonText = stringResource(com.phew.core_design.R.string.common_next),
                    onClick = {
                        nextPage()
                    },
                    isEnable = uiState.agreementAll || (uiState.agreedToTermsOfService && uiState.agreedToLocationTerms && uiState.agreedToPrivacyPolicy)
                )
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
                    end = 16.dp
                )
        ) {
            PageNumberView()
            ContentView(uiState = uiState, onClick = { agreement ->
                viewModel.agreement(agreement)
            })
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
private fun ContentView(uiState: SignUp, onClick: (String) -> Unit) {
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
        endClick = {
            //TODO 추후 노션 연결
        }
    )
    SignUpAgreeButton.AgreeButton(
        text = stringResource(R.string.signUp_agree_txt_location),
        onClick = {
            onClick(AGREEMENT_LOCATION)
        },
        isSelected = uiState.agreedToLocationTerms || uiState.agreementAll,
        endClick = {
            //TODO 추후 노션 연결
        }
    )
    SignUpAgreeButton.AgreeButton(
        text = stringResource(R.string.signUp_agree_personal),
        onClick = {
            onClick(AGREEMENT_PERSONAL)
        },
        isSelected = uiState.agreedToPrivacyPolicy || uiState.agreementAll,
        endClick = {
            //TODO 추후 노션 연결
        }
    )
}
