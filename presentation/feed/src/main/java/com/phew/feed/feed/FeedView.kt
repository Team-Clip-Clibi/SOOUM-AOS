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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.LazyPagingItems
import androidx.paging.LoadState
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.phew.core.ui.model.navigation.CardDetailArgs
import com.phew.core.ui.navigation.NavigationKeys
import com.phew.core.ui.state.SooumAppState
import com.phew.core_design.AppBar
import com.phew.core_design.DialogComponent
import com.phew.core_design.NeutralColor
import com.phew.core_design.TextComponent
import com.phew.core.ui.component.home.HomeTabType
import com.phew.domain.dto.FeedCardType
import com.phew.domain.dto.Latest
import com.phew.domain.dto.Notice
import com.phew.feed.FeedUi
import com.phew.feed.NAV_HOME_FEED_INDEX
import com.phew.feed.NAV_HOME_NEAR_INDEX
import com.phew.feed.NAV_HOME_POPULAR_INDEX
import com.phew.feed.viewModel.DistanceType
import com.phew.feed.viewModel.FeedPagingState
import com.phew.feed.viewModel.FeedType
import com.phew.feed.viewModel.FeedViewModel
import com.phew.feed.viewModel.NavigationEvent
import com.phew.feed.viewModel.UiState
import com.phew.presentation.feed.R
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.text.append
import com.phew.core.ui.R as CoreUiR


