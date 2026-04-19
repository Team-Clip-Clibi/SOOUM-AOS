package com.phew.presentation.settings.component.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phew.core_design.NeutralColor
import com.phew.core_design.TextComponent
import com.phew.core_design.component.control.SooumSwitch


@Composable
internal fun AlarmView(title: String, onClick: (Boolean) -> Unit, isActivate: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(color = NeutralColor.WHITE)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = TextComponent.BODY_1_M_14,
            color = NeutralColor.BLACK
        )
        SooumSwitch(
            isSelected = isActivate,
            isDisabled = false,
            onClick = { onClick(!isActivate) }
        )
    }
}

@Composable
internal fun AlarmViewWithSubTitle(
    title: String,
    subTitle: String,
    isActivate: Boolean,
    onClick: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(color = NeutralColor.WHITE)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = title,
                style = TextComponent.BODY_1_M_14,
                color = NeutralColor.BLACK
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subTitle,
                style = TextComponent.CAPTION_3_M_10,
                color = NeutralColor.GRAY_400
            )
        }
        SooumSwitch(
            isSelected = isActivate,
            isDisabled = false,
            onClick = { onClick(!isActivate) }
        )
    }
}

@Preview
@Composable
private fun PreviewAlarmView() {
    Column(modifier = Modifier.background(color = NeutralColor.GRAY_500)) {
        AlarmView(
            title = "작성한 카드의 댓글",
            isActivate = true,
            onClick = {}
        )
        Spacer(modifier = Modifier.height(10.dp))
        AlarmViewWithSubTitle(
            title = "태그 알림",
            subTitle = "관심 태그가 포함된 카드가 올라운 경우",
            isActivate = true,
            onClick = {}
        )
    }
}