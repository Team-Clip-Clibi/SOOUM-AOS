package com.phew.presentation.write.screen.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phew.core_design.AppBar
import com.phew.core_design.NeutralColor
import com.phew.core_design.R
import com.phew.core_design.TextComponent
import com.phew.core_design.theme.SooumTheme
import com.phew.presentation.write.R as WriteR

data class PollOptionUi(
    val id: Long,
    val text: String,
)

private const val REQUIRED_POLL_OPTIONS = 2
private const val MAX_POLL_OPTIONS = 4

@Composable
fun PollCreateScreen(
    options: List<PollOptionUi>,
    onOptionChange: (id: Long, text: String) -> Unit,
    onAddOption: () -> Unit,
    onRemoveOption: (id: Long) -> Unit,
    onClose: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val completeEnabled = options.size >= REQUIRED_POLL_OPTIONS &&
        options
            .take(REQUIRED_POLL_OPTIONS)
            .all { it.text.isNotBlank() }

    Column(
        modifier = modifier.background(NeutralColor.WHITE),
    ) {
        AppBar.CloseTextButtonAppBar(
            appBarText = stringResource(WriteR.string.write_poll_title),
            buttonText = stringResource(WriteR.string.write_screen_complete),
            onCloseClick = onClose,
            onButtonClick = onComplete,
            buttonEnabled = completeEnabled,
        )

        PollOptionEditor(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            options = options,
            onOptionChange = onOptionChange,
            onAddOption = onAddOption,
            onRemoveOption = onRemoveOption,
        )
    }
}

@Composable
private fun PollOptionEditor(
    options: List<PollOptionUi>,
    onOptionChange: (id: Long, text: String) -> Unit,
    onAddOption: () -> Unit,
    onRemoveOption: (id: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEachIndexed { index, option ->
            PollOptionField(
                value = option.text,
                onValueChange = { onOptionChange(option.id, it) },
                showRemove = index >= REQUIRED_POLL_OPTIONS,
                onRemove = { onRemoveOption(option.id) },
                imeAction = if (index == options.lastIndex) ImeAction.Done else ImeAction.Next,
            )
        }

        if (options.size < MAX_POLL_OPTIONS) {
            AddPollOptionButton(onClick = onAddOption)
        }
    }
}

@Composable
private fun PollOptionField(
    value: String,
    onValueChange: (String) -> Unit,
    showRemove: Boolean,
    onRemove: () -> Unit,
    imeAction: ImeAction,
) {
    val focusManager = LocalFocusManager.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(NeutralColor.GRAY_100)
                .padding(start = 20.dp, end = 24.dp),
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextComponent.SUBTITLE_1_M_16.copy(color = NeutralColor.BLACK),
            keyboardOptions = KeyboardOptions(imeAction = imeAction),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() },
            ),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = stringResource(WriteR.string.write_poll_option_input),
                            style = TextComponent.SUBTITLE_1_M_16,
                            color = NeutralColor.GRAY_500,
                        )
                    }
                    innerTextField()
                }
            },
        )

        if (showRemove) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(R.drawable.ic_subtraction_circle),
                    contentDescription = stringResource(WriteR.string.write_poll_remove_option),
                    tint = Color.Unspecified,
                )
            }
        }
    }
}

@Composable
private fun AddPollOptionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val contentColor = if (isPressed) NeutralColor.GRAY_400 else NeutralColor.GRAY_600

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .dashedRoundedBorder(color = NeutralColor.GRAY_300)
            .clip(RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            painter = painterResource(R.drawable.ic_plus),
            contentDescription = null,
            tint = contentColor,
        )
        Text(
            modifier = Modifier.padding(start = 6.dp),
            text = stringResource(WriteR.string.write_poll_add_option),
            style = TextComponent.SUBTITLE_1_M_16,
            color = contentColor,
        )
    }
}

private fun Modifier.dashedRoundedBorder(color: Color): Modifier = drawWithCache {
    val strokeWidth = 1.dp.toPx()
    val cornerRadius = 10.dp.toPx()
    val dash = 4.dp.toPx()
    val stroke = Stroke(
        width = strokeWidth,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash)),
    )

    onDrawBehind {
        drawRoundRect(
            color = color,
            cornerRadius = CornerRadius(cornerRadius),
            style = stroke,
        )
    }
}

@Composable
fun PollWritePreviewContent(
    modifier: Modifier = Modifier,
    initialOptionCount: Int = MAX_POLL_OPTIONS,
) {
    var options by remember(initialOptionCount) {
        mutableStateOf(
            List(initialOptionCount.coerceIn(REQUIRED_POLL_OPTIONS, MAX_POLL_OPTIONS)) { index ->
                PollOptionUi(
                    id = index.toLong(),
                    text = if (index < REQUIRED_POLL_OPTIONS) "항목 ${index + 1}" else "",
                )
            },
        )
    }

    PollCreateScreen(
        modifier = modifier,
        options = options,
        onOptionChange = { id, text ->
            options = options.map { if (it.id == id) it.copy(text = text) else it }
        },
        onAddOption = {
            if (options.size < MAX_POLL_OPTIONS) {
                val nextId = (options.maxOfOrNull(PollOptionUi::id) ?: 0) + 1
                options = options + PollOptionUi(nextId, "")
            }
        },
        onRemoveOption = { id ->
            options = options.filterNot { it.id == id }
        },
        onClose = {},
        onComplete = {},
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PollCreateOptionFourPreview() {
    SooumTheme {
        PollWritePreviewContent(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxHeight(),
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PollCreateInitialPreview() {
    SooumTheme {
        PollWritePreviewContent(
            modifier = Modifier.fillMaxSize(),
            initialOptionCount = REQUIRED_POLL_OPTIONS,
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PollCreateOptionThreePreview() {
    SooumTheme {
        PollWritePreviewContent(
            modifier = Modifier.fillMaxSize(),
            initialOptionCount = 3,
        )
    }
}
