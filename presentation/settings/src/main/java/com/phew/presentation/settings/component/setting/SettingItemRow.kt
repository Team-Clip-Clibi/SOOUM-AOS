package com.phew.presentation.settings.component.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.phew.core_design.NeutralColor
import com.phew.core_design.Primary
import com.phew.core_design.R
import com.phew.core_design.TextComponent
import com.phew.presentation.settings.model.setting.SettingItem

@Composable
internal fun SettingItemRow(
    item: SettingItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.title,
                style = TextComponent.BODY_1_M_14,
                color = NeutralColor.BLACK
            )
            
            item.subtitle?.let { subtitle ->
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = TextComponent.CAPTION_3_M_10,
                    color = NeutralColor.GRAY_400
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item.endText?.let { endText ->
                Text(
                    text = endText,
                    style = TextComponent.BODY_1_M_14,
                    color = Primary.DARK
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_right),
                contentDescription = null,
                tint = NeutralColor.GRAY_300,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}