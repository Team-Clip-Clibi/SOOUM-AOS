package com.phew.sign_up

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.phew.core_design.NeutralColor
import com.phew.core_design.TextComponent
import androidx.compose.ui.unit.dp
import com.phew.core_design.LargeButton

@Composable
fun OnBoarding(viewModel: SignUpViewModel) {
    Scaffold(bottomBar = {
        BottomView(
            onClickStart = {

            },
            onClickAlreadySignUp = {

            }
        )
    }) { paddingValues ->
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
        modifier = Modifier.fillMaxWidth(),
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
        Spacer(modifier = Modifier.height(54.5.dp))
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
            modifier = Modifier
                .size(24.dp)
                .padding(1.25.dp)
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
            .padding(start = 16.dp, end = 16.dp)
    ) {
        LargeButton.NoIconPrimary(
            buttonText = stringResource(R.string.onBoarding_btn_start),
            onClick = onClickStart
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.onBoarding_btn_already_sign_up),
            style = TextComponent.BODY_1_M_14,
            color = NeutralColor.GRAY_500,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .clickable { onClickAlreadySignUp() }
        )
    }
}

@Composable
@Preview
private fun Preview() {
    OnBoarding(viewModel = SignUpViewModel())
}