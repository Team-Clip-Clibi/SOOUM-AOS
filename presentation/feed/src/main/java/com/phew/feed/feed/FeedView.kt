package com.phew.feed.feed

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.phew.core_design.AppBar
import com.phew.core_design.DialogComponent
import com.phew.core_design.NeutralColor
import com.phew.core_design.TextComponent
import com.phew.domain.dto.FeedCardType
import com.phew.domain.dto.Notice
import com.phew.domain.dto.Notification
import com.phew.feed.FeedUi
import com.phew.feed.NAV_HOME_FEED_INDEX
import com.phew.feed.NAV_HOME_POPULAR_INDEX
import com.phew.feed.viewModel.FeedPagingState
import com.phew.feed.viewModel.FeedType
import com.phew.feed.viewModel.HomeViewModel
import com.phew.feed.viewModel.UiState
import com.phew.presentation.feed.R
import kotlinx.coroutines.flow.debounce

// TODO : Feed Route로 바꾸면서 네비게이션 처리 필요
//@Composable
//fun FeedRoute(
//    modifier: Modifier = Modifier,
//    viewModel: HomeViewModel = hiltViewModel()
//) {
//
//}

@Composable
fun FeedView(
    viewModel: HomeViewModel,
    finish: () -> Unit,
    requestPermission: () -> Unit,
    closeDialog: () -> Unit,
    noticeClick: () -> Unit,
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

    // 리컴포지션 최적화: 페이징 상태 직접 참조
    val currentPagingState = uiState.currentPagingState

    // 무한 스크롤 감지
    val isLoadingMore = currentPagingState is FeedPagingState.LoadingMore

    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo }
            .debounce(100)
            .collect { visibleItems ->
                val lastVisibleIndex = visibleItems.lastOrNull()?.index ?: 0
                val totalItems = lazyListState.layoutInfo.totalItemsCount

                val state = currentPagingState
                if (lastVisibleIndex >= totalItems - 5 &&
                    state is FeedPagingState.Success &&
                    state.hasNextPage &&
                    totalItems > 0
                ) {
                    viewModel.loadMoreFeeds()
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = NeutralColor.GRAY_100)
            .systemBarsPadding()
    ) {
        TopLayout(
            currentTab = uiState.currentTab,
            recentClick = {
                viewModel.switchTab(FeedType.Latest)
            },
            popularClick = {
                viewModel.switchTab(FeedType.Popular)
            },
            nearClick = viewModel::checkLocationPermission,
            isTabsVisible = isTabsVisible,
            notice = notice,
            noticeClick = noticeClick,
            activate = unRead
        )

        FeedContent(
            currentPagingState = currentPagingState,
            isRefreshing = isRefreshing,
            isLoadingMore = isLoadingMore,
            onRefresh = viewModel::refreshCurrentTab,
            lazyListState = lazyListState,
            nestedScrollConnection = nestedScrollConnection,
            composition = composition,
            progress = progress,
            onClick = {
                //   TODO 상세 보기 화면으로 이동 필요
            },
            onRemoveCard = viewModel::removeFeedCard
        )
        if (uiState.shouldShowPermissionRationale) {
            DialogComponent.DefaultButtonTwo(
                title = stringResource(R.string.home_feed_dialog_location_title),
                description = stringResource(R.string.home_feed_dialog_location_content),
                buttonTextStart = stringResource(R.string.home_feed_dialog_location_negative),
                buttonTextEnd = stringResource(R.string.home_feed_dialog_location_positive),
                onClick = {
                    requestPermission()
                    closeDialog()
                },
                onDismiss = {
                    closeDialog()
                }
            )
        }
    }
}

@Composable
private fun TopLayout(
    currentTab: FeedType,
    recentClick: () -> Unit,
    popularClick: () -> Unit,
    nearClick: () -> Unit,
    isTabsVisible: Boolean,
    notice: LazyPagingItems<Notice>,
    noticeClick: () -> Unit,
    activate: LazyPagingItems<Notification>,
) {
    val selectIndex = when (currentTab) {
        FeedType.Latest -> NAV_HOME_FEED_INDEX
        FeedType.Popular -> NAV_HOME_POPULAR_INDEX
    }
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
            recentClick = recentClick,
            popularClick = popularClick,
            nearClick = nearClick,
            isTabsVisible = isTabsVisible,
            onDistanceClick = { value ->

            }
        )
    }
}

