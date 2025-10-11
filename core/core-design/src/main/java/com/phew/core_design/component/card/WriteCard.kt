package com.phew.core_design.component.card

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.phew.core_design.NeutralColor
import com.phew.core_design.OpacityColor
import com.phew.core_design.Primary
import com.phew.core_design.R

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

    // 패딩
    val CardPadding = 16.dp
    val ContentPadding = 16.dp
}

// ===== 1. WriteCard (작성 카드) =====

enum class WriteCardType {
    DEFAULT,      // 기본
    MAXIMUM,      // 텍스트 많음
    SCROLL,       // 스크롤 필요
    THUMBNAIL     // 썸네일 포함
}

data class WriteCardData(
    val id: String,
    val type: WriteCardType = WriteCardType.DEFAULT,
    val content: String,
    val tags: List<String> = emptyList(),
    val hasAddButton: Boolean = true,
    val hasThumbnail: Boolean = false
)

@Composable
fun WriteCard(
    data: WriteCardData,
    modifier: Modifier = Modifier,
    onCardClick: () -> Unit = {},
    onTagClick: (String) -> Unit = {},
    onAddTagClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(
                when (data.type) {
                    WriteCardType.MAXIMUM, WriteCardType.SCROLL -> 320.dp
                    else -> 240.dp
                }
            ),
        shape = RoundedCornerShape(CardDesignTokens.CardRadius),
        colors = CardDefaults.cardColors(
            containerColor = CardDesignTokens.CardBackgroundCyan
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(CardDesignTokens.CardPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 썸네일 아이콘 (옵션)
            if (data.hasThumbnail) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            CardDesignTokens.ContentBoxDark,
                            RoundedCornerShape(6.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painterResource(R.drawable.ic_info_filled),
                        contentDescription = "이미지 첨부됨",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 콘텐츠 박스
            WriteContentBox(
                content = data.content,
                type = data.type,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 태그 영역
            TagRow(
                tags = data.tags,
                hasAddButton = data.hasAddButton,
                onTagClick = onTagClick,
                onAddTagClick = onAddTagClick
            )
        }
    }
}

@Composable
private fun WriteContentBox(
    content: String,
    type: WriteCardType,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = CardDesignTokens.ContentBoxDark,
                shape = RoundedCornerShape(CardDesignTokens.ContentBoxRadius)
            )
            .padding(CardDesignTokens.ContentPadding),
        contentAlignment = Alignment.Center
    ) {
        val scrollState = rememberScrollState()

        Text(
            text = content,
            color = CardDesignTokens.TextPrimary,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            modifier = if (type == WriteCardType.SCROLL) {
                Modifier.verticalScroll(scrollState)
            } else {
                Modifier
            },
            maxLines = if (type == WriteCardType.SCROLL) Int.MAX_VALUE else 7,
            overflow = if (type == WriteCardType.SCROLL) TextOverflow.Visible else TextOverflow.Ellipsis
        )
    }
}

// ===== 2. DeletedCard (삭제된 카드) =====

data class DeletedCardData(
    val id: String,
    val reason: String = "커뮤니티에만 있어요"
)

