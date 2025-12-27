package com.phew.core_design.component.card

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalTextToolbar
import androidx.compose.ui.platform.TextToolbar
import androidx.compose.ui.platform.TextToolbarStatus
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.phew.core_design.NeutralColor
import com.phew.core_design.OpacityColor
import com.phew.core_design.Primary
import com.phew.core_design.R
import com.phew.core_design.TextComponent
import com.phew.core_design.component.card.CardDesignTokens.PreviousCardImageSize
import com.phew.core_design.component.tag.TagRow
import com.phew.core_design.typography.FontTextStyle
import com.phew.core_design.typography.FontType

// FontType을 TAG FontFamily로 매핑하는 함수
@Composable
private fun getTagFontFamilyFromType(fontType: FontType?): FontFamily = 
    fontType?.getTagStyle()?.fontFamily ?: FontTextStyle.DEFAULT_TAG.fontFamily ?: FontFamily.Default

// ===== 디자인 토큰 =====
object CardDesignTokens {
    // 카드 배경색
    val CardBackgroundCyan = Primary.MAIN
    val CardBackgroundGray = NeutralColor.GRAY_100


    // 텍스트 색상
    val TextPrimary = NeutralColor.WHITE
    val TextDelete = NeutralColor.GRAY_400

    // 크기
    val CardRadius = 16.dp
    val CardPreviousRadius = 8.dp
    val PreviousCardImageSize = 40.dp
}

enum class CardType {
    WRITE, REPLY, DELETED
}

sealed class BaseCardData(open val id: String, open val type: CardType) {
    data class Write(
        val content: String,
        val isEditable: Boolean = true,
        val tags: List<String> = emptyList(),
        val backgroundResId: Int? = null,
        val backgroundUri: Uri? = null,
        val fontType: FontType? = null,
        val placeholder: String = "",
        val showAddButton: Boolean = true,
        val onContentChange: (String) -> Unit = {},
        val onContentClick: () -> Unit = {},
        val onAddTag: (String) -> Unit = {},
        val onRemoveTag: (String) -> Unit = {},
        val shouldFocusTagInput: Boolean = false,
        val onTagFocusHandled: () -> Unit = {},
        val currentTagInput: String = "",
        val onTagInputChange: (String) -> Unit = {},
        val enterClick: () -> Unit = {},
        override val id: String = ""
    ) : BaseCardData(id, CardType.WRITE)

    data class Reply(
        val previousCommentThumbnailUri: String? = null, // 대댓글에서 이전 댓글 썸네일
        val content: String,
        val tags: List<String> = emptyList(),
        val timeAgo: String = "",
        val isPreviousCard: Boolean = false,
        val hasPreviousCommentThumbnail: Boolean = false,
        val thumbnailUri: String = "",
        override val id: String = "",
        val backgroundImage: Uri? = null,
        val fontType: FontType? = null,
        val onTagClick: (String) -> Unit = { },
        val enterClick: () -> Unit = {},
    ) : BaseCardData(id, CardType.REPLY)

    data class Deleted(
        val reason: String = "삭제된 카드예요",
        override val id: String = ""
    ) : BaseCardData(id, CardType.DELETED)
}

@Composable
fun CardView(
    enterClick: () -> Unit = {},
    data: BaseCardData,
    modifier: Modifier = Modifier,
    onPreviousCardClick: () -> Unit = { },
) {
    when (data.type) {
        CardType.WRITE -> WriteCard(data as BaseCardData.Write, modifier, enterClick)
        CardType.REPLY -> ReplyCard(
            data as BaseCardData.Reply,
            modifier,
            onPreviousCardClick,
            enterClick
        )

        CardType.DELETED -> DeletedCard(data as BaseCardData.Deleted, modifier)
    }
}


