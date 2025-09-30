package com.phew.home.feed

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.phew.core_design.DialogComponent
import com.phew.core_design.TextComponent
import com.phew.domain.dto.FeedData
import com.phew.domain.dto.Notice
import com.phew.domain.dto.Notification
import com.phew.home.FeedUi
import com.phew.home.NAV_HOME_FEED_INDEX
import com.phew.home.NAV_HOME_NEAR_INDEX
import com.phew.home.NAV_HOME_POPULAR_INDEX
import com.phew.home.R
import com.phew.home.viewModel.Home
import com.phew.home.viewModel.UiState


@Composable
fun FeedView(
    viewModel: HomeViewModel,
    finish: () -> Unit,
    locationPermission: () -> Unit,
    dialogDismiss: Boolean,
    closeDialog: () -> Unit,
    noticeClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val notice = viewModel.notice.collectAsLazyPagingItems()
    val unRead = viewModel.unReadNotification.collectAsLazyPagingItems()
    val isRefreshing = uiState.refresh is UiState.Loading
    val lazyListState = rememberLazyListState()
    var isTabsVisible by remember { mutableStateOf(true) }
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
        isPlaying = isRefreshing
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
            recentClick = viewModel::initTestData,
            popularClick = viewModel::initTestData,
            nearClick = viewModel::checkLocationPermission,
            isTabsVisible = isTabsVisible,
            notice = notice,
            noticeClick = noticeClick,
            activate = unRead
        )

        FeedContent(
            uiState = uiState,
            isRefreshing = isRefreshing,
            onRefresh = viewModel::refresh,
            lazyListState = lazyListState,
            nestedScrollConnection = nestedScrollConnection,
            composition = composition,
            progress = progress
        )
        if (!dialogDismiss) {
            DialogComponent.DefaultButtonTwo(
                title = stringResource(R.string.home_feed_dialog_location_title),
                description = stringResource(R.string.home_feed_dialog_location_content),
                buttonTextStart = stringResource(R.string.home_feed_dialog_location_negative),
                buttonTextEnd = stringResource(R.string.home_feed_dialog_location_positive),
                onClick = { locationPermission() },
                onDismiss = {
                    closeDialog()
                    viewModel.initTestData()
                }
            )
        }
    }
}

@Composable
private fun TopLayout(
    recentClick: () -> Unit,
    popularClick: () -> Unit,
    nearClick: () -> Unit,
    isTabsVisible: Boolean,
    notice: LazyPagingItems<Notice>,
    noticeClick: () -> Unit,
    activate: LazyPagingItems<Notification>
) {
    var selectIndex by remember { mutableIntStateOf(NAV_HOME_FEED_INDEX) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        AppBar.HomeAppBar(
            onClick = noticeClick,
            newAlarm = notice.itemCount != 0 && activate.itemCount != 0,
        )
        FeedUi.AnimatedFeedTabLayout(
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
            onDistanceClick = { value ->

            }
        )
    }
}

@Composable
private fun FeedContent(
    uiState: Home,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    lazyListState: LazyListState,
    nestedScrollConnection: NestedScrollConnection,
    composition: LottieComposition?,
    progress: Float
) {
    when {
        uiState.feedItem.isEmpty() -> EmptyFeedView()
        else -> FeedListView(
            feedItems = uiState.feedItem,
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            lazyListState = lazyListState,
            nestedScrollConnection = nestedScrollConnection,
            composition = composition,
            progress = progress
        )
    }
}

@Composable
private fun EmptyFeedView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(com.phew.core_design.R.drawable.ic_feed_empty_view),
            contentDescription = "empty view",
            modifier = Modifier
                .width(162.dp)
                .height(113.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.home_feed_no_card),
            style = TextComponent.BODY_1_M_14,
            color = NeutralColor.GRAY_400,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedListView(
    feedItems: List<FeedData>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    lazyListState: LazyListState,
    nestedScrollConnection: NestedScrollConnection,
    composition: LottieComposition?,
    progress: Float,
) {
    val refreshingOffset = 56.dp
    val refreshState = rememberPullToRefreshState()
    val density = LocalDensity.current

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = refreshState,
        indicator = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(refreshingOffset + 20.dp),
                contentAlignment = Alignment.Center
            ) {
                val lottieProgress = if (isRefreshing) {
                    progress
                } else {
                    refreshState.distanceFraction
                }
                if (isRefreshing) {
                    LottieAnimation(
                        composition = composition,
                        progress = { lottieProgress },
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
                .padding(horizontal = 16.dp)
                .graphicsLayer {
                    translationY =
                        refreshState.distanceFraction * with(density) { refreshingOffset.toPx() }
                },
            state = lazyListState
        ) {
            items(feedItems) { feedItem ->
                FeedUi.FeedCardView(
                    location = feedItem.location,
                    writeTime = feedItem.writeTime,
                    commentValue = feedItem.commentValue,
                    likeValue = feedItem.likeValue,
                    uri = feedItem.uri,
                    content = feedItem.content
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}