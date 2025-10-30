package com.phew.presentation.detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.phew.core_common.TimeUtils
import com.phew.core_design.AvatarComponent.SmallAvatar
import com.phew.core_design.NeutralColor
import com.phew.core_design.R
import com.phew.core_design.TextComponent
import com.phew.core_design.component.tag.TagColorful
import com.phew.core_design.component.tag.TagDesignTokens

@Composable
internal fun CardDetailHeader(
    modifier: Modifier = Modifier,
    profileUri: String,
    nickName: String,
    distance: String,
    createAt: String
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(2f)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SmallAvatar(url = profileUri)

            Text(
                text = nickName,
                style = TextComponent.SUBTITLE_2_SB_14
            )

            if (distance.isNotEmpty()) {
                TagColorful(
                    text = distance,
                    iconContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_location_filled),
                            contentDescription = null,
                            tint = TagDesignTokens.ColorfulIconTint
                        )
                    }
                )
            }
        }

        Text(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(end = 5.dp),
            text = TimeUtils.getRelativeTimeString(createAt = createAt),
            style = TextComponent.CAPTION_2_M_12.copy(color = NeutralColor.GRAY_400),
            textAlign = TextAlign.End
        )
    }
}

@Preview
@Composable
private fun CardDetailHeaderPreview() {
    CardDetailHeader(
        profileUri = "",
        nickName = "닉네임",
        distance = "10km",
        createAt = "2025-10-09T03:54:10.026919"
    )
}