package com.phew.feed.notification

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_design.AppBar
import com.phew.core_design.NeutralColor
import com.phew.core_design.TextComponent
import com.phew.domain.dto.Notice
import com.phew.domain.dto.Notification
import com.phew.feed.NAV_NOTICE_ACTIVATE
import com.phew.feed.NAV_NOTICE_NOTIFY_INDEX
import com.phew.feed.NotificationUi
import com.phew.feed.viewModel.HomeViewModel
import com.phew.presentation.feed.R
import kotlinx.coroutines.launch


@Composable
fun NotifyView(
    viewModel: HomeViewModel,
    backClick: () -> Unit,
    logout: () -> Unit,
    snackBarHostState: SnackbarHostState,
) {
    var selectIndex by remember { mutableIntStateOf(NAV_NOTICE_ACTIVATE) }
    val lazyListState = rememberLazyListState()
    val isTabsVisible by remember(lazyListState) {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex == 0 &&
                    lazyListState.firstVisibleItemScrollOffset < 8
        }
    }
    val notices = viewModel.notice.collectAsLazyPagingItems()
    val unRead = viewModel.unReadNotification.collectAsLazyPagingItems()
    val read = viewModel.readNotification.collectAsLazyPagingItems()
    val scope = rememberCoroutineScope()
    val networkErrorMsg = stringResource(com.phew.core_design.R.string.error_network)
    val onBack by rememberUpdatedState(newValue = backClick)
    val onLogout by rememberUpdatedState(newValue = logout)
    val nestedScrollConnection = remember { object : NestedScrollConnection {} }
    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralColor.WHITE)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        TopBar(
            allClick = { selectIndex = NAV_NOTICE_ACTIVATE },
            noticeClick = { selectIndex = NAV_NOTICE_NOTIFY_INDEX },
            backClick = onBack,
            isTabsVisible = isTabsVisible,
            selectIndex = selectIndex
        )

        when (selectIndex) {
            NAV_NOTICE_ACTIVATE -> {
                when {
                    unRead.loadState.refresh is LoadState.Loading || read.loadState.refresh is LoadState.Loading -> {
                        EmptyNotifyView()
                    }

                    unRead.loadState.refresh is LoadState.Error || read.loadState.refresh is LoadState.Error -> {
                        val error = if (unRead.loadState.refresh is LoadState.Error) {
                            (unRead.loadState.refresh as LoadState.Error).error
                        } else (read.loadState.refresh as LoadState.Error).error
                        when (error.message) {
                            ERROR_NETWORK -> {
                                LaunchedEffect(error.message) {
                                    snackBarHostState.showSnackbar(
                                        message = networkErrorMsg,
                                        withDismissAction = true
                                    )
                                }
                            }

                            ERROR_LOGOUT -> onLogout()
                            else -> error.cause?.printStackTrace()
                        }
                    }

                    unRead.itemCount == 0 && read.itemCount == 0 -> {
                        EmptyNotifyView()
                    }

                    else -> ActivateNotify(
                        read = read,
                        unRead = unRead,
                        nestedScrollConnection = nestedScrollConnection,
                        failToLoad = {
                            scope.launch {
                                snackBarHostState.showSnackbar(
                                    message = networkErrorMsg,
                                    withDismissAction = true,
                                    duration = SnackbarDuration.Short
                                )
                            }
                        },
                        lazyListState = lazyListState,
                    )
                }
            }

            NAV_NOTICE_NOTIFY_INDEX -> {
                when {
                    notices.loadState.refresh is LoadState.Loading -> {
                        EmptyNotifyView()
                    }

                    notices.loadState.refresh is LoadState.Error -> {
                        val error = (notices.loadState.refresh as LoadState.Error).error
                        when (error.message) {
                            ERROR_NETWORK -> {
                                LaunchedEffect(error.message) {
                                    snackBarHostState.showSnackbar(
                                        message = networkErrorMsg,
                                        withDismissAction = true
                                    )
                                }
                            }

                            ERROR_LOGOUT -> onLogout()
                            else -> error.cause?.printStackTrace()
                        }
                    }

                    notices.itemCount == 0 -> {
                        EmptyNotifyView()
                    }

                    else -> {
                        NoticeView(
                            data = notices,
                            lazyListState = lazyListState,
                            nestedScrollConnection = nestedScrollConnection,
                            failToLoad = {
                                scope.launch {
                                    snackBarHostState.showSnackbar(
                                        message = networkErrorMsg,
                                        withDismissAction = true,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    backClick: () -> Unit,
    allClick: () -> Unit,
    noticeClick: () -> Unit,
    isTabsVisible: Boolean,
    selectIndex: Int,
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()) {
        AppBar.IconLeftAppBar(
            onClick = backClick,
            appBarText = stringResource(R.string.home_notice_top_bar)
        )
        NotificationUi.AnimatedNoticeTabLayout(
            allClick = allClick,
            noticeClick = noticeClick,
            isTabsVisible = isTabsVisible,
            selectTabData = selectIndex
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoticeView(
    data: LazyPagingItems<Notice>,
    lazyListState: LazyListState,
    nestedScrollConnection: NestedScrollConnection,
    failToLoad: () -> Unit,
) {
    val refreshState = rememberPullToRefreshState()
    val refreshingOffset = 56.dp
    val density = LocalDensity.current
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(com.phew.core_design.R.raw.ic_refresh)
    )
    val isRefreshing by remember(data.loadState.refresh) {
        derivedStateOf { data.loadState.refresh is LoadState.Loading }
    }
    val refreshProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = isRefreshing
    )
    (data.loadState.refresh as? LoadState.Error)?.error?.let { err ->
        LaunchedEffect("refresh_err_${err.message}") { failToLoad() }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { data.refresh() },
        state = refreshState,
        indicator = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(refreshingOffset + 20.dp),
                contentAlignment = Alignment.Center
            ) {
                val progress = if (isRefreshing) refreshProgress else refreshState.distanceFraction
                if (isRefreshing || refreshState.distanceFraction > 0f) {
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
                .nestedScroll(nestedScrollConnection)
                .padding(horizontal = 16.dp)
                .graphicsLayer {
                    translationY =
                        refreshState.distanceFraction * with(density) { refreshingOffset.toPx() }
                },
            state = lazyListState
        ) {
            // 본문
            items(
                count = data.itemCount,
            ) { index ->
                val item = data[index] ?: return@items
                NotificationUi.NoticeComponentView(data = item)
            }

            when (data.loadState.append) {
                is LoadState.Loading -> {
                    item {
                        val appendProgress by animateLottieCompositionAsState(
                            composition = composition,
                            iterations = LottieConstants.IterateForever,
                            isPlaying = true
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            LottieAnimation(
                                composition = composition,
                                progress = { appendProgress },
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }

                is LoadState.Error -> {
                    failToLoad()
                }

                else -> Unit
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivateNotify(
    unRead: LazyPagingItems<Notification>,
    read: LazyPagingItems<Notification>,
    lazyListState: LazyListState,
    nestedScrollConnection: NestedScrollConnection,
    failToLoad: () -> Unit,
) {
    val refreshState = rememberPullToRefreshState()
    val refreshOffset = 56.dp
    val density = LocalDensity.current
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(com.phew.core_design.R.raw.ic_refresh)
    )
    val isRefreshing = unRead.loadState.refresh is LoadState.Loading ||
            read.loadState.refresh is LoadState.Loading

    val refreshProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = isRefreshing
    )
    LaunchedEffect(unRead.loadState.refresh, read.loadState.refresh) {
        if (unRead.loadState.refresh is LoadState.Error || read.loadState.refresh is LoadState.Error) {
            failToLoad()
        }
    }
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { unRead.refresh() },
        indicator = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(refreshOffset + 20.dp),
                contentAlignment = Alignment.Center
            ) {
                val progress = if (isRefreshing) refreshProgress else refreshState.distanceFraction
                if (isRefreshing || refreshState.distanceFraction > 0f) {
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
                .nestedScroll(nestedScrollConnection)
                .graphicsLayer {
                    translationY =
                        refreshState.distanceFraction * with(density) { refreshOffset.toPx() }
                },
            state = lazyListState
        ) {
            items(
                count = unRead.itemCount,
            ) { index ->
                val item = unRead[index] ?: return@items
                NotificationUi.NotifyViewUnread(item)
            }
            when (unRead.loadState.refresh) {
                is LoadState.Loading -> {
                    item {
                        val appendProgress by animateLottieCompositionAsState(
                            composition = composition,
                            iterations = LottieConstants.IterateForever,
                            isPlaying = true
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            LottieAnimation(
                                composition = composition,
                                progress = { appendProgress },
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }

                is LoadState.Error -> {
                    failToLoad()
                }

                else -> Unit
            }
            if (read.itemCount > 0) {
                item {
                    Text(
                        text = stringResource(R.string.home_notice_last_alarm),
                        style = TextComponent.SUBTITLE_3_SB_14,
                        color = NeutralColor.BLACK
                    )
                }
            }
            items(
                count = read.itemCount,
            ) { index ->
                val item = read[index] ?: return@items
                NotificationUi.NotifyViewRead(item)
            }
        }
    }
}

@Composable
private fun EmptyNotifyView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(com.phew.core_design.R.drawable.ic_noti_no_data),
            contentDescription = "no notify"
        )
        Text(
            text = stringResource(R.string.home_notice_no_notice),
            style = TextComponent.BODY_1_M_14,
            color = NeutralColor.GRAY_400,
            modifier = Modifier.padding(top = 20.dp)
        )
    }
}
