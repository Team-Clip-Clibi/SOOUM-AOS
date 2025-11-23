package com.phew.core.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.phew.core.ui.R
import com.phew.core.ui.util.InquiryUtils
import com.phew.core_design.DialogComponent
import com.phew.core_design.NeutralColor
import com.phew.core_design.theme.SooumTheme

/**
 * 일반적인 에러 상황에서 사용하는 다이얼로그
 * - 에러 메시지 표시
 * - 문의하기 버튼으로 숨 팀에 문의 가능
 * 
 * @param onDismiss 다이얼로그 닫기 콜백
 * @param refreshToken 문의 시 사용할 refreshToken
 */
@Composable
fun ErrorDialog(
    onDismiss: () -> Unit,
    refreshToken: String
) {
    val context = LocalContext.current

    DialogComponent.DefaultButtonTwo(
        title = stringResource(R.string.error_dialog_title),
        description = stringResource(R.string.error_dialog_content),
        buttonTextStart = stringResource(R.string.error_dialog_cancel),
        buttonTextEnd = stringResource(R.string.error_dialog_inquiry),
        onClick = {
            onDismiss()
            InquiryUtils.openInquiryMail(context, refreshToken)
        },
        onDismiss = {
            onDismiss()
        },
        startButtonTextColor = NeutralColor.BLACK
    )
}

@Preview
@Composable
private fun ErrorDialogPreview() {
    SooumTheme {
        ErrorDialog(
            onDismiss = {},
            refreshToken = "sample_token"
        )
    }
}