@Composable
private fun FeedContent(
    currentPagingState: FeedPagingState,
    isRefreshing: Boolean,
    isLoadingMore: Boolean,
    onRefresh: () -> Unit,
    lazyListState: LazyListState,
    nestedScrollConnection: NestedScrollConnection,
    composition: LottieComposition?,
    progress: Float,
    onClick: (String) -> Unit,
    onRemoveCard: (String) -> Unit,
) {
    when (currentPagingState) {
        is FeedPagingState.None,
        is FeedPagingState.Loading -> {
            if (isRefreshing || currentPagingState is FeedPagingState.Loading) {
                // 로딩 UI는 PullToRefresh에서 처리 또는 초기 로딩
            }
        }

        is FeedPagingState.LoadingMore -> {
            // LoadingMore일 때도 성공 상태의 데이터를 표시하고 로딩 인디케이터를 추가
            // 하지만 현재 상태에서는 기존 데이터가 없으므로 빈 화면 표시
            if (currentPagingState.existingData.isEmpty()) {
                EmptyFeedView()
            } else {
                FeedListView(
                    feedCards = currentPagingState.existingData,
                    isRefreshing = isRefreshing,
                    isLoadingMore = isLoadingMore,
                    onRefresh = onRefresh,
                    lazyListState = lazyListState,
                    nestedScrollConnection = nestedScrollConnection,
                    composition = composition,
                    progress = progress,
                    onClick = onClick,
                    onRemoveCard = onRemoveCard
                )
            }
        }

        is FeedPagingState.Success -> {
            if (currentPagingState.feedCards.isEmpty()) {
                EmptyFeedView()
            } else {
                FeedListView(
                    feedCards = currentPagingState.feedCards,
                    isRefreshing = isRefreshing,
                    isLoadingMore = isLoadingMore,
                    onRefresh = onRefresh,
                    lazyListState = lazyListState,
                    nestedScrollConnection = nestedScrollConnection,
                    composition = composition,
                    progress = progress,
                    onClick = onClick,
                    onRemoveCard = onRemoveCard
                )
            }
        }

        is FeedPagingState.Error -> {
            ErrorView(
                message = currentPagingState.message,
                onRetry = onRefresh
            )
        }
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
    feedCards: List<FeedCardType>,
    isRefreshing: Boolean,
    isLoadingMore: Boolean,
    onRefresh: () -> Unit,
    lazyListState: LazyListState,
    nestedScrollConnection: NestedScrollConnection,
    composition: LottieComposition?,
    progress: Float,
    onClick: (String) -> Unit,
    onRemoveCard: (String) -> Unit,
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
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 60.dp)
                .graphicsLayer {
                    translationY = if (isRefreshing) {
                        refreshState.distanceFraction * with(density) { refreshingOffset.toPx() }
                    } else {
                        0f
                    }
                },
            state = lazyListState
        ) {
            itemsIndexed(
                items = feedCards,
                key = { _, feedCard ->
                    when (feedCard) {
                        is FeedCardType.BoombType -> feedCard.cardId
                        is FeedCardType.AdminType -> feedCard.cardId
                        is FeedCardType.NormalType -> feedCard.cardId
                    }
                }
            ) { index, feedCard ->
                FeedUi.TypedFeedCardView(
                    feedCard = feedCard,
                    onClick = onClick,
                    onRemoveCard = onRemoveCard
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 더 로딩 중일 때 로딩 인디케이터
            if (isLoadingMore) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = TextComponent.BODY_1_M_14,
            color = NeutralColor.GRAY_400,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        androidx.compose.material3.Button(
            onClick = onRetry
        ) {
            Text("다시 시도")
        }
    }
}

private const val TAG = "FeedView"