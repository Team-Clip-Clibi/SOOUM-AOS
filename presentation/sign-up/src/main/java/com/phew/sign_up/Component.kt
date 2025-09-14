package com.phew.sign_up

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.phew.core_design.NeutralColor
import com.phew.core_design.Primary
import com.phew.core_design.TextComponent

object Component {
    @Composable
    internal fun PageNumber(number: String, isSelect: Boolean = false) {
        Column(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = if (isSelect) Primary.LIGHT_2 else NeutralColor.GRAY_200,
                    shape = RoundedCornerShape(size = 100.dp)
                )
                .width(32.dp)
                .height(32.dp)
                .background(
                    color = if (isSelect) Primary.MAIN else NeutralColor.GRAY_300,
                    shape = RoundedCornerShape(size = 100.dp)
                ),
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = number,
                style = TextComponent.SUBTITLE_2_SB_14,
                color = NeutralColor.WHITE
            )
        }
    }

}