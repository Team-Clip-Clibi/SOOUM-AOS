package com.phew.core_design.component.card

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isFinite
import androidx.compose.ui.unit.sp
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
    val CardPadding = 32.dp
    val ContentPadding = 16.dp

    // Typography
    val BodyFontSize = 14.sp
    val BodyLineHeight = 20.sp
}

enum class CardType {
    WRITE, REPLY, DELETED
}

// TODO 사용이 어렵군.. 수정해..
sealed class BaseCardData(open val id: String, open val type: CardType) {
    data class Write(
        val content: String,
        val tags: List<String> = emptyList(),
        val showAddButton: Boolean = true,
        val onContentChange: (String) -> Unit = {},
        val onContentEnter: () -> Unit = {},
        val onAddTag: (String) -> Unit = {},
        val onRemoveTag: (String) -> Unit = {},
        override val id: String = ""
    ) : BaseCardData(id, CardType.WRITE)

    data class Reply(
        val authorName: String,
        val authorProfileUrl: String? = null,
        val content: String,
        val tags: List<String> = emptyList(),
        val timeAgo: String = "",
        val hasThumbnail: Boolean = false,
        val thumbnailUri: String = "",
        override val id: String = ""
    ) : BaseCardData(id, CardType.REPLY)

    data class Deleted(
        val reason: String = "삭제된 카드예요",
        override val id: String = ""
    ) : BaseCardData(id, CardType.DELETED)
}

data class WriteCardData(
    val content: String,
    val tags: List<String> = emptyList(),
    val showAddButton: Boolean = true,
    val hasThumbnail: Boolean = false,
    val id: String = ""
)

@Composable
fun CardView(
    data: BaseCardData,
    modifier: Modifier = Modifier
) {
    when (data.type) {
        CardType.WRITE -> WriteCard(data as BaseCardData.Write, modifier)
        CardType.REPLY -> ReplyCard(data as BaseCardData.Reply, modifier)
        CardType.DELETED -> DeletedCard(data as BaseCardData.Deleted, modifier)
    }
}


@Composable
private fun BaseCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    content: @Composable BoxScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(
                minWidth = 328.dp,
                minHeight = 328.dp
            ),
        shape = RoundedCornerShape(CardDesignTokens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            content = content
        )
    }
}


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun EditableWriteContentBox(
    content: String,
    modifier: Modifier = Modifier,
    onContentChange: (String) -> Unit,
    onEnterPressed: () -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .background(
                color = OpacityColor.blackSmallColor,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        val scrollState = rememberScrollState()
        val boundedHeight = maxHeight.takeIf { it.isFinite } ?: 200.dp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 61.dp, max = boundedHeight)
                .verticalScroll(scrollState),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = content,
                onValueChange = { newValue ->
                    if (newValue.count { it == '\n' } > content.count { it == '\n' }) {
                        onEnterPressed()
                    }
                    onContentChange(newValue)
                },
                textStyle = TextComponent.BODY_1_M_14.copy(
                    color = CardDesignTokens.TextPrimary,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(CardDesignTokens.TextPrimary),
                modifier = Modifier.fillMaxWidth(),
                maxLines = Int.MAX_VALUE,
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (content.isBlank()) {
                            Text(
                                text = " ",
                                style = TextComponent.BODY_1_M_14.copy(color = CardDesignTokens.TextPrimary),
                                textAlign = TextAlign.Center
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun ReadOnlyContentBox(
    content: String,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .background(
                color = OpacityColor.blackSmallColor,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        val scrollState = rememberScrollState()
        val boundedHeight = maxHeight.takeIf { it.isFinite } ?: 200.dp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 61.dp, max = boundedHeight)
                .verticalScroll(scrollState),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = content.ifBlank { " " },
                style = TextComponent.BODY_1_M_14.copy(color = CardDesignTokens.TextPrimary),
                maxLines = Int.MAX_VALUE,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun WriteCard(
    data: BaseCardData.Write,
    modifier: Modifier = Modifier
) {
    BaseCard(
        modifier = modifier,
        backgroundColor = CardDesignTokens.CardBackgroundCyan
    ) {
        EditableWriteContentBox(
            modifier = Modifier
                .align(Alignment.Center),
            content = data.content,
            onContentChange = data.onContentChange,
            onEnterPressed = data.onContentEnter
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(60.dp), // 이 Box의 높이를 60.dp로 고정합니다.
            contentAlignment = Alignment.Center
        ) {
            // 하단 60.dp 영역의 중앙에 TagRow를 배치합니다.
            if (data.tags.isNotEmpty() || data.showAddButton) {
                TagRow(
                    tags = data.tags,
                    enableAdd = data.showAddButton,
                    onAdd = data.onAddTag,
                    onRemove = data.onRemoveTag
                )
            }
        }
    }
}

@Composable
private fun ReplyCard(
    data: BaseCardData.Reply,
    modifier: Modifier = Modifier
) {
    BaseCard(
        modifier = modifier,
        backgroundColor = CardDesignTokens.CardBackgroundCyan
    ) {
        if (data.hasThumbnail) {
            //  TODO 해당 영역 라운드 처리가 안됨 수정 필요
            Box(
                modifier = Modifier
                    .padding(10.dp)
                    .size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.matchParentSize(),
                    color = CardDesignTokens.TextPrimary,
                ) { }

                if (data.thumbnailUri.isNotBlank()) {
                    AsyncImage(
                        model = data.thumbnailUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(CardDesignTokens.CardRadius))
                    )
                }

                Box(
                    modifier = Modifier
                        .size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back_thumbnail),
                        contentDescription = "썸네일 있음",
                        tint = CardDesignTokens.TextSecondary
                    )
                }
            }
        }

        ReadOnlyContentBox(
            modifier = Modifier
                .align(Alignment.Center),
            content = data.content,
        )

        if (data.tags.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(60.dp),
                contentAlignment = Alignment.Center
            ) {
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

/**
 *  TODO 이미지 파일이 중앙 정렬이 안됨
 */
@Composable
private fun DeletedCard(
    data: BaseCardData.Deleted,
    modifier: Modifier = Modifier
) {
    BaseCard(
        modifier = modifier,
        backgroundColor = CardDesignTokens.CardBackgroundGray
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
           
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.drawable.img_no_card),
                    contentDescription = null,
                    modifier = Modifier
                        .height(130.dp)
                        .width(220.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = data.reason,
                    color = CardDesignTokens.TextDelete,
                    style = TextComponent.BODY_1_M_14.copy(color = NeutralColor.GRAY_400)
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
            CardView(BaseCardData.Write("짧은 글 예시입니다.\n스크롤 안전!", listOf("Tag1", "Tag2")))
        }
        item {
            CardView(
                BaseCardData.Reply(
                    "2",
                    "sol",
                    content = "이건 ReplyCard 예시",
                    tags = listOf("답변", "예시"),
                    hasThumbnail = true
                )
            )
        }
        item {
            CardView(BaseCardData.Deleted("삭제된 카드예요"))
        }
    }
}