@Composable
private fun BaseCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    elevation: Dp = 2.dp,
    minimumHeight: Dp = 328.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(
                minWidth = 328.dp, minHeight = minimumHeight
            ),
        shape = RoundedCornerShape(CardDesignTokens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            content = content
        )
    }
}


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun EditableWriteContentBox(
    modifier: Modifier = Modifier,
    content: String,
    placeholder: String = "",
    onContentChange: (String) -> Unit,
    onContentClick: () -> Unit,
    fontType: FontType?,
    isEditable: Boolean,
    onEnterClick: () -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .background(
                color = OpacityColor.blackSmallColor, shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                enabled = isEditable,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onContentClick
            ),
        contentAlignment = Alignment.Center
    ) {
        val scrollState = rememberScrollState()
        var isFocused by remember { mutableStateOf(false) }

        if (isEditable) {
            LaunchedEffect(content) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
        }

        val textStyle = fontType?.getCardStyle() ?: TextComponent.BODY_1_M_14

        val maxHeight = with(androidx.compose.ui.platform.LocalDensity.current) {
            val verticalPadding = 40.dp // 20dp top + 20dp bottom
            (textStyle.lineHeight.toDp() * 8) + verticalPadding
        }

        CompositionLocalProvider(LocalTextToolbar provides DisabledTextToolbar) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 61.dp, max = maxHeight)
                    .padding(horizontal = 24.dp, vertical = 20.dp)
                    .verticalScroll(scrollState),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = content,
                    onValueChange = onContentChange,
                    enabled = isEditable,
                    textStyle = textStyle.copy(
                        color = CardDesignTokens.TextPrimary,
                        textAlign = TextAlign.Center
                    ),
                    cursorBrush = SolidColor(CardDesignTokens.TextPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (isEditable) isFocused = focusState.isFocused
                        },
                    maxLines = Int.MAX_VALUE,
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (content.isBlank() && !isFocused && isEditable) {
                                Text(
                                    text = placeholder,
                                    style = textStyle.copy(color = NeutralColor.WHITE),
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
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun ReadOnlyContentBox(
    content: String,
    modifier: Modifier = Modifier,
    fontType: FontType? = null
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .background(
                color = OpacityColor.blackSmallColor, shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        val scrollState = rememberScrollState()

        val textStyle = fontType?.getCardStyle()?.copy(color = CardDesignTokens.TextPrimary) 
            ?: TextComponent.BODY_1_M_14.copy(color = CardDesignTokens.TextPrimary)

        val maxHeight = with(androidx.compose.ui.platform.LocalDensity.current) {
            val verticalPadding = 40.dp // 20dp top + 20dp bottom
            (textStyle.lineHeight.toDp() * 8) + verticalPadding
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 61.dp, max = maxHeight)
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(scrollState),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = content.ifBlank { " " },
                style = textStyle,
                maxLines = Int.MAX_VALUE,
                overflow = TextOverflow.Clip,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun WriteCard(
    data: BaseCardData.Write,
    modifier: Modifier = Modifier,
    enterClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1f),
        shape = RoundedCornerShape(CardDesignTokens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, NeutralColor.GRAY_100)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val backgroundModifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(CardDesignTokens.CardRadius))

            when {
                data.backgroundUri != null -> {
                    AsyncImage(
                        model = data.backgroundUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = backgroundModifier
                    )
                }

                data.backgroundResId != null -> {
                    Image(
                        painter = painterResource(id = data.backgroundResId),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = backgroundModifier
                    )
                }

                else -> {
                    Box(
                        modifier = backgroundModifier.background(CardDesignTokens.CardBackgroundCyan)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                // 중앙 컨텐츠 영역 - Box로 중앙 정렬
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    EditableWriteContentBox(
                        modifier = Modifier.fillMaxWidth(),
                        content = data.content,
                        placeholder = data.placeholder,
                        onContentChange = data.onContentChange,
                        onContentClick = data.onContentClick,
                        fontType = data.fontType,
                        isEditable = data.isEditable,
                        onEnterClick = enterClick
                    )
                }

                // 하단 태그 영역
                if (data.tags.isNotEmpty() || data.showAddButton) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .align(Alignment.BottomCenter),
                        contentAlignment = Alignment.Center
                    ) {
                        TagRow(
                            tags = data.tags,
                            enableAdd = data.showAddButton,
                            onAdd = data.onAddTag,
                            onRemove = data.onRemoveTag,
                            shouldFocus = data.shouldFocusTagInput,
                            onFocusHandled = data.onTagFocusHandled,
                            currentInput = data.currentTagInput,
                            onInputChange = data.onTagInputChange,
                            fontFamily = getTagFontFamilyFromType(data.fontType),
                            enterClick = enterClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReplyCard(
    data: BaseCardData.Reply,
    modifier: Modifier = Modifier,
    onPreviewCard: () -> Unit,
    enterClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .border(1.dp, NeutralColor.GRAY_100, RoundedCornerShape(CardDesignTokens.CardRadius)),
        shape = RoundedCornerShape(CardDesignTokens.CardRadius),
        colors = CardDefaults.cardColors(containerColor = CardDesignTokens.CardBackgroundGray),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val backgroundModifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(CardDesignTokens.CardRadius))

            when {
                data.thumbnailUri.isNotEmpty() -> {
                    AsyncImage(
                        model = data.thumbnailUri,
                        contentDescription = "background image",
                        contentScale = ContentScale.Crop,
                        modifier = backgroundModifier
                    )
                }

                else -> {
                    Box(
                        modifier = backgroundModifier.background(CardDesignTokens.CardBackgroundCyan)
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                // 이전 댓글 썸네일 영역 (상단)
                if (data.isPreviousCard) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.TopStart),
                        contentAlignment = Alignment.TopStart
                    ) {
                        Box(
                            modifier = Modifier
                                .size(PreviousCardImageSize)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onPreviewCard
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (data.hasPreviousCommentThumbnail) {
                                Surface(
                                    modifier = Modifier.matchParentSize(),
                                    shape = RoundedCornerShape(CardDesignTokens.CardPreviousRadius)
                                ) {
                                    AsyncImage(
                                        model = data.previousCommentThumbnailUri,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clip(RoundedCornerShape(CardDesignTokens.CardPreviousRadius))
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(RoundedCornerShape(CardDesignTokens.CardPreviousRadius))
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_back_thumbnail),
                                    contentDescription = "이전 댓글 썸네일",
                                    tint = CardDesignTokens.TextPrimary
                                )
                            }
                        }
                    }
                }


                // 중앙 컨텐츠 영역 - Box로 중앙 정렬
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    ReadOnlyContentBox(
                        modifier = Modifier.fillMaxWidth(),
                        content = data.content,
                        fontType = data.fontType)
                }

                // 하단 태그 영역
                if (data.tags.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .align(Alignment.BottomCenter),
                        contentAlignment = Alignment.Center
                    ) {
                        TagRow(
                            tags = data.tags,
                            enableAdd = false,
                            onAdd = { },
                            onRemove = { },
                            shouldFocus = false,
                            onFocusHandled = { },
                            currentInput = "",
                            onInputChange = { },
                            fontFamily = getTagFontFamilyFromType(data.fontType),
                            onTagClick = data.onTagClick,
                            enterClick = enterClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeletedCard(
    data: BaseCardData.Deleted,
    modifier: Modifier = Modifier
) {
    BaseCard(
        modifier = modifier
            .aspectRatio(1f),
        elevation = 0.dp,
        backgroundColor = CardDesignTokens.CardBackgroundGray
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NeutralColor.GRAY_100)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .background(NeutralColor.GRAY_100)
            ) {
                Image(
                    painter = painterResource(R.drawable.img_no_card),
                    contentDescription = null,
                    modifier = Modifier
                        .height(130.dp)
                        .width(220.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                        NeutralColor.GRAY_100,
                        androidx.compose.ui.graphics.BlendMode.Multiply
                    )
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = data.reason,
                color = CardDesignTokens.TextDelete,
                style = TextComponent.BODY_1_M_14.copy(color = NeutralColor.GRAY_400),
                textAlign = TextAlign.Center
            )
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
            CardView(
                data = BaseCardData.Write(
                    content = "짧은 글 예시입니다.\n스크롤 안전!",
                    tags = listOf("Tag1", "Tag2")
                ),
                enterClick = {}
            )
        }
        item {
            CardView(
                enterClick = {}, data = BaseCardData.Reply(
                    previousCommentThumbnailUri = "2",
                    content = "이건 ReplyCard 예시",
                    tags = listOf("답변", "예시"),
                    hasPreviousCommentThumbnail = true,

                    )
            )
        }
        item {
            CardView(data = BaseCardData.Deleted("삭제된 카드예요"), enterClick = {})
        }
    }
}

private object DisabledTextToolbar : TextToolbar {
    override val status: TextToolbarStatus
        get() = TextToolbarStatus.Hidden

    override fun showMenu(
        rect: Rect,
        onCopyRequested: (() -> Unit)?,
        onPasteRequested: (() -> Unit)?,
        onCutRequested: (() -> Unit)?,
        onSelectAllRequested: (() -> Unit)?
    ) = Unit

    override fun hide() = Unit
}
