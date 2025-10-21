package com.phew.core_design

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.delay

const val blinkTime = 120

/**
 * button 중복 클래스 에러 발생시 아래의 명령어 터미널 작성
 * ./gradlew :core:core-design:clean
 * rm -rf core/core-design/build
 * ./gradlew assembleDebug
 */
object LargeButton {

    @Composable
    private fun BlinkLargeButton(
        enabled: Boolean = true,
        baseColor: Color = NeutralColor.BLACK,
        blinkColor: Color = NeutralColor.GRAY_600,
        disabledColor: Color = NeutralColor.GRAY_200,
        onClick: () -> Unit,
        content: @Composable RowScope.() -> Unit,
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(shape = RoundedCornerShape(10.dp))
                .drawBehind {
                    val color = when {
                        !enabled -> disabledColor
                        isPressed -> blinkColor
                        else -> baseColor
                    }
                    drawRect(color)
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            content()
        }
    }

    @Composable
    fun NoIconPrimary(
        buttonText: String,
        onClick: () -> Unit,
        isEnable: Boolean = true,
    ) {
        BlinkLargeButton(
            onClick = onClick,
            enabled = isEnable
        ) {
            Text(
                text = buttonText,
                style = TextComponent.TITLE_1_SB_18,
                color = if (isEnable) NeutralColor.WHITE else NeutralColor.GRAY_400
            )
        }
    }

    @Composable
    fun IconRightPrimary(
        buttonText: String,
        onClick: () -> Unit,
        @DrawableRes image: Int = R.drawable.ic_right,
        isEnable: Boolean = true,
    ) {
        BlinkLargeButton(
            onClick = onClick,
            enabled = isEnable
        ) {
            Text(
                text = buttonText,
                style = TextComponent.TITLE_1_SB_18,
                color = if (isEnable) NeutralColor.WHITE else NeutralColor.GRAY_400
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                painter = painterResource(image),
                contentDescription = "button icon",
                tint = if (isEnable) NeutralColor.WHITE else NeutralColor.GRAY_400,
                modifier = Modifier
                    .size(24.dp)
                    .padding(top = 6.dp, bottom = 6.dp)
            )
        }
    }

