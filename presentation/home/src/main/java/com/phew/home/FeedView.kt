package com.phew.home

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.phew.core_design.AppBar
import com.phew.core_design.NeutralColor
import com.phew.home.viewModel.HomeViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.phew.core_design.CardViewComponent
import com.phew.home.viewModel.UiState
import com.phew.domain.dto.FeedData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedView(viewModel: HomeViewModel, finish: () -> Unit) {
    var isTabsVisible by remember { mutableStateOf(true) }
    val uiState by viewModel.uiState.collectAsState()
    val isRefresh = uiState.refresh is UiState.Loading
    val lazyListState = rememberLazyListState()
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                isTabsVisible = available.y > 0 || lazyListState.firstVisibleItemIndex == 0
                return Offset.Zero
            }
        }
    }
    val composition by rememberLottieComposition(
       LottieCompositionSpec.RawRes(com.phew.core_design.R.raw.ic_refresh)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = isRefresh
    )
    BackHandler {
        finish()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = NeutralColor.GRAY_100)
            .systemBarsPadding()
    ) {
        TopLayout(
            recentClick = {},
            popularClick = {},
            nearClick = {},
            isTabsVisible = isTabsVisible
        )
        PullToRefreshBox(
            isRefreshing = isRefresh,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            indicator = {
                Box(
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isRefresh) {
                        LottieAnimation(
                            composition = composition,
                            progress = { progress },
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }
            }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection),
                state = lazyListState
            ) {
                val feedItems = (1..10).map {
                    FeedData(
                        location = "정자동",
                        writeTime = when (it) {
                            1 -> "1시간전"
                            2 -> "2025-09-21"
                            3 -> "2025-09-20"
                            else -> "2025-09-19"
                        },
                        commentValue = "1",
                        likeValue = "1",
                        uri = Uri.EMPTY,
                        content = "test"
                    )
                }
                items(feedItems.size) { item ->
                    CardViewComponent.FeedCardView(
                        location = feedItems[item].location,
                        writeTime = feedItems[item].writeTime,
                        commentValue = feedItems[item].commentValue,
                        likeValue = feedItems[item].likeValue,
                        uri = feedItems[item].uri,
                        content = feedItems[item].content
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun TopLayout(
    recentClick: () -> Unit,
    popularClick: () -> Unit,
    nearClick: () -> Unit,
    isTabsVisible: Boolean,
) {
    var selectIndex by remember { mutableIntStateOf(NAV_HOME_FEED_INDEX) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        AppBar.HomeAppBar(
            onClick = {},
            newAlarm = false,
        )
        AnimatedTabLayout(
            selectTabData = selectIndex,
            recentClick = {
                recentClick()
                selectIndex = NAV_HOME_FEED_INDEX
            },
            popularClick = {
                popularClick()
                selectIndex = NAV_HOME_POPULAR_INDEX
            },
            nearClick = {
                nearClick()
                selectIndex = NAV_HOME_NEAR_INDEX
            },
            isTabsVisible = isTabsVisible,
        )
    }
}


@Composable
@Preview
private fun Preview() {
    FeedView(viewModel = HomeViewModel(), finish = {})
}