package com.phew.core_design.component.tag

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phew.core_design.NeutralColor
import com.phew.core_design.OpacityColor
import com.phew.core_design.Primary
import com.phew.core_design.R
import com.phew.core_design.TextComponent
import com.phew.core_design.component.tag.TagDesignTokens.IconAndTextPadding

object TagPolicy {
    const val MIN_LENGTH = 1
    const val MAX_LENGTH = 15

    fun sanitize(input: String): String {
        val filtered = input.filterNot { it.isWhitespace() }
        return if (filtered.length <= MAX_LENGTH) filtered else filtered.take(MAX_LENGTH)
    }

    fun isValid(input: String): Boolean {
        val length = input.length
        return length in MIN_LENGTH..MAX_LENGTH
    }
}

object TagDesignTokens {

    val BackgroundColor = OpacityColor.blackSmallColor
    val BackgroundNumberColor = NeutralColor.WHITE
    val TextTintColor = NeutralColor.WHITE
    val NumberTextTinColor = NeutralColor.GRAY_600

    // 컬러풀 태그
    val ColorfulBackground = Primary.LIGHT_1
    val ColorfulText = Primary.DARK
    val ColorfulIconTint = Primary.MAIN

    // 아이콘 색상
    val IconTint = NeutralColor.GRAY_400

    // 사이즈
    val TagRadius = 8.dp
    val TagColorfulRadius = 20.dp
    val TagHeight = 28.dp
    val IconSize = 16.dp

    // 패딩
    val HorizontalPadding = 8.dp
    val ColorfulHorizontalPadding = 4.dp
    val VerticalPadding = 6.dp
    val IconPadding = 8.dp
    val IconAndTextPadding = 2.dp
}


enum class TagState {
    AddNew,    // + 태그 추가 버튼
    Focus,     // 포커스 활성 상태 (입력 준비)
    Typing,    // 텍스트 입력 중
    Input,     // 입력 완료 후
    Default,   // 기본 카드 보기 화면
    Number     // 추천 태그 (숫자 포함)
}

/**
 *  사용
 *  Tag(
 *     state = currentState,
 *     text = tagText,
 *     onTextChange = { tagText = it },
 *     onComplete = { /* 태그 완료 */ },
 *     onRemove = { /* 태그 제거 */ }
 * )
 */
@Composable
fun Tag(
    state: TagState,
    text: String = "",
    number: String = "",
    numberValue: String = number,
    numberUnit: String = "",
    onTextChange: (String) -> Unit = {},
    onComplete: () -> Unit = {},
    onRemove: () -> Unit = {},
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    requestFocusKey: Int = 0,
    showRemoveIcon: Boolean = false,
    onInputFocusChanged: (Boolean) -> Unit = {}
) {
    when (state) {
        TagState.AddNew -> TagAddNew(onClick = onClick, modifier = modifier)

        TagState.Focus,
        TagState.Typing,
        TagState.Input -> TagInputField(
            text = text,
            onTextChange = onTextChange,
            onComplete = onComplete,
            onRemove = onRemove,
            modifier = modifier,
            requestFocusKey = requestFocusKey,
            onFocusChanged = onInputFocusChanged
        )

        TagState.Default -> TagDefault(
            text = text,
            showRemoveIcon = showRemoveIcon,
            onRemove = onRemove,
            onClick = onClick,
            modifier = modifier
        )

        TagState.Number -> TagNumber(
            text = text, 
            number = number, 
            numberValue = numberValue,
            numberUnit = numberUnit,
            onClick = onClick, 
            modifier = modifier
        )
    }
}

