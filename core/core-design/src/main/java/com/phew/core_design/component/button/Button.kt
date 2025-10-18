package com.phew.core_design.component.button

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    val targetBorder = when {
        !enabled -> NeutralColor.GRAY_200
        selected -> Primary.MAIN
        else -> NeutralColor.GRAY_100
    }

    val targetText = when {
        !enabled -> NeutralColor.GRAY_300
        selected -> NeutralColor.GRAY_600
        else -> NeutralColor.GRAY_400
    }

    val backgroundColor by animateColorAsState(targetBackground)
    val borderColor by animateColorAsState(targetBorder)
    val textColor by animateColorAsState(targetText)

    Box(
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
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
