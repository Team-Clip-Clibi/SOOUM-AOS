package com.phew.sign_up

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.phew.core_design.NeutralColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.phew.core_design.LargeButton
import com.phew.core_design.Primary
import com.phew.core_design.TextComponent

@Composable
fun SignUpFinish(home: () -> Unit) {
    BackHandler {
        home()
    }
    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = NeutralColor.WHITE)
                    .navigationBarsPadding()
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                LargeButton.NoIconPrimary(
                    buttonText = stringResource(com.phew.core_design.R.string.common_okay),
                    onClick = home
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
                    bottom = paddingValues.calculateBottomPadding()
                )
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(com.phew.core_design.R.drawable.ic_finish),
                contentDescription = "finish sign up",
                contentScale = ContentScale.None,
                modifier = Modifier
                    .width(200.dp)
                    .height(230.dp)
                    .padding(bottom = 32.dp)
            )
            Text(
                text = stringResource(R.string.signUp_finish_txt_title),
                style = TextComponent.TITLE_2_SB_16,
                color = Primary.DARK,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
            Text(
                text = stringResource(R.string.signUp_finish_txt_content),
                style = TextComponent.HEAD1_B_28,
                color = NeutralColor.BLACK
            )
        }
    }
}

@Composable
@Preview
private fun Preview() {
    SignUpFinish(home = {})
}