@Composable
fun DeletedCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp),
        shape = RoundedCornerShape(CardDesignTokens.CardRadius),
        colors = CardDefaults.cardColors(
            containerColor = CardDesignTokens.CardBackgroundGray
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(CardDesignTokens.CardPadding),
            contentAlignment = Alignment.Center
        ) {
            // 콘텐츠 박스
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
                AsyncImage(
                    model = R.drawable.ic_deleted_card,
                    contentDescription = "profile image",
                    modifier = Modifier.fillMaxSize()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "삭제된 카드에요",
                    color = CardDesignTokens.TextDelete,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// ===== 3. ReplyCard (답변 카드) =====

data class ReplyCardData(
    val id: String,
    val authorName: String,
    val authorProfileUrl: String? = null,
    val content: String,
    val tags: List<String> = emptyList(),
    val timeAgo: String = "방금 전"
)

@Composable
fun ReplyCard(
    data: ReplyCardData,
    modifier: Modifier = Modifier,
    onCardClick: () -> Unit = {},
    onTagClick: (String) -> Unit = {}
) {
    Card(
        onClick = onCardClick,
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp),
        shape = RoundedCornerShape(CardDesignTokens.CardRadius),
        colors = CardDefaults.cardColors(
            containerColor = CardDesignTokens.CardBackgroundCyan
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(CardDesignTokens.CardPadding)
        ) {
            // 콘텐츠 박스
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(
                        color = CardDesignTokens.ContentBoxDark,
                        shape = RoundedCornerShape(CardDesignTokens.ContentBoxRadius)
                    )
                    .padding(CardDesignTokens.ContentPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = data.content,
                    color = CardDesignTokens.TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 태그 영역
            if (data.tags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    data.tags.forEach { tag ->
                        TagChip(text = tag, onClick = { onTagClick(tag) })
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

        }
    }
}

// ===== 4. TagAddCard (태그 추가 카드) =====

data class TagAddCardData(
    val id: String,
    val selectedTags: List<String> = emptyList(),
    val maxTags: Int = 5
)

@Composable
fun TagAddCard(
    data: TagAddCardData,
    modifier: Modifier = Modifier,
    onTagRemove: (String) -> Unit = {},
    onAddMoreClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp),
        shape = RoundedCornerShape(CardDesignTokens.CardRadius),
        colors = CardDefaults.cardColors(
            containerColor = CardDesignTokens.CardBackgroundCyan
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(CardDesignTokens.CardPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 상단: 선택된 태그들
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "태그 선택 (${data.selectedTags.size}/${data.maxTags})",
                    color = CardDesignTokens.TextPrimary,
                    fontSize = 14.sp,
                )

                // 선택된 태그 목록 (가로 스크롤)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    data.selectedTags.forEach { tag ->
                        RemovableTagChip(
                            text = tag,
                            onRemove = { onTagRemove(tag) }
                        )
                    }
                }
            }

            // 하단: 태그 추가 버튼
            if (data.selectedTags.size < data.maxTags) {
                Button(
                    onClick = onAddMoreClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CardDesignTokens.ContentBoxDark
                    ),
                    shape = RoundedCornerShape(CardDesignTokens.ContentBoxRadius)
                ) {
                    Text(
                        text = "+ 태그 추가하기",
                        color = CardDesignTokens.TextPrimary,
                        fontSize = 14.sp
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            CardDesignTokens.ContentBoxDark,
                            RoundedCornerShape(CardDesignTokens.ContentBoxRadius)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "최대 ${data.maxTags}개까지 선택 가능합니다",
                        color = CardDesignTokens.TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// ===== 공통 컴포넌트 =====

@Composable
private fun TagRow(
    tags: List<String>,
    hasAddButton: Boolean,
    onTagClick: (String) -> Unit,
    onAddTagClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { tag ->
            TagChip(text = tag, onClick = { onTagClick(tag) })
        }

        if (hasAddButton) {
            AddTagButton(onClick = onAddTagClick)
        }
    }
}

@Composable
private fun TagChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(CardDesignTokens.TagRadius),
        color = CardDesignTokens.TagBackground
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painterResource(R.drawable.ic_hash_stoke),
                contentDescription = "태그 제거",
                tint = CardDesignTokens.TextBackTint,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = text,
                color = CardDesignTokens.TextPrimary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun RemovableTagChip(
    text: String,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(CardDesignTokens.TagRadius),
        color = CardDesignTokens.TagBackground
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "#",
                color = CardDesignTokens.TextPrimary,
                fontSize = 12.sp
            )
            Text(
                text = text,
                color = CardDesignTokens.TextPrimary,
                fontSize = 12.sp
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(16.dp)
            ) {
                Icon(
                    painterResource(R.drawable.ic_delete),
                    contentDescription = "태그 제거",
                    tint = CardDesignTokens.TextBackTint,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun AddTagButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(CardDesignTokens.TagRadius),
        color = CardDesignTokens.TagBackground
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "+",
                color = CardDesignTokens.TextPrimary,
                fontSize = 12.sp
            )
            Text(
                text = "태그 추가",
                color = CardDesignTokens.TextPrimary,
                fontSize = 12.sp
            )
        }
    }
}

// ===== 프리뷰 =====

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun AllCardsPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("WriteCard - Default", fontSize = 12.sp, color = Color.Gray)
        WriteCard(
            data = WriteCardData(
                id = "1",
                type = WriteCardType.DEFAULT,
                content = "솔에서 편안한 이야기 나눠요",
                tags = listOf("Tag"),
                hasAddButton = true
            )
        )

        Text("WriteCard - Maximum", fontSize = 12.sp, color = Color.Gray)
        WriteCard(
            data = WriteCardData(
                id = "2",
                type = WriteCardType.MAXIMUM,
                content = "요즘 회사에서 제은 실수와 래도 나닌 크게 혼나는 것 같아요.\n그냥 편안한 마음으로 듣고 실천하며,\n그게 제 이영에 이어졌거나,\n실수하는 게 어쩐 큰 문 만천다",
                tags = listOf("Tag"),
                hasAddButton = true
            )
        )

        Text("WriteCard - Thumbnail", fontSize = 12.sp, color = Color.Gray)
        WriteCard(
            data = WriteCardData(
                id = "3",
                type = WriteCardType.DEFAULT,
                content = "임상",
                tags = listOf("Tag", "Tag", "Tag", "Tag", "Tag"),
                hasAddButton = false,
                hasThumbnail = true
            )
        )

        Text("DeletedCard", fontSize = 12.sp, color = Color.Gray)
        DeletedCard( )

        Text("ReplyCard", fontSize = 12.sp, color = Color.Gray)
        ReplyCard(
            data = ReplyCardData(
                id = "5",
                authorName = "soaum",
                content = "안녕하세요. 솔이 다니는 서비스가 되기 위해 여러분의 의견을 듣고자 합니다.",
                tags = listOf("Tag", "Tag", "Tag", "Tag", "Tag"),
                timeAgo = "방금 전"
            )
        )

        Text("TagAddCard - Empty", fontSize = 12.sp, color = Color.Gray)
        TagAddCard(
            data = TagAddCardData(
                id = "6",
                selectedTags = emptyList()
            )
        )

        Text("TagAddCard - With Tags", fontSize = 12.sp, color = Color.Gray)
        TagAddCard(
            data = TagAddCardData(
                id = "7",
                selectedTags = listOf("일상", "회사", "고민")
            )
        )

        Text("TagAddCard - Full", fontSize = 12.sp, color = Color.Gray)
        TagAddCard(
            data = TagAddCardData(
                id = "8",
                selectedTags = listOf("일상", "회사", "고민", "연애", "취미"),
                maxTags = 5
            )
        )
    }
}