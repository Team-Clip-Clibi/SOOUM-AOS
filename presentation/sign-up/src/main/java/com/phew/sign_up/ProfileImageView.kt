package com.phew.sign_up

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phew.core_design.AppBar
import com.phew.core_design.LargeButton
import com.phew.core_design.NeutralColor
import androidx.compose.material3.Text
import com.phew.core_design.TextComponent

@Composable
fun ProfileImageView(viewModel: SignUpViewModel, onBack: () -> Unit, nexPage: () -> Unit) {
    Scaffold(topBar = {
        AppBar.IconLeftAppBar(
            onClick = onBack, appBarText = stringResource(R.string.signUp_app_bar)
        )
    }, bottomBar = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .background(color = NeutralColor.WHITE)
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
            verticalAlignment = Alignment.Top,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                LargeButton.NoIconSecondary(
                    onClick = nexPage,
                    buttonText = stringResource(com.phew.core_design.R.string.common_skip)
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                LargeButton.NoIconPrimary(
                    onClick = nexPage,
                    buttonText = stringResource(com.phew.core_design.R.string.common_finish)
                )
            }
        }
    }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = NeutralColor.WHITE)
                .padding(
                    top = paddingValues.calculateTopPadding() + 12.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = paddingValues.calculateBottomPadding()
                )
                .verticalScroll(rememberScrollState())
        ) {
            TitleView()
        }
    }
}

@Composable
private fun TitleView() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Component.PageNumber("1", isSelect = true)
        Component.PageNumber("2", isSelect = true)
        Component.PageNumber("3", isSelect = true)
    }
    Text(
        text = stringResource(R.string.signUp_picture_title),
        style = TextComponent.HEAD_2_B_24,
        color = NeutralColor.BLACK
    )
}

@Composable
@Preview
private fun Preview() {
    ProfileImageView(viewModel = SignUpViewModel(), onBack = {}, nexPage = {})
}

