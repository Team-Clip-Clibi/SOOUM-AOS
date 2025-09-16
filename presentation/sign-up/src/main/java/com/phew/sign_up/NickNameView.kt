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
import com.phew.core_design.TextComponent
import com.phew.core_design.TextFiledComponent

@Composable
fun NickNameView(viewModel: SignUpViewModel, onBack: () -> Unit, nextPage: () -> Unit) {
    BackHandler {
        onBack()
    }
    val uiState by viewModel.uiState.collectAsState()
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
                    onClick = {
                        nextPage()
                    },
                    isEnable = uiState.nickName.trim().isNotEmpty()
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
private fun InPutNickNameView(nickName: String, onValueChange: (String) -> Unit) {
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
        helperText = stringResource(R.string.signUp_nickName_helper)
    )
}

