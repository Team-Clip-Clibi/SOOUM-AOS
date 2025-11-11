package com.phew.core_design.component.card

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phew.core_design.R
import com.phew.core_design.TextComponent
import com.phew.core_design.theme.LIGHT_1
import com.phew.core_design.theme.SooumTheme

/**
 *  가이드 카드
 *  - title Icon이 바뀌는 일이 없지만 혹시 바뀐다면, 외부에서 바꿀 수 있도록 처리
 */
@Composable
fun SooumGuideCard(
    modifier: Modifier = Modifier,
    @DrawableRes titleIcon: Int = R.drawable.ic_info_filled,
    title: String,
    content: String
) {

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 89.dp)
                .width(343.dp)
                .background(LIGHT_1)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = titleIcon),
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = title,
                    style = TextComponent.SUBTITLE_3_SB_14
                )
            }
            Text(
                text = content,
                style = TextComponent.CAPTION_2_M_12
            )
        }
    }
}

@Preview
@Composable
private fun SooumGuideCardPreview() {
    SooumTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SooumGuideCard(
                titleIcon = R.drawable.ic_info_filled,
                title = "내 계정 가져오기 안내",
                content = "기존 휴대폰의 숨 앱[설정>내 계정 내보내기]에서 발급한 코드를.."
            )
        }
    }
}