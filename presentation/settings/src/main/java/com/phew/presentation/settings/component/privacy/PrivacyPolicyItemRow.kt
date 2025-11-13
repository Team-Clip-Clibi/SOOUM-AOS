package com.phew.presentation.settings.component.privacy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.phew.core_design.NeutralColor
import com.phew.core_design.R
import com.phew.core_design.TextComponent
import com.phew.presentation.settings.model.privacy.PrivacyPolicyItem

@Composable
fun PrivacyPolicyItemRow(
    item: PrivacyPolicyItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = item.titleResId),
            style = TextComponent.BODY_1_M_14,
            color = NeutralColor.BLACK
        )
        
        Icon(
            painter = painterResource(id = R.drawable.ic_right),
            contentDescription = null,
            tint = NeutralColor.GRAY_300,
            modifier = Modifier.size(16.dp)
        )
    }
}