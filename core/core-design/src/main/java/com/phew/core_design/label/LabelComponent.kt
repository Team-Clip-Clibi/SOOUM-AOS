package com.phew.core_design.label

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.phew.core_design.TextComponent

object LabelComponent {

    @Composable
    fun LabelView(text: String, textColor: Color, backgroundColor: Color, onClick: () -> Unit) {
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .height(37.dp)
                .background(color = backgroundColor, shape = RoundedCornerShape(size = 8.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                color = textColor,
                style = TextComponent.SUBTITLE_3_SB_14
            )
        }
    }
}