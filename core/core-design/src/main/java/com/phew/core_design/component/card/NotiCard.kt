package com.phew.core_design.component.card

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phew.core_design.NeutralColor
import com.phew.core_design.NeutralColor.WHITE
import com.phew.core_design.Primary
import com.phew.core_design.R
import com.phew.core_design.TextComponent
import com.phew.core_design.UnKnowColor
import com.phew.core_design.theme.SooumTheme

// NotiCard 데이터 모델
data class NoticeCardData(
    val id: String,
    val title: String,
    val description: String,
    @param:DrawableRes val iconRes: Int,
    val iconTint: Color,
    val iconBackgroundColor: Color
)

@Composable
fun NoticeCardVersionA(data: NoticeCardData, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(color = WHITE, shape = RoundedCornerShape(16.dp))
            .shadow(
                elevation = 16.dp,
                spotColor = UnKnowColor.color,
                ambientColor = UnKnowColor.color
            )
            .padding(start = 12.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(data.iconRes),
            contentDescription = data.description,
            modifier = Modifier
                .padding(1.dp)
                .width(24.dp)
                .height(24.dp)
        )
        Text(
            text = data.description,
            style = TextComponent.CAPTION_1_SB_12,
            color = NeutralColor.GRAY_600,
            modifier = Modifier.weight(1f)
        )
        Image(
            painter = painterResource(R.drawable.ic_delete),
            contentDescription = "close notice",
            colorFilter = ColorFilter.tint(NeutralColor.GRAY_300),
            modifier = Modifier
                .size(32.dp)
                .padding(6.dp)
        )
    }
}

@Composable
fun NotiCard(
    data: NoticeCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = WHITE,
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
                    Spacer(modifier = Modifier.height(2.dp))
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


// Previews
@Preview(showBackground = true, backgroundColor = 0xFF212121)
@Composable
private fun Preview_NoticeCard() {
    SooumTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NoticeCardVersionA(
                data = NoticeCardData(
                    id = "1",
                    title = "숨 새소식",
                    description = "숨이 새로운 서비스로 찾아올 예정이에요",
                    iconRes = R.drawable.ic_mail_filled_bule,
                    iconBackgroundColor = NeutralColor.GRAY_100,
                    iconTint = Primary.DARK,
                ),
                onClick = {}
            )
        }
    }
}