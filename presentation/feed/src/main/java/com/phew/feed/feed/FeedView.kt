package com.phew.feed.feed

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.LazyPagingItems
import androidx.paging.LoadState
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.phew.core.ui.model.navigation.CardDetailArgs
import com.phew.core.ui.navigation.NavigationKeys
import com.phew.core_design.AppBar
import com.phew.core_design.DialogComponent
import com.phew.core_design.NeutralColor
import com.phew.core_design.TextComponent
import com.phew.core.ui.component.home.HomeTabType
import com.phew.core_common.BOTTOM_NAVIGATION_HEIGHT
import com.phew.core_design.LoadingAnimation
import com.phew.core_design.component.refresh.RefreshBox
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
import com.phew.core.ui.R as CoreUiR


@OptIn(FlowPreview::class, ExperimentalMaterial3Api::class)
@Composable
fun FeedView(
    viewModel: FeedViewModel,
    navController: NavHostController,
    finish: () -> Unit,
    requestPermission: () -> Unit,
    closeDialog: () -> Unit,
    noticeClick: () -> Unit,
    navigateToDetail: (CardDetailArgs) -> Unit,
    webViewClick: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val unRead = viewModel.unReadActivateAlarm.collectAsLazyPagingItems()
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
    val isRefreshing = when (uiState.currentTab) {
        FeedType.Latest -> uiState.latestPagingState is FeedPagingState.Loading
        else -> currentPagingState is FeedPagingState.Loading
    }
    val snackBarHostState = remember { SnackbarHostState() }
    val refreshState = rememberPullToRefreshState()
    val pullDistance = 102.dp
    val pullOffsetPx = with(LocalDensity.current) {
        refreshState.distanceFraction * pullDistance.toPx()
    }
    TopView(
        noticeClick = noticeClick,
        newNotice = unRead.itemCount != 0,
        snackBarHostState = snackBarHostState
    ) { paddingValues ->
        RefreshBox(
            isRefresh = isRefreshing,
            onRefresh = viewModel::refreshCurrentTab,
            state = refreshState,
            paddingValues = paddingValues,
            indicatorTopPadding = 60.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = NeutralColor.GRAY_100)
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding() + BOTTOM_NAVIGATION_HEIGHT.dp
                    )
            ) {
                FeedContentView(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { clip = false },
                    recentClick = {
                        viewModel.switchTab(FeedType.Latest)
                    },
                    popularClick = {
                        viewModel.switchTab(FeedType.Popular)
                    },
                    nearClick = viewModel::checkLocationPermission,
                    distanceClick = viewModel::switchDistanceTab,
                    selectDistance = uiState.distanceTab,
                    currentTab = uiState.currentTab,
                    feedNotice = if (feedNoticeState is UiState.Success) feedNoticeState.data else emptyList(),
                    feedNoticeClick = { url ->
                        webViewClick(url)
                    },
                    latestFeedItems = latestFeedItems,
                    onClick = viewModel::navigateToDetail,
                    onRemoveCard = viewModel::removeFeedCard,
                    currentPagingState = uiState.currentPagingState,
                    pullOffsetPx = pullOffsetPx,
                    onRefresh = viewModel::refreshCurrentTab
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
                if (uiState.checkCardDelete is UiState.Success && (uiState.checkCardDelete as UiState.Success<Boolean>).data) {
                    DialogComponent.NoDescriptionButtonOne(
                        title = stringResource(R.string.home_feed_dialog_delete_title),
                        buttonText = stringResource(com.phew.core_design.R.string.common_okay),
                        onDismiss = viewModel::initCheckCardDelete,
                        onClick = viewModel::initCheckCardDelete
                    )
                }
            }
        }
    }
}

@Composable
private fun TopView(
    newNotice: Boolean,
    noticeClick: () -> Unit,
    snackBarHostState: SnackbarHostState,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        snackbarHost = {
            DialogComponent.CustomAnimationSnackBarHost(hostState = snackBarHostState)
        },
        topBar = {
            AppBar.HomeAppBar(
                onClick = noticeClick,
                newAlarm = newNotice,
            )
        },
        content = content
    )
}

