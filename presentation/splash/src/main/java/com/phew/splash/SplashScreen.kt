package com.phew.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phew.core_design.NeutralColor
import com.phew.core_design.Primary

@Composable
fun SplashScreen(viewModel: SplashViewModel, nextPage: () -> Unit) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Primary.MAIN)
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                ),
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painter = painterResource(id = com.phew.core_design.R.drawable.ic_sooum_black),
                contentDescription = "app logo",
                tint = NeutralColor.WHITE,
                modifier = Modifier
                    .width(200.dp)
                    .height(33.dp)
                    .padding(1.dp)
                    .clickable { nextPage() } // TODO 앱 버전 체크 후 dialog으로 대체
            )
        }
    }
}

@Composable
@Preview
private fun Preview() {
    SplashScreen(viewModel = SplashViewModel(), nextPage = {})
}
