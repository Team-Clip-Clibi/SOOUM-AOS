package com.phew.feed.feed

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
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.phew.core.ui.R as CoreUiR
import com.phew.core.ui.model.navigation.CardDetailArgs
import com.phew.core.ui.navigation.NavigationKeys
import com.phew.core.ui.state.SooumAppState
import com.phew.core.ui.component.home.HomeTabType
import com.phew.core_design.AppBar
import com.phew.core_design.DialogComponent
import com.phew.core_design.DialogComponent.DeletedCardDialog
import com.phew.core_design.NeutralColor
import com.phew.core_design.TextComponent
import com.phew.core_common.BOTTOM_NAVIGATION_HEIGHT
import com.phew.core_design.LoadingAnimation
import com.phew.core_design.component.refresh.RefreshBox
import com.phew.domain.dto.CardArticle
import com.phew.domain.dto.FeedCardType
import com.phew.domain.dto.Notice
import com.phew.feed.FeedUi
import com.phew.feed.NAV_HOME_FEED_INDEX
import com.phew.feed.NAV_HOME_NEAR_INDEX
import com.phew.feed.NAV_HOME_POPULAR_INDEX
import com.phew.feed.viewModel.DistanceType
import com.phew.feed.viewModel.FeedType
import com.phew.feed.viewModel.FeedLikeUiState
import com.phew.feed.viewModel.FeedViewModel
import com.phew.feed.viewModel.NavigationEvent
import com.phew.feed.viewModel.UiState
import com.phew.presentation.feed.R
import com.phew.feed.NotifyTab
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedView(
    appState: SooumAppState,
    viewModel: FeedViewModel,
    navController: NavHostController,
    requestPermission: () -> Unit,
    closeDialog: () -> Unit,
    noticeClick: (String) -> Unit,
    navigateToDetail: (CardDetailArgs) -> Unit,
    webViewClick: (String) -> Unit,
    adUnitId: String
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val unRead = viewModel.unReadActivateAlarm.collectAsLazyPagingItems()
    val cardArticle = uiState.cardArticle
    val feedNoticeState = uiState.feedNotification
    var cachedFeedNotice by remember { mutableStateOf<List<Notice>>(emptyList()) }
    val feedItems = viewModel.feedPaging.collectAsLazyPagingItems()
    val lazyGridState = rememberLazyGridState()
    var hasScrolledToTop by rememberSaveable { mutableStateOf(false) }
    var previousHomeTab by rememberSaveable { mutableStateOf<HomeTabType?>(null) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentHomeTab = remember(navBackStackEntry) {
        HomeTabType.findHome(navBackStackEntry?.destination?.route)
    }

    val refreshCurrentFeed: () -> Unit = {
        viewModel.refreshCurrentTab()
        unRead.refresh()
    }

    LaunchedEffect(feedNoticeState) {
        if (feedNoticeState is UiState.Success) {
            cachedFeedNotice = feedNoticeState.data
        }
    }

    val feedNotice = when (feedNoticeState) {
        is UiState.Success -> feedNoticeState.data
        else -> cachedFeedNotice
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
    // Refresh handling after returning from other screens
    LaunchedEffect(navBackStackEntry) {
        navBackStackEntry?.savedStateHandle?.let { savedStateHandle ->
            val cardAdded = savedStateHandle.remove<Boolean>(NavigationKeys.CARD_ADDED) == true
            val cardUpdated = savedStateHandle.remove<Boolean>(NavigationKeys.CARD_UPDATED) == true
            val cardDeleted = savedStateHandle.remove<Boolean>(NavigationKeys.CARD_DELETED) == true
            if (cardAdded || cardUpdated || cardDeleted) {
                refreshCurrentFeed()
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

    // 스크롤 관리: 탭 이동 시 초기화 & feedScrollToTopEvent 처리
    LaunchedEffect(Unit) {
        // feedScrollToTopEvent 처리
        launch {
            appState.feedScrollToTopEvent.collect {
                viewModel.clickHomeTab()
                lazyGridState.animateScrollToItem(0)
            }
        }

        // 탭 이동 시 스크롤 초기화 처리
        launch {
            snapshotFlow { hasScrolledToTop }
                .collect { scrolledToTop ->
                    if (!scrolledToTop) {
                        lazyGridState.animateScrollToItem(0)
                        hasScrolledToTop = true
                    }
                }
        }
    }

    val snackBarHostState = remember { SnackbarHostState() }
    val refreshState = rememberPullToRefreshState()
    val feedRefreshLoadState = feedItems.loadState.refresh
    val isRefresh = feedRefreshLoadState is LoadState.Loading && feedItems.itemCount > 0
    var wasRefreshingExistingFeed by remember { mutableStateOf(false) }

    LaunchedEffect(feedRefreshLoadState, feedItems.itemCount) {
        when {
            feedRefreshLoadState is LoadState.Loading && feedItems.itemCount > 0 -> {
                wasRefreshingExistingFeed = true
            }

            wasRefreshingExistingFeed && feedRefreshLoadState is LoadState.NotLoading -> {
                wasRefreshingExistingFeed = false
                viewModel.onFeedRefreshCompleted()
            }

            feedRefreshLoadState is LoadState.Error -> {
                wasRefreshingExistingFeed = false
            }
        }
    }
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
            isRefresh = isRefresh,
            onRefresh = refreshCurrentFeed,
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
                        bottom = paddingValues.calculateBottomPadding()
                    )
            ) {
                FeedContentView(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { clip = false },
                    lazyGridState = lazyGridState,
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
                    feedNotice = feedNotice,
                    webViewClick = webViewClick,
                    feedItems = feedItems,
                    onClick = viewModel::navigateToDetail,
                    onRemoveCard = viewModel::removeFeedCard,
                    feedLikeStates = uiState.feedLikeStates,
                    onClickLike = viewModel::verifyAndToggleLike,
                    pullOffsetPx = pullOffsetPx,
                    onRefresh = refreshCurrentFeed,
                    hiddenCardIds = uiState.hiddenCardIds,
                    deleteNotice = viewModel::deleteNotice,
                    cardsArticle = cardArticle
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
                if (uiState.checkCardDelete is UiState.Success) {
                    DeletedCardDialog(
                        onDismiss = viewModel::initCheckCardDelete,
                        onConfirm = viewModel::initCheckCardDelete
                    )
                }
            }
        }
    }
}

@Composable
private fun TopView(
    newNotice: Boolean,
    noticeClick: (String) -> Unit,
    snackBarHostState: SnackbarHostState,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        snackbarHost = {
            DialogComponent.CustomAnimationSnackBarHost(hostState = snackBarHostState)
        },
        topBar = {
            AppBar.HomeAppBar(
                onClick = { noticeClick(NotifyTab.NOTIFY_ACTIVATE.toString()) },
                newAlarm = newNotice,
            )
        },
        content = content
    )
}

@Composable
private fun FeedContentView(
    modifier: Modifier,
    lazyGridState: LazyGridState,
    recentClick: () -> Unit,
    popularClick: () -> Unit,
    nearClick: () -> Unit,
    distanceClick: (DistanceType) -> Unit,
    selectDistance: DistanceType,
    currentTab: FeedType,
    feedNotice: List<Notice>,
    feedItems: LazyPagingItems<FeedCardType>,
    onClick: (String, Boolean) -> Unit,
    onRemoveCard: (String) -> Unit,
    feedLikeStates: Map<Long, FeedLikeUiState>,
    onClickLike: (Long, Int, Boolean) -> Unit,
    pullOffsetPx: Float,
    onRefresh: () -> Unit,
    hiddenCardIds: Set<Long>,
    webViewClick: (String) -> Unit,
    deleteNotice: (Int) -> Unit,
    cardsArticle: UiState<List<CardArticle>>,
) {
    val selectIndex = when (currentTab) {
        FeedType.Latest -> NAV_HOME_FEED_INDEX
        FeedType.Popular -> NAV_HOME_POPULAR_INDEX
        FeedType.Distance -> NAV_HOME_NEAR_INDEX
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        state = lazyGridState,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = BOTTOM_NAVIGATION_HEIGHT.dp)
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
        item(
            key = "feed_notice_section",
            span = { GridItemSpan(maxLineSpan) }
        ) {
            FeedUi.FeedNoticeView(
                noticeList = feedNotice,
                feedNoticeClick = { url -> webViewClick(url) },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .graphicsLayer { translationY = pullOffsetPx },
                deleteNotice = deleteNotice
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            when (cardsArticle) {
                is UiState.Success -> FeedUi.CardArticleView(
                    cardsArticle.data, modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .graphicsLayer { translationY = pullOffsetPx },
                    onCardClick = { id ->
                        onClick(id.toString(), false)
                    }
                )

                else -> Unit
            }
        }
        when (val refreshState = feedItems.loadState.refresh) {
            is LoadState.Error -> {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(modifier = Modifier.graphicsLayer { translationY = pullOffsetPx }) {
                        ErrorView(
                            message = refreshState.error.message
                                ?: stringResource(R.string.home_feed_load_error),
                            onRetry = onRefresh
                        )
                    }
                }
            }

            is LoadState.Loading,
            is LoadState.NotLoading,
                -> {
                val isInitialLoading = refreshState is LoadState.Loading && feedItems.itemCount == 0

                when {
                    isInitialLoading -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            LoadingFeedItem(pullOffsetPx = pullOffsetPx)
                        }
                    }

                    feedItems.itemCount == 0 -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(modifier = Modifier.graphicsLayer { translationY = pullOffsetPx }) {
                                EmptyFeedView()
                            }
                        }
                    }

                    else -> {
                        items(
                            count = feedItems.itemCount,
                            key = { index ->
                                feedItems.peek(index)?.cardId ?: "feed_placeholder_$index"
                            },
                            contentType = { "FeedCard" }
                        ) { index ->
                            val feedCard = feedItems[index] ?: return@items
                            if (feedCard.isHidden(hiddenCardIds)) return@items

                            FeedCardItem(
                                feedCard = feedCard,
                                pullOffsetPx = pullOffsetPx,
                                onClick = onClick,
                                onRemoveCard = onRemoveCard,
                                likeState = feedCard.cardId.toLongOrNull()?.let(feedLikeStates::get),
                                onClickLike = onClickLike,
                            )
                        }
                    }
                }

                when (val appendState = feedItems.loadState.append) {
                    is LoadState.Error -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(modifier = Modifier.graphicsLayer { translationY = pullOffsetPx }) {
                                ErrorView(
                                    message = appendState.error.message
                                        ?: stringResource(R.string.home_feed_load_error),
                                    onRetry = feedItems::retry
                                )
                            }
                        }
                    }

                    is LoadState.Loading -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            LoadingFeedItem(pullOffsetPx = pullOffsetPx)
                        }
                    }

                    is LoadState.NotLoading -> Unit
                }
            }
        }
    }
}