@Composable
internal fun TagRow(
    tags: List<String>,
    enableAdd: Boolean,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
    shouldFocus: Boolean = false,
    onFocusHandled: () -> Unit = {},
    currentInput: String = "",
    onInputChange: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var input by remember { mutableStateOf(currentInput) }
    var state by remember { mutableStateOf(TagState.AddNew) }
    val focusHandled by rememberUpdatedState(onFocusHandled)
    var focusTrigger by remember { mutableStateOf(0) }
    var awaitingFocus by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // currentInput과 동기화
    LaunchedEffect(currentInput) {
        input = currentInput
    }

    LaunchedEffect(shouldFocus) {
        if (shouldFocus) {
            state = TagState.Focus
            awaitingFocus = true
            focusTrigger++
            focusHandled()
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    // 입력 상태가 변경될 때 스크롤 처리
    LaunchedEffect(state) {
        if (state == TagState.Focus || state == TagState.Typing) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    // 입력 텍스트가 변경될 때마다 스크롤 처리
    LaunchedEffect(input) {
        if (state == TagState.Typing || state == TagState.Focus) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    val startPadding by remember(scrollState.value) {
        derivedStateOf { if (scrollState.value > 0) 0.dp else 16.dp }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(start = startPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { tag ->
            Tag(
                state = TagState.Default,
                text = tag,
                showRemoveIcon = enableAdd,
                onRemove = { onRemove(tag) },
                onClick = { onRemove(tag) }
            )
        }

        if (enableAdd) {
            Tag(
                state = state,
                text = input,
                onTextChange = {
                    val sanitized = TagPolicy.sanitize(it)
                    if (input != sanitized) {
                        input = sanitized
                        onInputChange(sanitized)
                    }
                    state = if (sanitized.isBlank()) TagState.Focus else TagState.Typing
                },
                onComplete = {
                    val candidate = TagPolicy.sanitize(input)
                    if (TagPolicy.isValid(candidate)) {
                        onAdd(candidate)
                        input = ""
                        onInputChange("")
                        state = TagState.Focus
                        awaitingFocus = true
                        focusTrigger++
                    }
                },
                onRemove = {
                    input = ""
                    onInputChange("")
                    state = TagState.AddNew
                },
                onClick = {
                    state = TagState.Focus
                    awaitingFocus = true
                    focusTrigger++
                },
                requestFocusKey = focusTrigger,
                onInputFocusChanged = { focused ->
                    if (!focused) {
                        if (!awaitingFocus) {
                            input = ""
                            state = TagState.AddNew
                        }
                    } else {
                        awaitingFocus = false
                    }
                }
            )
        }
    }
}


@Composable
private fun TagAddNew(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(TagDesignTokens.TagHeight),
        shape = RoundedCornerShape(TagDesignTokens.TagRadius),
        color = TagDesignTokens.BackgroundColor
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = TagDesignTokens.HorizontalPadding,
                vertical = TagDesignTokens.VerticalPadding
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(IconAndTextPadding)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_plus),
                contentDescription = "태그 추가",
                tint = TagDesignTokens.TextTintColor
            )
            Text(
                text = stringResource(R.string.tag_add_new),
                color = TagDesignTokens.TextTintColor,
                style = TextComponent.CAPTION_2_M_12
            )
        }
    }
}


@Composable
private fun TagInputField(
    text: String,
    onTextChange: (String) -> Unit,
    onComplete: () -> Unit = {},
    onRemove: () -> Unit = {},
    modifier: Modifier = Modifier,
    requestFocusKey: Int = 0,
    onFocusChanged: (Boolean) -> Unit = {}
) {
    var isFocused by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // 커서 깜빡임 애니메이션
    val cursorAlpha by rememberInfiniteTransition(label = "cursor").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor"
    )

    LaunchedEffect(requestFocusKey) {
        if (requestFocusKey > 0) {
            focusRequester.requestFocus()
            isFocused = true
            onFocusChanged(true)
        }
    }

    Surface(
        modifier = modifier.height(TagDesignTokens.TagHeight),
        shape = RoundedCornerShape(TagDesignTokens.TagRadius),
        color = TagDesignTokens.BackgroundColor
    ) {
        Row(
            modifier = Modifier
                .padding(
                    horizontal = TagDesignTokens.HorizontalPadding,
                    vertical = TagDesignTokens.VerticalPadding
                )
                .clickable { // Text 클릭 시 포커스 요청
                    focusRequester.requestFocus()
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(IconAndTextPadding)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_hash_stoke),
                contentDescription = "태그 입력",
                tint = TagDesignTokens.IconTint,
                modifier = Modifier.size(14.dp)
            )

            // 화면에 보이는 텍스트
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(16.dp)
            ) {
                // 입력 텍스트 + 커서
                Text(
                    text = buildString {
                        append(text)
                        if (isFocused) append(" ")
                    },
                    style = TextComponent.CAPTION_2_M_12.copy(
                        color = TagDesignTokens.TextTintColor
                    )
                )

                if (isFocused) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(14.dp)
                            .alpha(cursorAlpha)
                            .background(TagDesignTokens.TextTintColor)
                    )
                }
            }

            if (isCompleted || (text.isNotEmpty() && !isFocused)) {
                IconButton(
                    onClick = {
                        isCompleted = false
                        onTextChange("")
                        onRemove()
                    },
                    modifier = Modifier.size(16.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = "태그 제거",
                        tint = TagDesignTokens.IconTint,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }

    BasicTextField(
        value = text,
        onValueChange = { raw ->
            val sanitized = TagPolicy.sanitize(raw)
            if (sanitized != text || raw != text) {
                onTextChange(sanitized)
            }
        },
        modifier = Modifier
            .focusRequester(focusRequester)
            .onFocusChanged {
                isFocused = it.isFocused
                onFocusChanged(it.isFocused)
                if (it.isFocused) {
                    isCompleted = false
                } else {
                    // 포커스가 해제되면 키보드를 숨김
                    keyboardController?.hide()
                    if (text.isNotEmpty()) {
                        isCompleted = true
                    }
                }
            }
            .alpha(0f)
            .size(1.dp),
        textStyle = TextComponent.CAPTION_2_M_12.copy(color = TagDesignTokens.TextTintColor),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = {
                if (text.isNotEmpty()) {
                    isCompleted = true
                    onComplete()
                    isCompleted = false
                }
            }
        )
    )
}



