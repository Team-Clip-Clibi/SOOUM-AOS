package com.phew.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.phew.core_design.NeutralColor
import androidx.compose.material3.Text
import com.phew.core_design.TextComponent

@Composable
fun AnimatedTabLayout(
    selectTabData: Int,
    recentClick: () -> Unit,
    popularClick: () -> Unit,
    nearClick: () -> Unit,
    isTabsVisible: Boolean,
) {
    val tabItem = listOf(
        stringResource(R.string.home_feed_tab_recent_card),
        stringResource(R.string.home_feed_tab_popular_card),
        stringResource(R.string.home_feed_tab_near_card)
    )

    AnimatedVisibility(
        visible = isTabsVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = 300)),
        exit = fadeOut(animationSpec = tween(durationMillis = 300))
    ) {
        TabRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(width = 1.dp, color = NeutralColor.GRAY_200)
                .background(color = NeutralColor.WHITE)
                .padding(start = 16.dp, end = 16.dp),
            selectedTabIndex = selectTabData
        ) {
            tabItem.forEachIndexed { index, title ->
                val isSelected = selectTabData == index
                Tab(
                    selected = isSelected,
                    onClick = {
                        when (index) {
                            NAV_HOME_FEED_INDEX -> recentClick()
                            NAV_HOME_POPULAR_INDEX -> popularClick()
                            NAV_HOME_NEAR_INDEX -> nearClick()
                        }
                    },
                    text = {
                        Text(
                            text = title,
                            style = TextComponent.TITLE_2_SB_16,
                            color = if (isSelected) NeutralColor.BLACK else NeutralColor.GRAY_400
                        )
                    },
                )
            }
        }
    }
}