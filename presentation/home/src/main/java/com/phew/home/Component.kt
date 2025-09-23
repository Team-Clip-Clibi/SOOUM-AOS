package com.phew.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.phew.core_design.NeutralColor
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import com.phew.core_design.TextComponent
import androidx.compose.material3.Divider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue


@Composable
fun AnimatedTabLayout(
    selectTabData: Int,
    recentClick: () -> Unit,
    popularClick: () -> Unit,
    nearClick: () -> Unit,
    isTabsVisible: Boolean,
    onDistanceClick: (Int) -> Unit
) {
    val tabItem = listOf(
        stringResource(R.string.home_feed_tab_recent_card),
        stringResource(R.string.home_feed_tab_popular_card),
        stringResource(R.string.home_feed_tab_near_card)
    )

    AnimatedVisibility(
        visible = isTabsVisible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(durationMillis = 150)
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(durationMillis = 150)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(color = NeutralColor.WHITE)
        ) {
            TabRow(
                selectedTabIndex = selectTabData,
                modifier = Modifier
                    .wrapContentWidth(align = Alignment.Start)
                    .height(56.dp)
                    .padding(start = 16.dp, end = 16.dp),
                containerColor = NeutralColor.WHITE,
                contentColor = NeutralColor.BLACK,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier
                            .tabIndicatorOffset(tabPositions[selectTabData]),
                        height = 2.dp,
                        color = NeutralColor.BLACK
                    )
                },
                divider = {}
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
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp),
                color = NeutralColor.GRAY_200
            )
            if (selectTabData == NAV_HOME_NEAR_INDEX) {
                var selectDistance by remember { mutableIntStateOf(DISTANCE_1KM) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(color = NeutralColor.WHITE)
                        .padding(start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DistanceText(
                        distance = stringResource(R.string.home_feed_1km_distance),
                        onClick = { onDistanceClick(DISTANCE_1KM) },
                        isSelect = selectDistance == DISTANCE_1KM
                    )
                    DistanceText(
                        distance = stringResource(R.string.home_feed_5km_distance),
                        onClick = { onDistanceClick(DISTANCE_5KM) },
                        isSelect = selectDistance == DISTANCE_5KM
                    )
                    DistanceText(
                        distance = stringResource(R.string.home_feed_10km_distance),
                        onClick = { onDistanceClick(DISTANCE_10KM) },
                        isSelect = selectDistance == DISTANCE_10KM
                    )
                    DistanceText(
                        distance = stringResource(R.string.home_feed_20km_distance),
                        onClick = { onDistanceClick(DISTANCE_20KM) },
                        isSelect = selectDistance == DISTANCE_20KM
                    )
                }
            }
        }
    }
}

@Composable
private fun DistanceText(distance: String, onClick: (String) -> Unit, isSelect: Boolean) {
    Text(
        text = distance,
        style = TextComponent.SUBTITLE_3_SB_14,
        color = if (isSelect) NeutralColor.BLACK else NeutralColor.GRAY_400,
        modifier = Modifier
            .width(48.dp)
            .height(37.dp)
            .padding(start = 10.dp, top = 8.dp, end = 10.dp, bottom = 8.dp)
            .clickable { onClick(distance) }
    )
}