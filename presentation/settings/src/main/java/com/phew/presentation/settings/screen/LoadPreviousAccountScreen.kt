package com.phew.presentation.settings.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phew.core_design.AppBar.IconLeftAppBar
import com.phew.core_design.LargeButton
import com.phew.core_design.NeutralColor
import com.phew.core_design.TextComponent
import com.phew.core_design.TextFiledComponent
import com.phew.core_design.component.card.SooumGuideCard
import com.phew.core_design.DialogComponent
import com.phew.core_design.R as DesignR
import com.phew.presentation.settings.R
import com.phew.presentation.settings.viewmodel.LoadPreviousAccountViewModel
import com.phew.presentation.settings.viewmodel.TransferAccountEvent


@Composable
internal fun LoadPreviousAccountRoute(
    modifier: Modifier = Modifier,
    viewModel: LoadPreviousAccountViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
    onNavigateToFeed: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var transferCode by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState.transferEvent) {
        when (uiState.transferEvent) {
            is TransferAccountEvent.Success -> {
                showSuccessDialog = true
            }
            is TransferAccountEvent.Error -> {
                showErrorDialog = true
            }
            null -> {
                // Do nothing
            }
        }
    }
    
    LoadPreviousAccountScreen(
        modifier = modifier,
        code = transferCode,
        changeCode = { transferCode = it },
        onBackPressed = onBackPressed,
        isLoading = uiState.isLoading,
        onClicked = {
            if (transferCode.isNotBlank()) {
                viewModel.transferAccount(transferCode)
            }
        }
    )
    
    // 성공 다이얼로그
    if (showSuccessDialog) {
        DialogComponent.DefaultButtonOne(
            title = stringResource(R.string.load_previous_account_dialog_success_title),
            description = stringResource(R.string.load_previous_account_dialog_success_message),
            buttonText = stringResource(R.string.load_previous_account_dialog_success_ok),
            onClick = {
                showSuccessDialog = false
                viewModel.clearTransferEvent()
                onNavigateToFeed()
            },
            onDismiss = {
                showSuccessDialog = false
                viewModel.clearTransferEvent()
            }
        )
    }
    
    // 실패 다이얼로그
    if (showErrorDialog) {
        DialogComponent.DefaultButtonOne(
            title = stringResource(R.string.load_previous_account_dialog_wrong_title),
            description = stringResource(R.string.load_previous_account_dialog_wrong_message),
            buttonText = stringResource(R.string.load_previous_account_dialog_wrong_ok),
            onClick = {
                showErrorDialog = false
                viewModel.clearTransferEvent()
            },
            onDismiss = {
                showErrorDialog = false
                viewModel.clearTransferEvent()
            }
        )
    }
}

@Composable
private fun LoadPreviousAccountScreen(
    modifier: Modifier = Modifier,
    code: String,
    changeCode: (String) -> Unit,
    onBackPressed: () -> Unit,
    isLoading: Boolean = false,
    onClicked: () -> Unit
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
                    appBarText = stringResource(R.string.load_previous_account_top_title)
                )
            }
        },
        bottomBar = {
            Box(
                modifier.padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                LargeButton.NoIconPrimary(
                    buttonText = stringResource(R.string.load_previous_account_ok),
                    isEnable = !isLoading && code.isNotBlank(),
                    onClick = onClicked
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
            Spacer(Modifier.height(16.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.load_previous_account_title),
                style = TextComponent.HEAD_2_B_24,
                color = NeutralColor.BLACK
            )
            Spacer(Modifier.height(4.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.load_previous_account_subtitle),
                style = TextComponent.TITLE_2_SB_16,
                color = NeutralColor.GRAY_500
            )
            Spacer(Modifier.height(32.dp))
            TextFiledComponent.NoIcon(
                value = code,
                onValueChange = { input ->
                    changeCode(input)
                },
                placeHolder = stringResource(R.string.load_previous_account_text_hint)
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                SooumGuideCard(
                    title = stringResource(R.string.load_previous_account_guide_title),
                    content = stringResource(R.string.load_previous_account_guide_message)
                )
            }
        }
    }
}

@Preview
@Composable
private fun LoadPreviousAccountPreview(

) {
    LoadPreviousAccountScreen(
        code = "",
        changeCode = {},
        onClicked = {},
        onBackPressed = {},
        isLoading = false
    )
}