@Composable
private fun TagDefault(
    text: String,
    showRemoveIcon: Boolean = false,
    onRemove: () -> Unit = {},
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(TagDesignTokens.TagHeight),
        shape = RoundedCornerShape(TagDesignTokens.TagRadius),
        color = TagDesignTokens.BackgroundColor
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = TagDesignTokens.HorizontalPadding,
                vertical = TagDesignTokens.VerticalPadding
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(IconAndTextPadding)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_hash_stoke),
                contentDescription = "태그 입력",
                tint = TagDesignTokens.IconTint,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                color = TagDesignTokens.TextTintColor,
                style = TextComponent.CAPTION_2_M_12.copy(color = TagDesignTokens.TextTintColor)
            )

            if (showRemoveIcon) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(16.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = "태그 제거",
                        tint = TagDesignTokens.IconTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TagNumber(
    text: String,
    number: String,
    numberValue: String = number,
    numberUnit: String = "",
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(TagDesignTokens.TagHeight),
        shape = RoundedCornerShape(TagDesignTokens.TagRadius),
        color = TagDesignTokens.BackgroundNumberColor
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = TagDesignTokens.HorizontalPadding,
                vertical = TagDesignTokens.VerticalPadding
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(IconAndTextPadding)
        ) {
            Icon(
                painterResource(R.drawable.ic_hash_stoke),
                contentDescription = "태그 제거",
                tint = Primary.MAIN,
                modifier = Modifier.size(14.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = text,
                    color = TagDesignTokens.NumberTextTinColor,
                    style = TextComponent.CAPTION_2_M_12
                )
                Text(
                    text = " ",
                    color = TagDesignTokens.NumberTextTinColor,
                    style = TextComponent.CAPTION_2_M_12
                )
                Text(
                    text = numberValue,
                    color = TagDesignTokens.NumberTextTinColor,
                    style = TextComponent.CAPTION_2_M_12
                )
                if (numberUnit.isNotEmpty()) {
                    Text(
                        text = numberUnit,
                        color = TagDesignTokens.NumberTextTinColor.copy(alpha = 0.7f),
                        style = TextComponent.CAPTION_2_M_12.copy(fontSize = 10.sp)
                    )
                }
            }
        }
    }
}

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
        shape = RoundedCornerShape(TagDesignTokens.TagColorfulRadius),
        color = TagDesignTokens.ColorfulBackground
    ) {
        Row(
            modifier = Modifier
                .padding(
                    horizontal = TagDesignTokens.ColorfulHorizontalPadding,
                )
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(IconAndTextPadding)
        ) {
            if (iconContent != null) {
                Box(modifier = Modifier.size(TagDesignTokens.IconSize)) {
                    iconContent()
                }
            }
            Text(
                text = text,
                style = TextComponent.CAPTION_3_M_10.copy(color = TagDesignTokens.ColorfulText)
            )
        }
    }
}

// 추후 수정 필요
@Composable
fun TagList(
    tags: List<String>,
    onTagsChange: (List<String>) -> Unit
) {
    var currentInput by remember { mutableStateOf("") }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(tags) { tag ->
            Surface(
                shape = RoundedCornerShape(50),
                color = TagDesignTokens.BackgroundNumberColor
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tag,
                        style = TextComponent.CAPTION_2_M_12.copy(color = TagDesignTokens.TextTintColor)
                    )
                    IconButton(
                        onClick = { onTagsChange(tags - tag) },
                        modifier = Modifier.size(16.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete),
                            contentDescription = "삭제",
                            tint = TagDesignTokens.IconTint,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        item {
            TagInputField(
                text = currentInput,
                onTextChange = { currentInput = TagPolicy.sanitize(it) },
                onComplete = {
                    if (TagPolicy.isValid(currentInput)) {
                        onTagsChange(tags + currentInput)
                        currentInput = ""
                    }
                },
                onRemove = { currentInput = "" }
            )
        }
    }
}

// ===== 프리뷰 =====

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun TagPreview() {
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
                    "InputField",
                    fontSize = 12.sp,
                    color = NeutralColor.GRAY_500,
                    modifier = Modifier.width(80.dp)
                )
                var inputText by remember { mutableStateOf("") }
                TagInputField(
                    text = inputText,
                    onTextChange = { inputText = it },
                    onComplete = { /* 완료 처리 */ },
                    onRemove = { /* 제거 처리 */ }
                )
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

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Colorful",
                    fontSize = 12.sp,
                    color = NeutralColor.GRAY_500,
                    modifier = Modifier.width(80.dp)
                )
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
}
