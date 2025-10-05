package com.phew.sign_up

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.phew.core_design.AppBar
import androidx.compose.ui.unit.dp
import com.phew.core_design.NeutralColor
import com.phew.core_design.Primary
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.phew.core_design.LargeButton
import com.phew.core_design.TextComponent
import com.phew.core_design.TextFiledComponent

@Composable
fun AuthCodeView(viewModel: SignUpViewModel, onBack: () -> Unit, home: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    BackHandler {
        onBack()
    }
    Scaffold(
        topBar = {
            AppBar.IconLeftAppBar(
                onClick = {
                    onBack()
                },
                appBarText = stringResource(R.string.authCode_top_bar)
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
                ExplainContent(
                    onClick = home,
                    isEnable = uiState.authCode.trim().isNotEmpty()
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = NeutralColor.WHITE)
                .padding(
                    top = paddingValues.calculateTopPadding() + 16.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = paddingValues.calculateBottomPadding()
                )
        ) {
            Text(
                text = stringResource(R.string.authCode_txt_title),
                style = TextComponent.HEAD_2_B_24,
                color = NeutralColor.BLACK,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            )
            TextFiledComponent.NoIcon(
                value = uiState.authCode,
                onValueChange = { input ->
                    viewModel.authCode(input)
                },
                placeHolder = stringResource(R.string.authCode_hint_title),
                useHelper = true,
                helperText = stringResource(R.string.authCode_txt_code_content),
                helperTextColor = NeutralColor.GRAY_500
            )
        }
    }

}

@Composable
private fun ExplainContent(onClick: () -> Unit, isEnable: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(89.dp)
            .background(color = Primary.LIGHT_1, shape = RoundedCornerShape(size = 10.dp))
            .padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
        horizontalAlignment = Alignment.Start,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.Start),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(com.phew.core_design.R.drawable.ic_info_filled),
                contentDescription = stringResource(R.string.authCode_txt_code_content),
            )
            Text(
                text = stringResource(R.string.authCode_txt_content_title),
                style = TextComponent.SUBTITLE_2_SB_14,
                color = NeutralColor.BLACK
            )
        }
        Text(
            text = stringResource(R.string.authCode_txt_content_content),
            style = TextComponent.CAPTION_2_M_12,
            color = NeutralColor.GRAY_500
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    LargeButton.NoIconPrimary(
        onClick = onClick,
        buttonText = stringResource(com.phew.core_design.R.string.common_okay),
        isEnable = isEnable
    )
}
