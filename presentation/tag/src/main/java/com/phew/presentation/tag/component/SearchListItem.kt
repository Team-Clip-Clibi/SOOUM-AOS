package com.phew.presentation.tag.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phew.core_design.NeutralColor
import com.phew.core_design.R
import com.phew.core_design.TextComponent

@Composable
internal fun SearchListItem(
    modifier: Modifier = Modifier,
    title: String,
    content: String,
    onClick: (String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(NeutralColor.WHITE)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onClick(title) }
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_hash_stoke),
            contentDescription = "shap",
            tint = NeutralColor.GRAY_400,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = TextComponent.BODY_1_M_14,
                color = if (isPressed) NeutralColor.GRAY_500 else NeutralColor.BLACK
            )
            Text(
                text = content,
                style = TextComponent.CAPTION_3_M_10,
                color =if (isPressed) NeutralColor.GRAY_400  else NeutralColor.GRAY_500
            )
        }
    }
}

@Preview
@Composable
private fun SearchListItemPreview() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ){
        SearchListItem(
            title = "영화",
            content = "100+",
            onClick = {}
        )

        SearchListItem(
            title = "영화1",
            content = "200+",
            onClick = {}
        )

        SearchListItem(
            title = "영화2",
            content = "300+",
            onClick = {}
        )
    }
}