@OptIn(FlowPreview::class)
@Composable
fun FeedView(
    viewModel: FeedViewModel,
    navController: NavHostController,
    appState: SooumAppState,
    finish: () -> Unit,
    requestPermission: () -> Unit,
    closeDialog: () -> Unit,
    noticeClick: () -> Unit,
    navigateToDetail: (CardDetailArgs) -> Unit,
    webViewClick: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val feedNoticeState = uiState.feedNotification
    val latestFeedItems = viewModel.latestFeedPaging.collectAsLazyPagingItems()
    val lazyListState = rememberLazyListState()
    var hasScrolledToTop by rememberSaveable { mutableStateOf(false) }
    var previousHomeTab by rememberSaveable { mutableStateOf<HomeTabType?>(null) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentHomeTab = remember(navBackStackEntry) {
        HomeTabType.findHome(navBackStackEntry?.destination?.route)
    }
    val currentPagingState = uiState.currentPagingState
    val pagingStateForEffect by rememberUpdatedState(currentPagingState)
    val isLatestRefreshing = latestFeedItems.loadState.refresh is LoadState.Loading
    val isRefreshing = when (uiState.currentTab) {
        FeedType.Latest -> isLatestRefreshing
        else -> currentPagingState is FeedPagingState.Loading
    }
    val toolbarHeight = 56.dp
    val toolbarHeightPx = with(LocalDensity.current) { toolbarHeight.toPx() }
    val toolbarOffsetHeightPx = remember { mutableStateOf(0f) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = toolbarOffsetHeightPx.value + delta
                toolbarOffsetHeightPx.value = newOffset.coerceIn(-toolbarHeightPx, 0f)
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

    // Navigation event handling
    LaunchedEffect(viewModel) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is NavigationEvent.NavigateToDetail -> {
                    navigateToDetail(event.args)
                }
            }
        }
    }

    // Scroll to top handling from AppState
    LaunchedEffect(appState) {
        appState.feedScrollToTopEvent.collect {
            if (!lazyListState.isScrollInProgress) {
                lazyListState.animateScrollToItem(0)
                toolbarOffsetHeightPx.value = 0f
            }
        }
    }


    // Refresh handling after writing a card
    LaunchedEffect(navBackStackEntry) {
        navBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
            if (savedStateHandle.remove<Boolean>(NavigationKeys.CARD_ADDED) == true) {
                when (uiState.currentTab) {
                    FeedType.Latest -> latestFeedItems.refresh()
                    FeedType.Popular, FeedType.Distance -> viewModel.refreshCurrentTab()
                }
            }
        }
    }

    // 하단 탭 이동 시 스크롤 초기화 플래그 갱신
    LaunchedEffect(currentHomeTab) {
        if (previousHomeTab != currentHomeTab) {
            if (
                currentHomeTab == HomeTabType.FEED &&
                previousHomeTab != null &&
                previousHomeTab != HomeTabType.FEED
            ) {
                hasScrolledToTop = false
            }
            previousHomeTab = currentHomeTab
        }
    }

    LaunchedEffect(hasScrolledToTop) {
        if (!hasScrolledToTop && !lazyListState.isScrollInProgress) {
            lazyListState.animateScrollToItem(0)
            hasScrolledToTop = true
        }
    }


    val isLoadingMore = currentPagingState is FeedPagingState.LoadingMore

    LaunchedEffect(lazyListState, uiState.currentTab) {
        if (uiState.currentTab != FeedType.Latest) {
            snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo }
                .debounce(100)
                .collect { visibleItems ->
                    val lastVisibleIndex = visibleItems.lastOrNull()?.index ?: 0
                    val totalItems = lazyListState.layoutInfo.totalItemsCount

                    val state = pagingStateForEffect
                    if (lastVisibleIndex >= totalItems - 5 &&
                        state is FeedPagingState.Success &&
                        state.hasNextPage &&
                        totalItems > 0
                    ) {
                        viewModel.loadMoreFeeds()
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = NeutralColor.GRAY_100)
            .systemBarsPadding()
            .nestedScroll(nestedScrollConnection)
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
            notice = feedNoticeState is UiState.Success && feedNoticeState.data.isNotEmpty(),
            noticeClick = noticeClick,
            distanceClick = { value ->
                viewModel.switchDistanceTab(value)
            },
            selectDistance = uiState.distanceTab,
            toolbarOffsetHeightPx = toolbarOffsetHeightPx.value
        )

        FeedContent(
            currentTab = uiState.currentTab,
            currentPagingState = currentPagingState,
            latestFeedItems = latestFeedItems,
            isRefreshing = isRefreshing,
            isLoadingMore = isLoadingMore,
            onRefresh = viewModel::refreshCurrentTab,
            lazyListState = lazyListState,
            nestedScrollConnection = nestedScrollConnection,
            composition = composition,
            progress = progress,
            onClick = { cardId ->
                viewModel.navigateToDetail(cardId)
            },
            onRemoveCard = viewModel::removeFeedCard,
            feedNotice = if (feedNoticeState is UiState.Success) feedNoticeState.data else emptyList(),
            feedNoticeClick = { url ->
                webViewClick(url)
            }
        )
        if (uiState.shouldShowPermissionRationale) {
            DialogComponent.DefaultButtonTwo(
                title = stringResource(CoreUiR.string.location_permission_title),
                description = stringResource(CoreUiR.string.location_permission_description),
                buttonTextStart = stringResource(CoreUiR.string.location_permission_negative),
                buttonTextEnd = stringResource(CoreUiR.string.location_permission_positive),
                onClick = {
                    requestPermission()
                    closeDialog()
                },
                onDismiss = {
                    closeDialog()
                },
                startButtonTextColor = NeutralColor.GRAY_600
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
    notice: Boolean,
    noticeClick: () -> Unit,
    distanceClick: (DistanceType) -> Unit,
    selectDistance : DistanceType,
    toolbarOffsetHeightPx: Float,
) {
    val selectIndex = when (currentTab) {
        FeedType.Latest -> NAV_HOME_FEED_INDEX
        FeedType.Popular -> NAV_HOME_POPULAR_INDEX
        FeedType.Distance -> NAV_HOME_NEAR_INDEX
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        AppBar.HomeAppBar(
            onClick = noticeClick,
            newAlarm = notice,
        )
        FeedUi.AnimatedFeedTabLayout(
            selectTabData = selectIndex,
            recentClick = recentClick,
            popularClick = popularClick,
            nearClick = nearClick,
            onDistanceClick = { value ->
                distanceClick(value)
            },
            selectDistanceType = selectDistance,
            translationY = toolbarOffsetHeightPx
        )
    }
}

@Composable
private fun FeedContent(
    currentTab: FeedType,
    currentPagingState: FeedPagingState,
    latestFeedItems: LazyPagingItems<Latest>?,
    isRefreshing: Boolean,
    isLoadingMore: Boolean,
    onRefresh: () -> Unit,
    lazyListState: LazyListState,
    nestedScrollConnection: NestedScrollConnection,
    composition: LottieComposition?,
    progress: Float,
    onClick: (String) -> Unit,
    onRemoveCard: (String) -> Unit,
    feedNotice: List<Notice>,
    feedNoticeClick: (String) -> Unit,
) {
    if (currentTab == FeedType.Latest && latestFeedItems != null) {
        LatestFeedPagingContent(
            latestFeedItems = latestFeedItems,
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            lazyListState = lazyListState,
            nestedScrollConnection = nestedScrollConnection,
            composition = composition,
            progress = progress,
            onClick = onClick,
            onRemoveCard = onRemoveCard,
            feedNotice = feedNotice,
            feedNoticeClick = feedNoticeClick
        )
        return
    }
    
    when (currentPagingState) {
        is FeedPagingState.None,
        is FeedPagingState.Loading -> {
            // Popular/Distance 로딩 상태 - PullToRefresh에서 처리
        }

        is FeedPagingState.LoadingMore -> {
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
                    cardClick = onClick,
                    onRemoveCard = onRemoveCard,
                    feedNotice = feedNotice,
                    feedNoticeClick = feedNoticeClick
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
                    cardClick = onClick,
                    onRemoveCard = onRemoveCard,
                    feedNotice = feedNotice,
                    feedNoticeClick = feedNoticeClick
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
    cardClick: (String) -> Unit,
    onRemoveCard: (String) -> Unit,
    feedNotice: List<Notice>,
    feedNoticeClick: (String) -> Unit,
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
                        modifier = Modifier.size(44.dp)
                    )
                }
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
                .padding(start = 16.dp, end = 16.dp, bottom = 60.dp)
                .graphicsLayer {
                    translationY = if (isRefreshing) {
                        refreshState.distanceFraction * with(density) { refreshingOffset.toPx() }
                    } else {
                        0f
                    }
                },
            state = lazyListState
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                FeedUi.FeedNoticeView(
                    feedNotice = feedNotice,
                    feedNoticeClick = feedNoticeClick
                )
            }
            item {
                Spacer(modifier = Modifier.height(10.dp))
            }
            itemsIndexed(
                items = feedCards,
                key = { _, feedCard ->
                    when (feedCard) {
                        is FeedCardType.BoombType -> feedCard.cardId
                        is FeedCardType.AdminType -> feedCard.cardId
                        is FeedCardType.NormalType -> feedCard.cardId
                    }
                }
            ) { _, feedCard ->
                FeedUi.TypedFeedCardView(
                    feedCard = feedCard,
                    onClick = cardClick,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LatestFeedPagingContent(
    latestFeedItems: LazyPagingItems<Latest>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    lazyListState: LazyListState,
    nestedScrollConnection: NestedScrollConnection,
    composition: LottieComposition?,
    progress: Float,
    onClick: (String) -> Unit,
    onRemoveCard: (String) -> Unit,
    feedNotice: List<Notice>,
    feedNoticeClick: (String) -> Unit,
) {
    val isLoading = latestFeedItems.loadState.refresh is LoadState.Loading
    val appendLoadState = latestFeedItems.loadState.append
    val isAppending = appendLoadState is LoadState.Loading
    val isPagingRefreshing = isRefreshing || isLoading
    val refreshError = latestFeedItems.loadState.refresh as? LoadState.Error
    
    if (refreshError != null && latestFeedItems.itemCount == 0) {
        ErrorView(
            message = refreshError.error.message ?: stringResource(R.string.home_feed_load_error),
            onRetry = {
                latestFeedItems.retry()
            }
        )
        return
    }

    // 빈 화면 처리
    if (latestFeedItems.itemCount == 0 && !isPagingRefreshing) {
        EmptyFeedView()
        return
    }
    
    val refreshingOffset = 56.dp
    val refreshState = rememberPullToRefreshState()
    val density = LocalDensity.current
    
    PullToRefreshBox(
        isRefreshing = isPagingRefreshing,
        onRefresh = {
            latestFeedItems.refresh()
        },
        state = refreshState,
        indicator = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(refreshingOffset + 20.dp),
                contentAlignment = Alignment.Center
            ) {
                val lottieProgress = if (isPagingRefreshing) {
                    progress
                } else {
                    refreshState.distanceFraction
                }
                if (isPagingRefreshing) {
                    LottieAnimation(
                        composition = composition,
                        progress = { lottieProgress },
                        modifier = Modifier.size(44.dp)
                    )
                }
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
                .padding(start = 16.dp, end = 16.dp, bottom = 60.dp)
                .graphicsLayer {
                    translationY = if (isPagingRefreshing) {
                        refreshState.distanceFraction * with(density) { refreshingOffset.toPx() }
                    } else {
                        0f
                    }
                },
            state = lazyListState
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                FeedUi.FeedNoticeView(
                    feedNotice = feedNotice,
                    feedNoticeClick = feedNoticeClick
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(10.dp))
            }

            items(
                count = latestFeedItems.itemCount,
                key = latestFeedItems.itemKey { it.cardId }, // 고유 키
                contentType = latestFeedItems.itemContentType { "LatestFeed" }
            ) { index ->
                latestFeedItems[index]?.let { latest ->
                    val feedCardType = classifyLatestFeedType(latest)
                    FeedUi.TypedFeedCardView(
                        feedCard = feedCardType,
                        onClick = onClick,
                        onRemoveCard = onRemoveCard
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
            // 다음 페이지 로딩 중 상태 표시
            if (isAppending) {
                item {
                    Box(modifier = Modifier.fillParentMaxWidth()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }

            val appendError = appendLoadState as? LoadState.Error
            if (appendError != null) {
                item {
                    PagingAppendErrorItem(
                        message = appendError.error.message ?: stringResource(R.string.home_feed_load_error),
                        onRetry = { latestFeedItems.retry() }
                    )
                }
            }
        }
    }
}

@Composable
private fun PagingAppendErrorItem(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = TextComponent.BODY_1_M_14,
            color = NeutralColor.GRAY_400,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        androidx.compose.material3.Button(onClick = onRetry) {
            Text("다시 시도")
        }
    }
}

private fun classifyLatestFeedType(item: Latest): FeedCardType {
    return when {
        !item.storyExpirationTime.isNullOrEmpty() -> FeedCardType.BoombType(
            cardId = item.cardId,
            storyExpirationTime = item.storyExpirationTime,
            content = item.cardContent,
            imageUrl = item.cardImgUrl,
            likeValue = item.likeCount.toString(),
            imageName = item.cardImageName,
            font = item.font,
            location = item.distance,
            writeTime = item.createAt,
            commentValue = item.commentCardCount.toString()
        )

        item.isAdminCard -> FeedCardType.AdminType(
            cardId = item.cardId,
            content = item.cardContent,
            imageUrl = item.cardImgUrl,
            imageName = item.cardImageName,
            font = item.font,
            location = item.distance,
            writeTime = item.createAt,
            commentValue = item.commentCardCount.toString(),
            likeValue = item.likeCount.toString()
        )

        else -> FeedCardType.NormalType(
            cardId = item.cardId,
            content = item.cardContent,
            imageUrl = item.cardImgUrl,
            imageName = item.cardImageName,
            font = item.font,
            location = item.distance,
            writeTime = item.createAt,
            commentValue = item.commentCardCount.toString(),
            likeValue = item.likeCount.toString()
        )
    }
}

private const val TAG = "FeedView"
