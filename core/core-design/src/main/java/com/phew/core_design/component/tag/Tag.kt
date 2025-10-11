package com.phew.core_design.component.tag


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phew.core_design.NeutralColor
import com.phew.core_design.Primary
import com.phew.core_design.R

object TagDesignTokens {
    // 다크 태그 (기본)
    val DarkBackground = NeutralColor.GRAY_600
    val DarkText = NeutralColor.WHITE

    // 컬러풀 태그
    val ColorfulBackground = Primary.LIGHT_1
    val ColorfulText = Primary.MAIN
    val ColorfulIconTint = Primary.MAIN

    // 사이즈
    val TagRadius = 4.dp
    val TagHeight = 32.dp
    val IconSize = 16.dp

    // 패딩
    val HorizontalPadding = 12.dp
    val VerticalPadding = 6.dp
    val IconPadding = 8.dp
}

// ===== 1. 기본 다크 태그들 =====

@Composable
fun TagAddNew(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(TagDesignTokens.TagHeight),
        shape = RoundedCornerShape(TagDesignTokens.TagRadius),
        color = TagDesignTokens.DarkBackground
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = TagDesignTokens.HorizontalPadding,
                vertical = TagDesignTokens.VerticalPadding
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "+",
                color = TagDesignTokens.DarkText,
                fontSize = 14.sp
            )
            Text(
                text = "태그 추가",
                color = TagDesignTokens.DarkText,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun TagFocus(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(TagDesignTokens.TagHeight),
        shape = RoundedCornerShape(TagDesignTokens.TagRadius),
        color = TagDesignTokens.DarkBackground
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = TagDesignTokens.HorizontalPadding,
                vertical = TagDesignTokens.VerticalPadding
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "#",
                color = TagDesignTokens.DarkText,
                fontSize = 14.sp
            )
            Text(
                text = "|",
                color = TagDesignTokens.DarkText,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun TagTyping(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    Surface(
        modifier = modifier.height(TagDesignTokens.TagHeight),
        shape = RoundedCornerShape(TagDesignTokens.TagRadius),
        color = TagDesignTokens.DarkBackground
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = TagDesignTokens.HorizontalPadding,
                vertical = TagDesignTokens.VerticalPadding
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "#",
                color = TagDesignTokens.DarkText,
                fontSize = 14.sp
            )

            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .widthIn(min = 40.dp, max = 120.dp)
                    .focusRequester(focusRequester)
                    .onFocusChanged { isFocused = it.isFocused },
                textStyle = TextStyle(
                    color = TagDesignTokens.DarkText,
                    fontSize = 14.sp
                ),
                cursorBrush = SolidColor(TagDesignTokens.DarkText),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
fun TagInput(
    text: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(TagDesignTokens.TagHeight),
        shape = RoundedCornerShape(TagDesignTokens.TagRadius),
        color = TagDesignTokens.DarkBackground
    ) {
        Row(
            modifier = Modifier.padding(
                start = TagDesignTokens.HorizontalPadding,
                end = TagDesignTokens.IconPadding,
                top = TagDesignTokens.VerticalPadding,
                bottom = TagDesignTokens.VerticalPadding
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "#",
                color = TagDesignTokens.DarkText,
                fontSize = 14.sp
            )
            Text(
                text = text,
                color = TagDesignTokens.DarkText,
                fontSize = 14.sp
            )
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(TagDesignTokens.IconSize)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "태그 제거",
                    tint = TagDesignTokens.DarkText,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun TagDefault(
    text: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(TagDesignTokens.TagHeight),
        shape = RoundedCornerShape(TagDesignTokens.TagRadius),
        color = TagDesignTokens.DarkBackground
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = TagDesignTokens.HorizontalPadding,
                vertical = TagDesignTokens.VerticalPadding
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "#",
                color = TagDesignTokens.DarkText,
                fontSize = 14.sp
            )
            Text(
                text = text,
                color = TagDesignTokens.DarkText,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun TagNumber(
    text: String,
    number: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(TagDesignTokens.TagHeight),
        shape = RoundedCornerShape(TagDesignTokens.TagRadius),
        color = TagDesignTokens.DarkBackground
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = TagDesignTokens.HorizontalPadding,
                vertical = TagDesignTokens.VerticalPadding
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painterResource(R.drawable.ic_hash_stoke),
                contentDescription = "태그 제거",
                tint = Primary.MAIN,
                modifier = Modifier.size(14.dp)
            )

            Text(
                text = "$text $number",
                color = TagDesignTokens.DarkText,
                fontSize = 14.sp
            )
        }
    }
}

// ===== 2. 컬러풀 태그 =====

@Composable
fun TagColorful(
    text: String,
    iconContent: @Composable (() -> Unit)? = null,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(TagDesignTokens.TagHeight),
        shape = RoundedCornerShape(TagDesignTokens.TagRadius),
        color = TagDesignTokens.ColorfulBackground
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = TagDesignTokens.HorizontalPadding,
                vertical = TagDesignTokens.VerticalPadding
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (iconContent != null) {
                Box(modifier = Modifier.size(TagDesignTokens.IconSize)) {
                    iconContent()
                }
            }
            Text(
                text = text,
                color = TagDesignTokens.ColorfulText,
                fontSize = 14.sp
            )
        }
    }
}

// ===== 프리뷰 =====

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun TagAddRemoveDynamicallyPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Add & Remove Dynamically",
            fontSize = 20.sp,
            color = Primary.MAIN
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Add new",
                    fontSize = 12.sp,
                    color = NeutralColor.GRAY_500,
                    modifier = Modifier.width(80.dp)
                )
                TagAddNew(onClick = {})
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Focus",
                    fontSize = 12.sp,
                    color = NeutralColor.GRAY_500,
                    modifier = Modifier.width(80.dp)
                )
                TagFocus()
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Typing",
                    fontSize = 12.sp,
                    color = NeutralColor.GRAY_500,
                    modifier = Modifier.width(80.dp)
                )
                var typingText by remember { mutableStateOf("Tag") }
                TagTyping(text = typingText, onTextChange = { typingText = it })
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Input",
                    fontSize = 12.sp,
                    color = NeutralColor.GRAY_500,
                    modifier = Modifier.width(80.dp)
                )
                TagInput(text = "Tag", onRemove = {})
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Default",
                    fontSize = 12.sp,
                    color = NeutralColor.GRAY_500,
                    modifier = Modifier.width(80.dp)
                )
                TagDefault(text = "Tag")
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Number",
                    fontSize = 12.sp,
                    color = NeutralColor.GRAY_500,
                    modifier = Modifier.width(80.dp)
                )
                TagNumber(text = "Tag", number = "123")
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun TagColorfulPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Colorful Tag",
            fontSize = 20.sp,
            color = Primary.MAIN
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TagColorful(
                text = "Distance",
                iconContent = {
                    Icon(
                        imageVector = Icons.Default.Close, // 실제로는 Distance 아이콘 사용
                        contentDescription = null,
                        tint = TagDesignTokens.ColorfulIconTint
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun AllTagsPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "모든 태그 타입",
            fontSize = 20.sp,
            color = Primary.MAIN
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TagAddNew(onClick = {})
            TagFocus()
            TagDefault(text = "Tag")
            TagInput(text = "Tag", onRemove = {})
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TagNumber(text = "Tag", number = "123")
            TagColorful(
                text = "Distance",
                iconContent = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = TagDesignTokens.ColorfulIconTint,
                        modifier = Modifier.size(14.dp)
                    )
                }
            )
        }
    }
}