package com.phew.core_design.component.button

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.phew.core_design.NeutralColor
import com.phew.core_design.Primary
import com.phew.core_design.TextComponent

@Composable
fun RoundButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val targetBackground = when {
        !enabled -> NeutralColor.GRAY_100
        selected -> Primary.LIGHT_1
        else -> NeutralColor.GRAY_100
    }

    val targetText = when {
        !enabled -> NeutralColor.GRAY_300
        selected -> NeutralColor.GRAY_600
        else -> NeutralColor.GRAY_400
    }

    val backgroundColor by animateColorAsState(targetBackground)
    val textColor by animateColorAsState(targetText)

    Box(
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .then(
                if (!selected) {
                    Modifier.border(1.dp, NeutralColor.GRAY_100, RoundedCornerShape(16.dp))
                } else {
                    Modifier
                }
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextComponent.CAPTION_2_M_12,
            color = textColor
        )
    }
}


@Composable
fun IconButtons(
    enabled: Boolean = true,
    baseColor: Color = NeutralColor.WHITE,
    selectedIconTintColor: Color,
    disabledColor: Color = NeutralColor.GRAY_200,
    @DrawableRes unSelectedIconId: Int,
    @DrawableRes selectedIconId: Int,
    buttonText: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .height(44.dp)
            .width(60.dp)
            .padding(vertical = 12.dp)
            .drawBehind {
                val color = when {
                    !enabled -> disabledColor
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
    ) {
        Icon(
            painter = if(enabled) painterResource(selectedIconId) else painterResource(unSelectedIconId),
            contentDescription = "button icon",
            tint = if (enabled) selectedIconTintColor  else NeutralColor.GRAY_500,
            modifier = Modifier
                .size(20.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = buttonText,
            style = TextComponent.TITLE_1_SB_18,
            color = NeutralColor.GRAY_500
        )
    }
}