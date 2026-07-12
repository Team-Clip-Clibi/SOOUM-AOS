package com.phew.core_design.component.button

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.phew.core_design.NeutralColor
import com.phew.core_design.R
import com.phew.core_design.TextComponent

enum class StandardLineButtonSize(
    internal val height: Dp,
    internal val cornerRadius: Dp,
    internal val horizontalPadding: Dp,
    internal val iconSpacing: Dp,
    internal val iconSize: Dp,
    internal val textStyle: TextStyle,
) {
    Large(
        height = 56.dp,
        cornerRadius = 10.dp,
        horizontalPadding = 24.dp,
        iconSpacing = 8.dp,
        iconSize = 24.dp,
        textStyle = TextComponent.TITLE_1_SB_18,
    ),
    Medium(
        height = 48.dp,
        cornerRadius = 10.dp,
        horizontalPadding = 16.dp,
        iconSpacing = 6.dp,
        iconSize = 20.dp,
        textStyle = TextComponent.SUBTITLE_1_M_16,
    ),
    Small(
        height = 32.dp,
        cornerRadius = 8.dp,
        horizontalPadding = 16.dp,
        iconSpacing = 6.dp,
        iconSize = 16.dp,
        textStyle = TextComponent.BODY_1_M_14,
    ),
}

enum class StandardLineButtonIcon {
    None,
    Left,
    Right,
}

@Composable
fun StandardLineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: StandardLineButtonSize = StandardLineButtonSize.Large,
    icon: StandardLineButtonIcon = StandardLineButtonIcon.None,
    @DrawableRes iconResId: Int? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(size.cornerRadius)
    val backgroundColor = if (enabled) NeutralColor.WHITE else NeutralColor.GRAY_100
    val contentColor = when {
        !enabled -> NeutralColor.GRAY_400
        isPressed -> NeutralColor.GRAY_400
        else -> NeutralColor.GRAY_600
    }
    val resolvedIconResId = iconResId ?: when (icon) {
        StandardLineButtonIcon.Left -> R.drawable.ic_plus
        StandardLineButtonIcon.Right -> R.drawable.ic_right
        StandardLineButtonIcon.None -> null
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(size.height)
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = NeutralColor.GRAY_300,
                shape = shape,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = size.horizontalPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon == StandardLineButtonIcon.Left && resolvedIconResId != null) {
            StandardLineButtonIconContent(
                iconResId = resolvedIconResId,
                contentColor = contentColor,
                size = size.iconSize,
            )
            Spacer(Modifier.width(size.iconSpacing))
        }

        Text(
            text = text,
            style = size.textStyle,
            color = contentColor,
        )

        if (icon == StandardLineButtonIcon.Right && resolvedIconResId != null) {
            Spacer(Modifier.width(size.iconSpacing))
            StandardLineButtonIconContent(
                iconResId = resolvedIconResId,
                contentColor = contentColor,
                size = size.iconSize,
            )
        }
    }
}

@Composable
private fun StandardLineButtonIconContent(
    @DrawableRes iconResId: Int,
    contentColor: Color,
    size: Dp,
) {
    Icon(
        painter = painterResource(iconResId),
        contentDescription = null,
        tint = contentColor,
        modifier = Modifier.size(size),
    )
}
