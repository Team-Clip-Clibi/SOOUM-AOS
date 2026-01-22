package com.phew.presentation.settings.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phew.core.ui.component.ErrorDialog
import com.phew.core_design.AppBar.IconLeftAppBar
import com.phew.core_design.LargeButton
import com.phew.core_design.MediumButton.DisabledSecondary
import com.phew.core_design.MediumButton.SelectedSecondary
import com.phew.core_design.NeutralColor
import com.phew.core_design.TextComponent
import com.phew.presentation.settings.R
import com.phew.presentation.settings.viewmodel.WithdrawalReason
import com.phew.presentation.settings.viewmodel.WithdrawalUiEffect
import com.phew.presentation.settings.viewmodel.WithdrawalUiState
import com.phew.presentation.settings.viewmodel.WithdrawalViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.phew.core_design.R as DesignR

@Composable
internal fun WithdrawalRoute(
    modifier: Modifier = Modifier,
    viewModel: WithdrawalViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
    onWithdrawalComplete: () -> Unit = { }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var errorWithRefreshToken by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is WithdrawalUiEffect.ShowSuccessDialog -> {
                    onWithdrawalComplete()
                }

                is WithdrawalUiEffect.ShowError -> {
                    errorWithRefreshToken = effect.refreshToken
                }
            }
        }
    }

    WithdrawalScreen(
        modifier = modifier,
        uiState = uiState,
        onBackPressed = onBackPressed,
        onWithdrawal = viewModel::onWithdrawal,
        onSelectReason = viewModel::selectReason,
        onUpdateCustomReason = viewModel::updateCustomReason
    )

    errorWithRefreshToken?.let { refreshToken ->
        ErrorDialog(
            onDismiss = { errorWithRefreshToken = null },
            refreshToken = refreshToken
        )
    }
}

@Composable
private fun WithdrawalScreen(
    modifier: Modifier,
    uiState: WithdrawalUiState,
    onBackPressed: () -> Unit,
    onWithdrawal: () -> Unit,
    onSelectReason: (WithdrawalReason) -> Unit,
    onUpdateCustomReason: (String) -> Unit
) {
    val reasons = WithdrawalReason.entries.toTypedArray()

    val scrollState = rememberScrollState()
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NeutralColor.WHITE,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NeutralColor.WHITE)
            ) {
                IconLeftAppBar(
                    image = DesignR.drawable.ic_left,
                    onClick = onBackPressed,
                    appBarText = stringResource(R.string.withdrawal_top_app_bar)
                )
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = NeutralColor.WHITE)
                    .navigationBarsPadding() // 기본 네비게이션 바 패딩만 적용
                    .imePadding() // 키보드 패딩 추가
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    LargeButton.NoIconPrimary(
                        buttonText = stringResource(R.string.withdrawal_button),
                        isEnable = uiState.isWithdrawal && !uiState.isLoading,
                        onClick = onWithdrawal
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier // modifier 재사용 버그 수정: 파라미터 modifier 대신 새로운 Modifier 사용
                .fillMaxSize()
                .background(NeutralColor.WHITE)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.withdrawal_title),
                style = TextComponent.HEAD_2_B_24,
                color = NeutralColor.BLACK,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // 탈퇴 사유 버튼들 (1~6)
            reasons.forEach { reason ->
                val reasonTextRes = when (reason.resourceKey) {
                    "withdrawal_reason_1" -> R.string.withdrawal_reason_1
                    "withdrawal_reason_2" -> R.string.withdrawal_reason_2
                    "withdrawal_reason_3" -> R.string.withdrawal_reason_3
                    "withdrawal_reason_4" -> R.string.withdrawal_reason_4
                    "withdrawal_reason_5" -> R.string.withdrawal_reason_5
                    "withdrawal_reason_6" -> R.string.withdrawal_reason_6
                    else -> R.string.withdrawal_reason_6 // fallback
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    if (uiState.selectedReason == reason) {
                        SelectedSecondary(
                            buttonText = stringResource(reasonTextRes),
                            textAlign = TextAlign.Start,
                            onClick = { onSelectReason(reason) }
                        )
                    } else {
                        DisabledSecondary(
                            buttonText = stringResource(reasonTextRes),
                            textAlign = TextAlign.Start,
                            onClick = { onSelectReason(reason) }
                        )
                    }
                }
            }

            if (uiState.selectedReason == WithdrawalReason.OTHER) {
                OutlinedTextField(
                    value = uiState.customReasonText,
                    onValueChange = { text: String ->
                        if (text.length <= 250) {
                            onUpdateCustomReason(text)
                        }
                    },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.withdrawal_reason),
                            style = TextComponent.SUBTITLE_1_M_16,
                            color = NeutralColor.GRAY_500
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = NeutralColor.GRAY_100,
                        focusedBorderColor = NeutralColor.GRAY_100,
                        unfocusedContainerColor = NeutralColor.GRAY_100,
                        focusedContainerColor = NeutralColor.GRAY_100
                    ),
                    textStyle = TextComponent.SUBTITLE_1_M_16.copy(
                        color = NeutralColor.BLACK
                    )
                )

                LaunchedEffect(uiState.selectedReason) {
                    if (uiState.selectedReason == WithdrawalReason.OTHER) {
                        delay(100) // UI 업데이트를 위한 약간의 지연
                        focusRequester.requestFocus()
                        // TextField 위치로 스크롤
                        scope.launch {
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }
                    }
                }
            }
        }
    }
}
