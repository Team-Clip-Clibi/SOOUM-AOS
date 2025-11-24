package com.phew.presentation.tag.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.phew.core_design.MediumButton.IconPrimary
import com.phew.core_design.NeutralColor
import com.phew.core_design.TextComponent
import com.phew.core_design.Warning
import com.phew.core_design.R as DesignR

@Composable
internal fun TagListItem (
    tag: String,
    tagId: Long,
    isFavorite: Boolean,
    onTagClick: (Long, String) -> Unit,
    onFavoriteClick: (Long) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onTagClick(tagId, tag) }
                ),
            text = tag,
            style = TextComponent.SUBTITLE_1_M_16,
            color = NeutralColor.GRAY_600
        )
        IconPrimary(
            icon = {
                if (isFavorite) {
                    Icon(
                        painter = painterResource(DesignR.drawable.ic_star_filled),
                        contentDescription = "favorite",
                        tint = Warning.M_YELLOW
                    )
                } else {
                    Icon(
                        painter = painterResource(DesignR.drawable.ic_star_filled),
                        contentDescription = "favorite",
                        tint = NeutralColor.GRAY_200
                    )
                }
            },
            onClick = {
                onFavoriteClick(tagId)
            }
        )
    }
}