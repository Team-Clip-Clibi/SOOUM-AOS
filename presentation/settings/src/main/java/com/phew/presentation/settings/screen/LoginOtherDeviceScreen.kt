package com.phew.presentation.settings.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phew.core_design.AppBar.IconLeftAppBar
import com.phew.core_design.LargeButton
import com.phew.core_design.NeutralColor
import com.phew.core_design.Primary
import com.phew.core_design.R as DesignR
import com.phew.core_design.TextComponent
import com.phew.presentation.settings.model.LoginOtherDeviceNavigationEvent
import com.phew.presentation.settings.viewmodel.LoginOtherDeviceViewModel
import com.phew.presentation.settings.R
import kotlinx.coroutines.flow.collectLatest

@Composable
internal fun LoginOtherDeviceRoute(
    modifier: Modifier = Modifier,
    viewModel: LoginOtherDeviceViewModel = hiltViewModel(),
    onBackPressed: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.navigationEvent.collectLatest { event ->
            when (event) {
                LoginOtherDeviceNavigationEvent.NavigateBack -> onBackPressed()
            }
        }
    }

    LoginOtherDeviceScreen(
        modifier = modifier,
        code = uiState.code,
        remainingTimeText = uiState.remainingTimeText,
        isRetryEnabled = uiState.isRetryEnabled,
        isLoading = uiState.isLoading,
        onBackPressed = viewModel::onBackPressed,
        onRetryCodeClick = viewModel::onRetryCodeClick
    )
}

@Composable
private fun LoginOtherDeviceScreen(
    modifier: Modifier = Modifier,
    code: String,
    remainingTimeText: String,
    isRetryEnabled: Boolean,
    isLoading: Boolean = false,
    onBackPressed: () -> Unit,
    onRetryCodeClick: () -> Unit
) {
    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NeutralColor.WHITE)
            ) {
                IconLeftAppBar(
                    image = DesignR.drawable.ic_left,
                    onClick = onBackPressed,
                    appBarText = stringResource(R.string.other_device_top_title)
                )
            }
        },
        bottomBar = {
            Box(
                modifier.padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                LargeButton.NoIconPrimary(
                    buttonText = stringResource(R.string.other_device_retry_code),
                    isEnable = isRetryEnabled && !isLoading,
                    onClick = {
                        onRetryCodeClick()
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(NeutralColor.WHITE)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Title
            Text(
                text = stringResource(R.string.other_device_title),
                style = TextComponent.HEAD_2_B_24,
                color = NeutralColor.BLACK,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
                    .height(54.dp)
                    .background(
                        color = NeutralColor.GRAY_100,
                        shape = RoundedCornerShape(10.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = code,
                    onValueChange = { },
                    readOnly = true,
                    textStyle = TextComponent.SUBTITLE_1_M_16.copy(
                        color = NeutralColor.BLACK,
                        textAlign = TextAlign.Start
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.weight(2.5f)
                        .padding(start = 24.dp, end = 10.dp)
                ) { innerTextField ->
                    innerTextField()
                }

                // Timer text
                Text(
                    text = remainingTimeText,
                    style = TextComponent.BODY_2_R_14,
                    color = Primary.DARK,
                    modifier = Modifier.weight(0.5f)
                        .padding(end = 24.dp),
                    textAlign = TextAlign.End
                )
            }

            
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text =  stringResource(R.string.other_device_one_hour_timeout),
                style = TextComponent.CAPTION_2_M_12,
                color = NeutralColor.GRAY_500
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginOtherDeviceScreenPreview() {
    LoginOtherDeviceScreen(
        code = "eHq8kSd926",
        remainingTimeText = "48:48",
        isRetryEnabled = true,
        isLoading = false,
        onBackPressed = {},
        onRetryCodeClick = {}
    )
}