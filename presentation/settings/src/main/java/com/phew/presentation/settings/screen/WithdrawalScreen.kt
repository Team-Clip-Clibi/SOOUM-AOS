package com.phew.presentation.settings.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.phew.core_design.AppBar.IconLeftAppBar
import com.phew.core_design.DialogComponent
import com.phew.core_design.LargeButton
import com.phew.core_design.MediumButton.NoIconSecondary
import com.phew.core_design.NeutralColor
import com.phew.core_design.Primary
import com.phew.core_design.TextComponent
import com.phew.core_design.R as DesignR
import com.phew.presentation.settings.R
import com.phew.presentation.settings.viewmodel.WithdrawalReason
import com.phew.presentation.settings.viewmodel.WithdrawalUiEffect
import com.phew.presentation.settings.viewmodel.WithdrawalUiState
import com.phew.presentation.settings.viewmodel.WithdrawalViewModel

@Composable
internal fun WithdrawalRoute(
    modifier: Modifier = Modifier,
    viewModel: WithdrawalViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
    onWithdrawalComplete: () -> Unit = { }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is WithdrawalUiEffect.ShowSuccessDialog -> {
                    showSuccessDialog = true
                }
                is WithdrawalUiEffect.ShowError -> {
                    errorMessage = effect.message
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
    
    // 성공 다이얼로그 (withdrawal_dialog_* 키 사용)
    if (showSuccessDialog) {
        DialogComponent.DefaultButtonOne(
            title = stringResource(R.string.withdrawal_dialog_title),
            description = stringResource(R.string.withdrawal_dialog_content),
            buttonText = stringResource(R.string.withdrawal_dialog_ok),
            onClick = {
                showSuccessDialog = false
                onWithdrawalComplete()
            },
            onDismiss = {
                showSuccessDialog = false
                onWithdrawalComplete()
            }
        )
    }
    
    errorMessage?.let { message ->
        // TODO: 에러 처리 구현
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
    val reasons = WithdrawalReason.values()
    
    val scrollState = rememberScrollState()
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier
            .background(NeutralColor.WHITE),
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
                    .imePadding()
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                LargeButton.NoIconPrimary(
                    buttonText = stringResource(R.string.withdrawal_button),
                    isEnable = uiState.isWithdrawal && !uiState.isLoading,
                    onClick = onWithdrawal
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
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
                val reasonTextRes = when (reason) {
                    WithdrawalReason.RARELY_USE -> R.string.withdrawal_reason_1
                    WithdrawalReason.NO_DESIRED_FEATURE -> R.string.withdrawal_reason_2
                    WithdrawalReason.FREQUENT_ERRORS -> R.string.withdrawal_reason_3
                    WithdrawalReason.DIFFICULT_TO_USE -> R.string.withdrawal_reason_4
                    WithdrawalReason.CREATE_NEW_ACCOUNT -> R.string.withdrawal_reason_5
                    WithdrawalReason.OTHER -> R.string.withdrawal_reason_6
                }
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    NoIconSecondary(
                        buttonText = stringResource(reasonTextRes),
                        onClick = { onSelectReason(reason) },
                        isSelect = uiState.selectedReason == reason,
                        borderColor = if (uiState.selectedReason == reason) Primary.DARK else NeutralColor.GRAY_100,
                        baseColor = if (uiState.selectedReason == reason) Primary.LIGHT_1 else NeutralColor.WHITE,
                        textCenter = false
                    )
                }
            }
            
            // OTHER 선택 시 TextField 표시
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
                            color = NeutralColor.BLACK
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
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
                
                // OTHER 선택 시 TextField로 스크롤하고 포커스
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
