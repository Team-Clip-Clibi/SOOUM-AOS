package com.phew.reports

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phew.core_design.AppBar
import com.phew.core_design.DialogComponent
import com.phew.core_design.LargeButton
import com.phew.core_design.MediumButton
import com.phew.core_design.NeutralColor
import com.phew.core_design.Primary
import com.phew.core_design.TextComponent
import com.phew.core_design.theme.BLACK
import com.phew.core_design.theme.WHITE
import com.phew.domain.dto.ReportReason

@Composable
internal fun ReportView(
    viewModel: ReportCardViewModel = hiltViewModel(),
    cardId: String,
    onBack: () -> Unit,
    snackBarHostState: SnackbarHostState,
) {
    BackHandler {
        onBack()
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HandleState(
        uiState = uiState,
        snackBarHostState = snackBarHostState,
        onBack = onBack,
        showFinishDialog = {
            DialogComponent.DefaultButtonOne(
                title = stringResource(R.string.card_report_dialog_title),
                description = stringResource(R.string.card_report_dialog_content),
                buttonText = stringResource(com.phew.core_design.R.string.common_okay),
                onClick = { onBack() },
                onDismiss = { onBack() }
            )
        },
    )
    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = WHITE)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                LargeButton.NoIconPrimary(
                    onClick = { viewModel.reportCard(cardId) },
                    isEnable = uiState.reportReason != ReportReason.NONE,
                    buttonText = stringResource(com.phew.core_design.R.string.common_okay)
                )
            }
        },
        topBar = {
            AppBar.IconLeftAppBar(
                onClick = {
                    onBack()
                },
                appBarText = stringResource(R.string.card_report_app_bar_title)
            )
        },
        snackbarHost = {
            DialogComponent.CustomAnimationSnackBarHos(hostState = snackBarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = WHITE)
                .padding(
                    top = paddingValues.calculateTopPadding() + 16.dp,
                    bottom = paddingValues.calculateBottomPadding() + 16.dp,
                    start = 16.dp,
                    end = 16.dp
                )
                .verticalScroll(rememberScrollState())
        ) {
            TitleView()
            ItemView(
                onclick = { data ->
                    viewModel.setReportReason(data)
                },
                reportReason = uiState.reportReason
            )
        }
    }
}

@Composable
private fun TitleView() {
    Text(
        text = stringResource(R.string.card_report_title),
        style = TextComponent.HEAD_2_B_24,
        color = BLACK,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    )
}

@Composable
private fun ItemView(onclick: (ReportReason) -> Unit, reportReason: ReportReason) {
    MediumButton.NoIconSecondary(
        buttonText = stringResource(R.string.card_report_item_defamation_abuse),
        baseColor = if (reportReason == ReportReason.DEFAMATION_AND_ABUSE) Primary.LIGHT_1 else NeutralColor.GRAY_100,
        borderColor = if (reportReason == ReportReason.DEFAMATION_AND_ABUSE) Primary.MAIN else NeutralColor.GRAY_100,
        onClick = {
            onclick(ReportReason.DEFAMATION_AND_ABUSE)
        },
        textCenter = false
    )
    Spacer(modifier = Modifier.height(10.dp))
    MediumButton.NoIconSecondary(
        buttonText = stringResource(R.string.card_report_item_impersonation_fraud),
        baseColor = if (reportReason == ReportReason.IMPERSONATION_AND_FRAUD) Primary.LIGHT_1 else NeutralColor.GRAY_100,
        borderColor = if (reportReason == ReportReason.IMPERSONATION_AND_FRAUD) Primary.MAIN else NeutralColor.GRAY_100,
        onClick = {
            onclick(ReportReason.IMPERSONATION_AND_FRAUD)
        },
        textCenter = false
    )
    Spacer(modifier = Modifier.height(10.dp))
    MediumButton.NoIconSecondary(
        buttonText = stringResource(R.string.card_report_item_pornography),
        baseColor = if (reportReason == ReportReason.PORNOGRAPHY) Primary.LIGHT_1 else NeutralColor.GRAY_100,
        borderColor = if (reportReason == ReportReason.PORNOGRAPHY) Primary.MAIN else NeutralColor.GRAY_100,
        onClick = {
            onclick(ReportReason.PORNOGRAPHY)
        },
        textCenter = false
    )
    Spacer(modifier = Modifier.height(10.dp))
    MediumButton.NoIconSecondary(
        buttonText = stringResource(R.string.card_report_item_in_appropriate_advertising),
        baseColor = if (reportReason == ReportReason.INAPPROPRIATE_ADVERTISING) Primary.LIGHT_1 else NeutralColor.GRAY_100,
        borderColor = if (reportReason == ReportReason.INAPPROPRIATE_ADVERTISING) Primary.MAIN else NeutralColor.GRAY_100,
        onClick = {
            onclick(ReportReason.INAPPROPRIATE_ADVERTISING)
        },
        textCenter = false
    )
    Spacer(modifier = Modifier.height(10.dp))
    MediumButton.NoIconSecondary(
        buttonText = stringResource(R.string.card_report_item_privacy_violation),
        baseColor = if (reportReason == ReportReason.PRIVACY_VIOLATION) Primary.LIGHT_1 else NeutralColor.GRAY_100,
        borderColor = if (reportReason == ReportReason.PRIVACY_VIOLATION) Primary.MAIN else NeutralColor.GRAY_100,
        onClick = {
            onclick(ReportReason.PRIVACY_VIOLATION)
        },
        textCenter = false
    )
    Spacer(modifier = Modifier.height(10.dp))
    MediumButton.NoIconSecondary(
        buttonText = stringResource(R.string.card_report_item_other),
        baseColor = if (reportReason == ReportReason.OTHER) Primary.LIGHT_1 else NeutralColor.GRAY_100,
        borderColor = if (reportReason == ReportReason.OTHER) Primary.MAIN else NeutralColor.GRAY_100,
        onClick = {
            onclick(ReportReason.OTHER)
        },
        textCenter = false
    )
}

@Composable
private fun HandleState(
    uiState: ReportCardViewModel.ReportState,
    snackBarHostState: SnackbarHostState,
    onBack: () -> Unit,
    showFinishDialog: @Composable () -> Unit,
) {
    if (uiState.reportCard is ReportCardViewModel.UiState.Success) {
        showFinishDialog()
    }
    val context = LocalContext.current
    LaunchedEffect(uiState) {
        when (uiState.reportCard) {
            is ReportCardViewModel.UiState.Fail -> {
                snackBarHostState.showSnackbar(
                    message = context.getString(com.phew.core_design.R.string.error_app),
                    duration = SnackbarDuration.Short
                )
                onBack()
            }

            else -> Unit
        }
    }
}