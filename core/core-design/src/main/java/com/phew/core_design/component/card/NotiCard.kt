package com.phew.core_design.component.card

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phew.core_design.NeutralColor
import com.phew.core_design.Primary
import com.phew.core_design.R
import com.phew.core_design.TextComponent
import com.phew.core_design.UnKnowColor
import com.phew.core_design.theme.SooumTheme

// NotiCard 데이터 모델
data class NotiCardData(
    val id: String,
    val title: String,
    val description: String,
    @DrawableRes val iconRes: Int,
    val iconTint: Color,
    val iconBackgroundColor: Color
)

@Composable
fun NotiCard(
    data: NotiCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = NeutralColor.WHITE,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 좌측: 아이콘 + 텍스트
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // 아이콘
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(data.iconBackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(data.iconRes),
                        contentDescription = data.title,
                        tint = data.iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // 텍스트 영역
                Column(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = data.title,
                        style = TextComponent.CAPTION_2_M_12,
                        color = NeutralColor.GRAY_400
                    )
                    Text(
                        text = data.description,
                        style = TextComponent.SUBTITLE_3_SB_14,
                        color = NeutralColor.GRAY_600,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun NoticeCardPager(
    dataList: List<NotiCardData>,
    onClick: (NotiCardData) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 카드 리스트
        dataList.forEach { data ->
            NotiCard(
                data = data,
                onClick = { onClick(data) }
            )
        }
    }
}


// Previews
@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun Preview_NotiCard() {
    SooumTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 숨 새소식
            NotiCard(
                data = NotiCardData(
                    id = "1",
                    title = "숨 새소식",
                    description = "숨이 새로운 서비스로 찾아올 예정이에요",
                    iconRes = R.drawable.ic_mail_filled_bule,
                    iconBackgroundColor = NeutralColor.GRAY_100,
                    iconTint = Primary.DARK,
                ),
                onClick = {}
            )

            // 서비스 안내
            NotiCard(
                data = NotiCardData(
                    id = "2",
                    title = "서비스 안내",
                    description = "숨 공식 인스타그램 안내드려요",
                    iconRes = R.drawable.ic_notification,
                    iconBackgroundColor = NeutralColor.GRAY_100,
                    iconTint = Color.Red,
                ),
                onClick = {}
            )

            // 서비스 점검
            NotiCard(
                data = NotiCardData(
                    id = "3",
                    title = "서비스 점검",
                    description = "카드 작성 시 발생했던 오류가 해결됐어요",
                    iconRes = R.drawable.ic_tool_filled,
                    iconBackgroundColor = NeutralColor.GRAY_100,
                    iconTint = NeutralColor.GRAY_400,
                ),
                onClick = {}
            )
        }
    }
}