@Composable
private fun LoadingFeedItem(pullOffsetPx: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp)
            .graphicsLayer { translationY = pullOffsetPx }
    ) {
        LoadingAnimation.LoadingView(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun FeedCardItem(
    feedCard: FeedCardType,
    pullOffsetPx: Float,
    onClick: (String, Boolean) -> Unit,
    onRemoveCard: (String) -> Unit,
    likeState: FeedLikeUiState?,
    onClickLike: (Long, Int, Boolean) -> Unit,
) {
    val cardId = feedCard.cardId.toLongOrNull()
    val displayedIsLike = likeState?.isLike ?: feedCard.isLike
    val displayedLikeCount = likeState?.likeCount ?: feedCard.likeValue.toIntOrNull() ?: 0
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp)
            .graphicsLayer { translationY = pullOffsetPx }
    ) {
        FeedUi.TypedFeedCardView(
            feedCard = feedCard,
            onClick = { id ->
                onClick(id, feedCard.isEventCard())
            },
            onRemoveCard = onRemoveCard,
            isLike = displayedIsLike,
            likeCount = displayedLikeCount,
            isLikeLoading = likeState?.isLoading == true,
            likeAnimationKey = likeState?.animationVersion ?: 0,
            onClickLike = {
                cardId?.let {
                    onClickLike(it, displayedLikeCount, displayedIsLike)
                }
            },
        )
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
    onRetry: () -> Unit,
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

private val FeedCardType.cardId: String
    get() = when (this) {
        is FeedCardType.BoombType -> cardId
        is FeedCardType.AdminType -> cardId
        is FeedCardType.NormalType -> cardId
    }

private val FeedCardType.likeValue: String
    get() = when (this) {
        is FeedCardType.BoombType -> likeValue
        is FeedCardType.AdminType -> likeValue
        is FeedCardType.NormalType -> likeValue
    }

private val FeedCardType.isLike: Boolean
    get() = when (this) {
        is FeedCardType.BoombType -> isLike
        is FeedCardType.AdminType -> isLike
        is FeedCardType.NormalType -> isLike
    }

private fun FeedCardType.isHidden(hiddenCardIds: Set<Long>): Boolean {
    return cardId.toLongOrNull() in hiddenCardIds
}