@Composable
private fun FeedContentView(
    modifier: Modifier, recentClick: () -> Unit,
    popularClick: () -> Unit,
    nearClick: () -> Unit,
    distanceClick: (DistanceType) -> Unit,
    selectDistance: DistanceType,
    currentTab: FeedType,
    feedNotice: List<Notice>,
    feedNoticeClick: (String) -> Unit,
    latestFeedItems: LazyPagingItems<Latest>,
    onClick: (String) -> Unit,
    onRemoveCard: (String) -> Unit,
    currentPagingState: FeedPagingState,
    pullOffsetPx: Float,
    onRefresh : () -> Unit
) {
    val selectIndex = when (currentTab) {
        FeedType.Latest -> NAV_HOME_FEED_INDEX
        FeedType.Popular -> NAV_HOME_POPULAR_INDEX
        FeedType.Distance -> NAV_HOME_NEAR_INDEX
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Box(
                modifier = Modifier
                    .zIndex(1f)
            ) {
                FeedUi.FeedTab(
                    selectTabData = selectIndex,
                    recentClick = recentClick,
                    popularClick = popularClick,
                    nearClick = nearClick,
                    onDistanceClick = distanceClick,
                    selectDistanceType = selectDistance
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            FeedUi.FeedNoticeView(
                feedNotice = feedNotice,
                feedNoticeClick = feedNoticeClick,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .graphicsLayer { translationY = pullOffsetPx }
            )
        }
        when (currentTab) {
            FeedType.Latest -> {
                when (latestFeedItems.loadState.refresh) {
                    is LoadState.Error -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            ErrorView(
                                message = (latestFeedItems.loadState.refresh as LoadState.Error).error.message
                                    ?: stringResource(R.string.home_feed_load_error),
                                onRetry = {
                                    latestFeedItems.retry()
                                }
                            )
                        }
                    }

                    LoadState.Loading -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            LoadingAnimation.LoadingView(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 100.dp)
                            )
                        }
                    }

                    is LoadState.NotLoading -> {
                        if (latestFeedItems.itemCount == 0) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                EmptyFeedView()
                            }
                        } else {
                            items(
                                count = latestFeedItems.itemCount,
                                key = latestFeedItems.itemKey { it.cardId },
                                contentType = latestFeedItems.itemContentType { "LatestFeed" }
                            ) { index ->
                                latestFeedItems[index]?.let { latest ->
                                    val feedCardType = classifyLatestFeedType(latest)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight()
                                            .padding(horizontal = 16.dp)
                                            .graphicsLayer { translationY = pullOffsetPx }
                                    ) {
                                        FeedUi.TypedFeedCardView(
                                            feedCard = feedCardType,
                                            onClick = onClick,
                                            onRemoveCard = onRemoveCard,
                                        )
                                    }
                                }
                            }
                        }
                        when(val appendState = latestFeedItems.loadState.append){
                            is LoadState.Error -> {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    ErrorView(
                                        message = appendState.error.message
                                            ?: stringResource(R.string.home_feed_load_error),
                                        onRetry = {
                                            latestFeedItems.retry()
                                        }
                                    )
                                }
                            }
                            is LoadState.Loading -> {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 20.dp)
                                            .graphicsLayer { translationY = pullOffsetPx }
                                    ) {
                                        LoadingAnimation.LoadingView(
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                            is LoadState.NotLoading -> {
                                // No-op
                            }
                        }
                    }
                }
            }

            else -> {
                when (currentPagingState) {
                    is FeedPagingState.Error -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            ErrorView(
                                message = currentPagingState.message,
                                onRetry = onRefresh
                            )
                        }
                    }
                    is FeedPagingState.LoadingMore -> {
                        if (currentPagingState.existingData.isEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                EmptyFeedView()
                            }
                        } else {
                            itemsIndexed(
                                items = currentPagingState.existingData,
                                key = { _, feedCard ->
                                    when (feedCard) {
                                        is FeedCardType.BoombType -> feedCard.cardId
                                        is FeedCardType.AdminType -> feedCard.cardId
                                        is FeedCardType.NormalType -> feedCard.cardId
                                    }
                                }
                            ) { _, feedCard ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .padding(horizontal = 16.dp)
                                        .graphicsLayer { translationY = pullOffsetPx }
                                ) {
                                    FeedUi.TypedFeedCardView(
                                        feedCard = feedCard,
                                        onClick = onClick,
                                        onRemoveCard = onRemoveCard,
                                    )
                                }
                            }
                        }
                    }
                    is FeedPagingState.Success -> {
                        if (currentPagingState.feedCards.isEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                EmptyFeedView()
                            }
                        } else {
                            itemsIndexed(
                                items = currentPagingState.feedCards,
                                key = { _, feedCard ->
                                    when (feedCard) {
                                        is FeedCardType.BoombType -> feedCard.cardId
                                        is FeedCardType.AdminType -> feedCard.cardId
                                        is FeedCardType.NormalType -> feedCard.cardId
                                    }
                                }
                            ) { _, feedCard ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .padding(horizontal = 16.dp)
                                        .graphicsLayer { translationY = pullOffsetPx }
                                ) {
                                    FeedUi.TypedFeedCardView(
                                        feedCard = feedCard,
                                        onClick = onClick,
                                        onRemoveCard = onRemoveCard,
                                    )
                                }
                            }
                        }
                    }

                    else -> Unit
                }
            }
        }
    }
}

@Composable
private fun EmptyFeedView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 100.dp),
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
        Button(
            onClick = onRetry
        ) {
            Text(stringResource(R.string.retry))
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