    @Composable
    fun IconLeftPrimary(
        buttonText: String,
        onClick: () -> Unit,
        @DrawableRes image: Int = R.drawable.ic_plus,
        isEnable: Boolean = true,
    ) {
        BlinkLargeButton(
            onClick = onClick,
            enabled = isEnable
        ) {
            Icon(
                painter = painterResource(image),
                contentDescription = "button icon",
                tint = if (isEnable) NeutralColor.WHITE else NeutralColor.GRAY_400,
                modifier = Modifier
                    .size(24.dp)
                    .padding(1.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = buttonText,
                style = TextComponent.TITLE_1_SB_18,
                color = if (isEnable) NeutralColor.WHITE else NeutralColor.GRAY_400
            )
        }
    }

    @Composable
    fun NoIconSecondary(
        buttonText: String,
        onClick: () -> Unit,
        isEnable: Boolean = true,
    ) {
        BlinkLargeButton(
            baseColor = NeutralColor.GRAY_100,
            blinkColor = NeutralColor.GRAY_200,
            disabledColor = NeutralColor.GRAY_200,
            onClick = onClick,
            enabled = isEnable
        ) {
            Text(
                text = buttonText,
                style = TextComponent.TITLE_1_SB_18,
                color = if (isEnable) NeutralColor.GRAY_600 else NeutralColor.GRAY_400
            )
        }
    }

    @Composable
    fun IconRightSecondary(
        buttonText: String,
        onClick: () -> Unit,
        @DrawableRes image: Int = R.drawable.ic_right,
        isEnable: Boolean = true,
    ) {
        BlinkLargeButton(
            baseColor = NeutralColor.GRAY_100,
            blinkColor = NeutralColor.GRAY_200,
            disabledColor = NeutralColor.GRAY_200,
            onClick = onClick,
            enabled = isEnable
        ) {
            Text(
                text = buttonText,
                style = TextComponent.TITLE_1_SB_18,
                color = if (isEnable) NeutralColor.GRAY_600 else NeutralColor.GRAY_400
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                painter = painterResource(image),
                contentDescription = "button icon",
                tint = NeutralColor.GRAY_400,
                modifier = Modifier
                    .size(24.dp)
                    .padding(top = 6.dp, bottom = 6.dp)
            )
        }
    }

    @Composable
    fun IconLeftSecondary(
        buttonText: String,
        onClick: () -> Unit,
        @DrawableRes image: Int = R.drawable.ic_plus,
        isEnable: Boolean = true,
    ) {
        BlinkLargeButton(
            baseColor = NeutralColor.GRAY_100,
            blinkColor = NeutralColor.GRAY_200,
            disabledColor = NeutralColor.GRAY_200,
            onClick = onClick,
            enabled = isEnable
        ) {
            Icon(
                painter = painterResource(image),
                contentDescription = "button icon",
                tint = NeutralColor.GRAY_400,
                modifier = Modifier
                    .size(24.dp)
                    .padding(1.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = buttonText,
                style = TextComponent.TITLE_1_SB_18,
                color = if (isEnable) NeutralColor.GRAY_600 else NeutralColor.GRAY_400
            )
        }
    }

    @Composable
    fun NoIconTertiary(
        buttonText: String,
        onClick: () -> Unit,
        isEnable: Boolean = true,
    ) {
        BlinkLargeButton(
            baseColor = NeutralColor.WHITE,
            blinkColor = NeutralColor.GRAY_100,
            disabledColor = NeutralColor.GRAY_200,
            onClick = onClick,
            enabled = isEnable
        ) {
            Text(
                text = buttonText,
                style = TextComponent.TITLE_1_SB_18,
                color = if (isEnable) NeutralColor.GRAY_500 else NeutralColor.GRAY_400
            )
        }
    }

    @Composable
    fun IconRightTertiary(
        buttonText: String,
        onClick: () -> Unit,
        @DrawableRes image: Int = R.drawable.ic_right,
        isEnable: Boolean = true,
    ) {
        BlinkLargeButton(
            baseColor = NeutralColor.WHITE,
            blinkColor = NeutralColor.GRAY_100,
            disabledColor = NeutralColor.GRAY_200,
            onClick = onClick,
            enabled = isEnable
        ) {
            Text(
                text = buttonText,
                style = TextComponent.TITLE_1_SB_18,
                color = if (isEnable) NeutralColor.GRAY_500 else NeutralColor.GRAY_400
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                painter = painterResource(image),
                contentDescription = "button icon",
                tint = NeutralColor.GRAY_300,
                modifier = Modifier
                    .size(24.dp)
                    .padding(top = 6.dp, bottom = 6.dp)
            )
        }
    }

    @Composable
    fun IconLeftTertiary(
        buttonText: String,
        onClick: () -> Unit,
        @DrawableRes image: Int = R.drawable.ic_plus,
        isEnable: Boolean = true,
    ) {
        BlinkLargeButton(
            baseColor = NeutralColor.WHITE,
            blinkColor = NeutralColor.GRAY_100,
            disabledColor = NeutralColor.GRAY_200,
            onClick = onClick,
            enabled = isEnable
        ) {
            Icon(
                painter = painterResource(image),
                contentDescription = "button icon",
                tint = NeutralColor.GRAY_300,
                modifier = Modifier
                    .size(24.dp)
                    .padding(1.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = buttonText,
                style = TextComponent.TITLE_1_SB_18,
                color = if (isEnable) NeutralColor.GRAY_500 else NeutralColor.GRAY_400
            )
        }
    }
}

object MediumButton {
    @Composable
    private fun BlinkMediumButton(
        enabled: Boolean = true,
        baseColor: Color = NeutralColor.BLACK,
        blinkColor: Color = NeutralColor.GRAY_600,
        disabledColor: Color = NeutralColor.GRAY_200,
        borderColor: Color = baseColor,
        onClick: () -> Unit,
        content: @Composable RowScope.() -> Unit,
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(shape = RoundedCornerShape(10.dp))
                .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(10.dp))
                .drawBehind {
                    val color = when {
                        !enabled -> disabledColor
                        isPressed -> blinkColor
                        else -> baseColor
                    }
                    drawRect(color)
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            content()
        }
    }

    @Composable
    fun NoIconPrimary(
        buttonText: String,
        onClick: () -> Unit,
        isEnable: Boolean = true,
    ) {
        BlinkMediumButton(
            onClick = onClick,
            enabled = isEnable
        ) {
            Text(
                text = buttonText,
                style = TextComponent.SUBTITLE_1_M_16,
                color = if (isEnable) NeutralColor.WHITE else NeutralColor.GRAY_400
            )
        }
    }

    @Composable
    fun IconRightPrimary(
        buttonText: String,
        onClick: () -> Unit,
        @DrawableRes image: Int = R.drawable.ic_right,
        isEnable: Boolean = true,
    ) {
        BlinkMediumButton(
            onClick = onClick,
            enabled = isEnable
        ) {
            Text(
                text = buttonText,
                style = TextComponent.SUBTITLE_1_M_16,
                color = if (isEnable) NeutralColor.WHITE else NeutralColor.GRAY_400
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                painter = painterResource(image),
                contentDescription = "button icon",
                tint = if (isEnable) NeutralColor.WHITE else NeutralColor.GRAY_400,
                modifier = Modifier
                    .size(24.dp)
                    .padding(top = 6.dp, bottom = 6.dp)
            )
        }
    }

    @Composable
    fun IconLeftPrimary(
        buttonText: String,
        onClick: () -> Unit,
        @DrawableRes image: Int = R.drawable.ic_plus,
        isEnable: Boolean = true,
    ) {
        BlinkMediumButton(
            onClick = onClick,
            enabled = isEnable
        ) {
            Icon(
                painter = painterResource(image),
                contentDescription = "button icon",
                tint = if (isEnable) NeutralColor.WHITE else NeutralColor.GRAY_400,
                modifier = Modifier
                    .size(24.dp)
                    .padding(1.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = buttonText,
                style = TextComponent.SUBTITLE_1_M_16,
                color = if (isEnable) NeutralColor.WHITE else NeutralColor.GRAY_400
            )
        }
    }

    @Composable
    fun NoIconSecondary(
        buttonText: String,
        onClick: () -> Unit,
        isEnable: Boolean = true,
        isSelect: Boolean = true,
        baseColor: Color = NeutralColor.GRAY_100,
        blinkColor: Color = NeutralColor.GRAY_200,
        disabledColor: Color = NeutralColor.GRAY_200,
        borderColor: Color = baseColor,
        selectTextColor: Color = NeutralColor.GRAY_600,
        disEnableTextColor: Color = NeutralColor.GRAY_400,
    ) {
        BlinkMediumButton(
            baseColor = baseColor,
            blinkColor = blinkColor,
            disabledColor = disabledColor,
            borderColor = borderColor,
            onClick = onClick,
            enabled = isEnable
        ) {
            Text(
                text = buttonText,
                style = TextComponent.SUBTITLE_1_M_16,
                color = if (isEnable && isSelect) selectTextColor else disEnableTextColor
            )
        }
    }

    @Composable
    fun SelectedSecondary(
        buttonText: String,
        onClick: () -> Unit,
        isEnable: Boolean = true,
    ) {
        BlinkMediumButton(
            baseColor = Primary.LIGHT_1,
            blinkColor = Primary.DARK,
            disabledColor = NeutralColor.GRAY_100,
            borderColor = Primary.DARK,
            onClick = onClick,
            enabled = isEnable
        ) {
            Text(
                text = buttonText,
                style = TextComponent.SUBTITLE_1_M_16,
                color = if (isEnable) NeutralColor.GRAY_600 else NeutralColor.GRAY_400
            )
        }
    }

    @Composable
    fun DisabledSecondary(
        buttonText: String,
        onClick: () -> Unit,
        isEnable: Boolean = true,
    ) {
        BlinkMediumButton(
            baseColor = NeutralColor.GRAY_200,
            disabledColor = NeutralColor.GRAY_200,
            borderColor = NeutralColor.GRAY_200,
            onClick = onClick,
            enabled = isEnable
        ) {
            Text(
                text = buttonText,
                style = TextComponent.SUBTITLE_1_M_16,
                color = if (isEnable) NeutralColor.GRAY_600 else NeutralColor.GRAY_400
            )
        }
    }

    @Composable
    fun IconRightSecondary(
        buttonText: String,
        onClick: () -> Unit,
        @DrawableRes image: Int = R.drawable.ic_right,
        isEnable: Boolean = true,
    ) {
        BlinkMediumButton(
            baseColor = NeutralColor.GRAY_100,
            blinkColor = NeutralColor.GRAY_200,
            disabledColor = NeutralColor.GRAY_200,
            onClick = onClick,
            enabled = isEnable
        ) {
            Text(
                text = buttonText,
                style = TextComponent.SUBTITLE_1_M_16,
                color = if (isEnable) NeutralColor.GRAY_600 else NeutralColor.GRAY_400
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                painter = painterResource(image),
                contentDescription = "button icon",
                tint = NeutralColor.GRAY_400,
                modifier = Modifier
                    .size(24.dp)
                    .padding(top = 6.dp, bottom = 6.dp)
            )
        }
    }

    @Composable
    fun IconLeftSecondary(
        buttonText: String,
        onClick: () -> Unit,
        @DrawableRes image: Int = R.drawable.ic_plus,
        isEnable: Boolean = true,
    ) {
        BlinkMediumButton(
            baseColor = NeutralColor.GRAY_100,
            blinkColor = NeutralColor.GRAY_200,
            disabledColor = NeutralColor.GRAY_200,
            onClick = onClick,
            enabled = isEnable
        ) {
            Icon(
                painter = painterResource(image),
                contentDescription = "button icon",
                tint = NeutralColor.GRAY_400,
                modifier = Modifier
                    .size(24.dp)
                    .padding(1.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = buttonText,
                style = TextComponent.SUBTITLE_1_M_16,
                color = if (isEnable) NeutralColor.GRAY_600 else NeutralColor.GRAY_400
            )
        }
    }

    @Composable
    fun NoIconTertiary(
        buttonText: String,
        onClick: () -> Unit,
        isEnable: Boolean = true,
    ) {
        BlinkMediumButton(
            baseColor = NeutralColor.WHITE,
            blinkColor = NeutralColor.GRAY_100,
            disabledColor = NeutralColor.GRAY_200,
            onClick = onClick,
            enabled = isEnable
        ) {
            Text(
                text = buttonText,
                style = TextComponent.SUBTITLE_1_M_16,
                color = if (isEnable) NeutralColor.GRAY_500 else NeutralColor.GRAY_400
            )
        }
    }

    @Composable
    fun IconRightTertiary(
        buttonText: String,
        onClick: () -> Unit,
        @DrawableRes image: Int = R.drawable.ic_right,
        isEnable: Boolean = true,
    ) {
        BlinkMediumButton(
            baseColor = NeutralColor.WHITE,
            blinkColor = NeutralColor.GRAY_100,
            disabledColor = NeutralColor.GRAY_200,
            onClick = onClick,
            enabled = isEnable
        ) {
            Text(
                text = buttonText,
                style = TextComponent.SUBTITLE_1_M_16,
                color = if (isEnable) NeutralColor.GRAY_500 else NeutralColor.GRAY_400
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                painter = painterResource(image),
                contentDescription = "button icon",
                tint = NeutralColor.GRAY_300,
                modifier = Modifier
                    .size(24.dp)
                    .padding(top = 6.dp, bottom = 6.dp)
            )
        }
    }

    @Composable
    fun IconLeftTertiary(
        buttonText: String,
        onClick: () -> Unit,
        @DrawableRes image: Int = R.drawable.ic_plus,
        isEnable: Boolean = true,
    ) {
        BlinkMediumButton(
            baseColor = NeutralColor.WHITE,
            blinkColor = NeutralColor.GRAY_100,
            disabledColor = NeutralColor.GRAY_200,
            onClick = onClick,
            enabled = isEnable
        ) {
            Icon(
                painter = painterResource(image),
                contentDescription = "button icon",
                tint = NeutralColor.GRAY_300,
                modifier = Modifier
                    .size(24.dp)
                    .padding(1.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = buttonText,
                style = TextComponent.SUBTITLE_1_M_16,
                color = if (isEnable) NeutralColor.GRAY_500 else NeutralColor.GRAY_400
            )
        }
    }
}

object SmallButton {

    @Composable
    private fun BlinkSmallButton(
        enabled: Boolean = true,
        baseColor: Color = NeutralColor.BLACK,
        blinkColor: Color = NeutralColor.GRAY_600,
        disabledColor: Color = NeutralColor.GRAY_200,
        onClick: () -> Unit,
        content: @Composable RowScope.() -> Unit,
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .clip(shape = RoundedCornerShape(8.dp))
                .drawBehind {
                    val color = when {
                        !enabled -> disabledColor
                        isPressed -> blinkColor
                        else -> baseColor
                    }
                    drawRect(color)
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            content()
        }
    }

    @Composable
    fun NoIconPrimary(
        buttonText: String,
        onClick: () -> Unit,
        isEnable: Boolean = true,
    ) {
        BlinkSmallButton(
            onClick = onClick,
            enabled = isEnable
        ) {
            Text(
                text = buttonText,
                style = TextComponent.BODY_1_M_14,
                color = if (isEnable) NeutralColor.WHITE else NeutralColor.GRAY_400
            )
        }
    }

    @Composable
    fun NoIconSecondary(
        buttonText: String,
        onClick: () -> Unit,
        isEnable: Boolean = true,
    ) {
        BlinkSmallButton(
            baseColor = NeutralColor.GRAY_100,
            blinkColor = NeutralColor.GRAY_200,
            disabledColor = NeutralColor.GRAY_200,
            onClick = onClick,
            enabled = isEnable
        ) {
            Text(
                text = buttonText,
                style = TextComponent.BODY_1_M_14,
                color = if (isEnable) NeutralColor.GRAY_600 else NeutralColor.GRAY_400
            )
        }
    }

    @Composable
    fun NoIconTertiary(
        buttonText: String,
        onClick: () -> Unit,
        isEnable: Boolean = true,
    ) {
        BlinkSmallButton(
            baseColor = NeutralColor.WHITE,
            blinkColor = NeutralColor.GRAY_100,
            disabledColor = NeutralColor.GRAY_200,
            onClick = onClick,
            enabled = isEnable
        ) {
            Text(
                text = buttonText,
                style = TextComponent.BODY_1_M_14,
                color = if (isEnable) NeutralColor.GRAY_500 else NeutralColor.GRAY_400
            )
        }
    }
}

object SignUpAgreeButton {
    @Composable
    fun AgreeAllButton(
        text: String,
        @DrawableRes image: Int = R.drawable.ic_check,
        onClick: () -> Unit,
        isSelected: Boolean = false,
        selectColor : Color = NeutralColor.BLACK
    ) {
        var clicked by remember { mutableStateOf(false) }
        var animating by remember { mutableStateOf(false) }
        var backgroundColor by remember { mutableStateOf(NeutralColor.GRAY_100) }
        val latestOnClick by rememberUpdatedState(newValue = onClick)
        LaunchedEffect(clicked) {
            if (clicked && !animating) {
                animating = true
                backgroundColor = NeutralColor.GRAY_200
                delay(blinkTime.toLong())
                backgroundColor = NeutralColor.GRAY_100
                latestOnClick()
                animating = false
                clicked = false
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(color = backgroundColor, shape = RoundedCornerShape(10.dp))
                .clip(shape = RoundedCornerShape(10.dp))
                .clickable(
                    enabled = !animating,
                ) { clicked = true }
                .padding(start = 24.dp, end = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                painter = painterResource(image),
                contentDescription = "button icon",
                tint = if (isSelected) selectColor else NeutralColor.GRAY_400,
                modifier = Modifier
                    .size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = TextComponent.TITLE_1_SB_18,
                color = NeutralColor.GRAY_600
            )
        }
    }

    @Composable
    fun AgreeButton(
        text: String,
        @DrawableRes image: Int = R.drawable.ic_check,
        @DrawableRes endImage: Int = R.drawable.ic_right,
        onClick: () -> Unit,
        endClick : () -> Unit,
        isSelected: Boolean = false,
    ) {
        var clicked by remember { mutableStateOf(false) }
        var animating by remember { mutableStateOf(false) }
        var backgroundColor by remember { mutableStateOf(NeutralColor.WHITE) }
        val latestOnClick by rememberUpdatedState(newValue = onClick)

        LaunchedEffect(clicked) {
            if (clicked && !animating) {
                animating = true
                backgroundColor = NeutralColor.GRAY_200
                delay(blinkTime.toLong())
                backgroundColor = NeutralColor.WHITE
                latestOnClick()
                animating = false
                clicked = false
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color = backgroundColor, shape = RoundedCornerShape(10.dp))
                .clickable(enabled = !animating) { clicked = true }
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                painter = painterResource(image),
                contentDescription = null,
                tint = if (isSelected) Primary.DARK else NeutralColor.GRAY_400,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = TextComponent.SUBTITLE_1_M_16,
                color = NeutralColor.GRAY_600
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                painter = painterResource(endImage),
                contentDescription = null,
                tint = NeutralColor.GRAY_500,
                modifier = Modifier
                    .size(32.dp)
                    .padding(vertical = 8.dp)
                    .clickable { endClick() }
            )
        }
    }
}

@Composable
@Preview
private fun Preview(text: String = "Button") {
    SignUpAgreeButton.AgreeButton(
        text = text,
        onClick = {},
        isSelected = true,
        endClick = {}
    )
}
