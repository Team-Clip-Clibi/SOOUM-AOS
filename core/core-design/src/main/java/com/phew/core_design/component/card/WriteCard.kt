package com.phew.core_design.component.card

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isFinite
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.phew.core_design.NeutralColor
import com.phew.core_design.OpacityColor
import com.phew.core_design.Primary
import com.phew.core_design.R
import com.phew.core_design.TextComponent
import com.phew.core_design.component.tag.TagRow

// ===== 디자인 토큰 =====
object CardDesignTokens {
    // 카드 배경색
    val CardBackgroundCyan = Primary.MAIN
    val CardBackgroundGray = NeutralColor.GRAY_100

    // 콘텐츠 박스 배경색
    val ContentBoxDark = NeutralColor.GRAY_600
    val ContentBoxGray = NeutralColor.GRAY_200

    // 태그 배경색
    val TagBackground = OpacityColor.blackSmallColor

    // 텍스트 색상
    val TextPrimary = NeutralColor.WHITE
    val TextSecondary = NeutralColor.GRAY_400
    val TextDelete = NeutralColor.GRAY_400
    val TextBackTint = NeutralColor.GRAY_300

    // 크기
    val CardRadius = 12.dp
    val ContentBoxRadius = 8.dp
    val TagRadius = 4.dp
    val AvatarSize = 40.dp
    val CornerRadius = 12.dp

    // 패딩
    val CardPadding = 16.dp
    val ContentPadding = 16.dp

    // Typography
    val BodyFontSize = 14.sp
    val BodyLineHeight = 20.sp
}

enum class CardType {
    WRITE, REPLY, DELETED
}

sealed class BaseCardData(open val id: String, open val type: CardType) {
    data class Write(
        override val id: String,
        val content: String,
        val tags: List<String> = emptyList(),
        val hasAddButton: Boolean = true,
        val hasThumbnail: Boolean = false
    ) : BaseCardData(id, CardType.WRITE)

    data class Reply(
        override val id: String,
        val authorName: String,
        val authorProfileUrl: String? = null,
        val content: String,
        val tags: List<String> = emptyList(),
        val timeAgo: String = ""
    ) : BaseCardData(id, CardType.REPLY)

    data class Deleted(
        override val id: String,
        val reason: String = "삭제된 카드예요"
    ) : BaseCardData(id, CardType.DELETED)
}

data class WriteCardData(
    val id: String,
    val content: String,
    val tags: List<String> = emptyList(),
    val hasAddButton: Boolean = true,
    val hasThumbnail: Boolean = false
)

@Composable
fun CardView(data: BaseCardData) {
    when (data.type) {
        CardType.WRITE -> WriteCard(data as BaseCardData.Write)
        CardType.REPLY -> ReplyCard(data as BaseCardData.Reply)
        CardType.DELETED -> DeletedCard(data as BaseCardData.Deleted)
    }
}


@Composable
private fun BaseCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    imgUrl: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 328.dp)
            .widthIn(min = 328.dp)
            .clip(RoundedCornerShape(CardDesignTokens.CardRadius))
    ) {
        Surface(
            modifier = Modifier.matchParentSize(),
            color = backgroundColor,
            shape = RoundedCornerShape(CardDesignTokens.CardRadius),
            shadowElevation = 2.dp
        ) {}

        if (imgUrl != null) {
            AsyncImage(
                model = imgUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(CardDesignTokens.CardRadius))
            )
        }

        // wrapContentHeight로 안전하게
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(CardDesignTokens.CardPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}



@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun WriteContentBox(content: String, modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = CardDesignTokens.ContentBoxDark,
                shape = RoundedCornerShape(CardDesignTokens.ContentBoxRadius)
            )
            .padding(CardDesignTokens.ContentPadding)
    ) {
        val scrollState = rememberScrollState()
        val boundedHeight = maxHeight.takeIf { it.isFinite } ?: 200.dp

        Box(
            modifier = Modifier
                .heightIn(min = 61.dp, max = boundedHeight)
                .verticalScroll(scrollState),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = content.ifBlank { " " },
                style = TextComponent.BODY_1_M_14.copy(color = CardDesignTokens.TextPrimary),
                maxLines = Int.MAX_VALUE
            )
        }
    }
}

@Composable
private fun WriteCard(data: BaseCardData.Write) {
    BaseCard(backgroundColor = CardDesignTokens.CardBackgroundCyan) {
        if (data.hasThumbnail) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(CardDesignTokens.ContentBoxDark, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_info_filled),
                    contentDescription = "썸네일 있음",
                    tint = CardDesignTokens.TextPrimary
                )
            }
        }

        WriteContentBox(content = data.content)

        TagRow(
            tags = data.tags,
            enableAdd = data.hasAddButton,
            onAdd = {},
            onRemove = {}
        )
    }
}

@Composable
private fun ReplyCard(data: BaseCardData.Reply) {
    BaseCard(backgroundColor = CardDesignTokens.CardBackgroundCyan) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp, horizontal = 32.dp)
                        .heightIn(min = 103.dp) // height를 heightIn으로 변경하여 유연성 확보
                        .width(264.dp)
                        .background(
                            color = OpacityColor.blackSmallColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .align(Alignment.Center),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp), // 패딩 조정
                        text = data.content,
                        style = TextComponent.BODY_1_M_14,
                        color = NeutralColor.WHITE,
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (data.tags.isNotEmpty()) {
                TagRow(
                    tags = data.tags,
                    enableAdd = false,
                    onAdd = {},
                    onRemove = {}
                )
            }
        }
    }
}

@Composable
private fun DeletedCard(data: BaseCardData.Deleted) {
    BaseCard(backgroundColor = CardDesignTokens.CardBackgroundGray) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = CardDesignTokens.ContentBoxGray,
                    shape = RoundedCornerShape(CardDesignTokens.ContentBoxRadius)
                )
                .padding(CardDesignTokens.ContentPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.ic_deleted_card),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = data.reason,
                    color = CardDesignTokens.TextDelete,
                    fontSize = 14.sp
                )
            }
        }
    }
}


// ===== 프리뷰 =====
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun CardViewPreview() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CardView(BaseCardData.Write("1", "짧은 글 예시입니다.\n스크롤 안전!", listOf("Tag1", "Tag2")))
        }
        item {
            CardView(BaseCardData.Reply("2", "sol", content = "이건 ReplyCard 예시", tags = listOf("답변", "예시")))
        }
        item {
            CardView(BaseCardData.Deleted("3", "삭제된 카드예요"))
        }
    }
}


