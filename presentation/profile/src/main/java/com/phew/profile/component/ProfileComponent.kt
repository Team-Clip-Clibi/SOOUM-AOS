package com.phew.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Tab
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.phew.core_design.NeutralColor
import com.phew.core_design.TextComponent
import com.phew.profile.R
import com.phew.profile.TAB_MY_COMMENT_CARD
import com.phew.profile.TAB_MY_FEED_CARD

@Composable
internal fun ProfileTab(
    selectTabData: Int = TAB_MY_FEED_CARD,
    onFeedCardClick:  () -> Unit,
    onCommentCardClick: () -> Unit,
) {
    val item = listOf(
        stringResource(R.string.profile_txt_card),
        stringResource(R.string.profile_txt_comment_card)
    )
    TabRow(
        selectedTabIndex = selectTabData,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(color = NeutralColor.WHITE),
        contentColor = NeutralColor.WHITE,
        containerColor = NeutralColor.WHITE,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectTabData]),
                height = 2.dp,
                color = NeutralColor.BLACK
            )
        },
        divider = {}
    ) {
        item.forEachIndexed { index, title ->
            val isSelected = selectTabData == index
            Tab(
                selected = isSelected,
                onClick = {
                    when (index) {
                        TAB_MY_FEED_CARD -> onFeedCardClick()
                        TAB_MY_COMMENT_CARD -> onCommentCardClick()
                    }
                },
                text = {
                    Text(
                        text = title,
                        style = TextComponent.TITLE_2_SB_16,
                        color = if (isSelected) NeutralColor.BLACK else NeutralColor.GRAY_400,
                    )
                }
            )
        }
    }
}