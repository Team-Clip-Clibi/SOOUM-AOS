package com.phew.feed.notification

import android.content.Context
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.phew.core.ui.model.navigation.CardDetailArgs
import com.phew.core_design.AppBar
import com.phew.core_design.DialogComponent
import com.phew.core_design.DialogComponent.DeletedCardDialog
import com.phew.core_design.LoadingAnimation
import com.phew.core_design.NeutralColor
import com.phew.core_design.TextComponent
import com.phew.core_design.component.refresh.RefreshBox
import com.phew.domain.dto.Notice
import com.phew.domain.dto.Notification
import com.phew.feed.NotificationUi
import com.phew.feed.NotifyTab
import com.phew.feed.viewModel.FeedViewModel
import com.phew.feed.viewModel.NavigationEvent
import com.phew.feed.viewModel.UiState
import com.phew.presentation.feed.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotifyView(
    viewModel: FeedViewModel,
    backClick: () -> Unit,
    navigateToDetail: (CardDetailArgs) -> Unit,
    navigateToWebView: (String) -> Unit,
    userSelectIndex: NotifyTab
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val notices = viewModel.notice.collectAsLazyPagingItems()
    val read = viewModel.readActivateAlarm.collectAsLazyPagingItems()
    val unRead = viewModel.unReadActivateAlarm.collectAsLazyPagingItems()
    val onBack by rememberUpdatedState(newValue = backClick)
    val snackBarHostState = remember { SnackbarHostState() }
    val refreshState = rememberPullToRefreshState()
    var selectIndex by remember { mutableStateOf(userSelectIndex) }
    val isRefreshing by remember(
        key1 = notices.loadState.refresh,
        key2 = unRead.loadState.refresh,
        key3 = read.loadState.refresh
    ) {
        derivedStateOf {
            when (selectIndex) {
                NotifyTab.NOTIFY_ACTIVATE -> {
                    unRead.loadState.refresh is LoadState.Loading || read.loadState.refresh is LoadState.Loading
                }

                NotifyTab.NOTIFY_SERVICE -> notices.loadState.refresh is LoadState.Loading
            }
        }
    }
    LaunchedEffect(viewModel) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is NavigationEvent.NavigateToDetail -> {
                    unRead.refresh()
                    read.refresh()
                    navigateToDetail(event.args)
                }
            }
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                when (selectIndex) {
                    NotifyTab.NOTIFY_ACTIVATE -> {
                        unRead.refresh()
                        read.refresh()
                    }

                    NotifyTab.NOTIFY_SERVICE -> {
                        notices.refresh()
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    val context = LocalContext.current
    BackHandler { onBack() }
    LaunchedEffect(Unit) {
        viewModel.readActivateNotify()
    }
    HandleReadActivateError(
        activateError = uiState.setReadNotify,
        snackBarHostState = snackBarHostState,
        context = context
    )
    NoticeViewTopBar(
        onBackClick = onBack,
        snackBarHostState = snackBarHostState,
        selectIndex = selectIndex,
        onTabClick = { data ->
            selectIndex = data
        }
    ) { paddingValues ->
        RefreshBox(
            isRefresh = isRefreshing,
            onRefresh = {
                when (selectIndex) {
                    NotifyTab.NOTIFY_ACTIVATE -> {
                        unRead.refresh()
                        read.refresh()
                    }

                    NotifyTab.NOTIFY_SERVICE -> {
                        notices.refresh()
                    }
                }
            },
            state = refreshState,
            paddingValues = paddingValues,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = NeutralColor.WHITE)
                    .graphicsLayer {
                        this.translationY = refreshState.distanceFraction * 102.dp.toPx()
                    }
                    .padding(
                        top = paddingValues.calculateTopPadding() + 9.dp,
                        bottom = paddingValues.calculateBottomPadding(),
                    )
            ) {
                when (selectIndex) {
                    NotifyTab.NOTIFY_ACTIVATE -> ActivateAlarm(
                        unReadAlarm = unRead,
                        readAlarm = read,
                        snackBarHostState = snackBarHostState,
                        onItemExpose = viewModel::addItemAsRead,
                        context = context,
                        onCardClick = { cardId ->
                            viewModel.navigateToDetail(
                                cardId = cardId.toString(),
                                isEventCard = false
                            )
                        }
                    )

                    NotifyTab.NOTIFY_SERVICE -> NotifyViewContent(
                        data = notices,
                        snackBarHostState = snackBarHostState,
                        onNoticeClick = navigateToWebView
                    )
                }
            }
        }
        }
        if (uiState.checkCardDelete is UiState.Success) {
            val onDialogHandled = {
                viewModel.initCheckCardDelete()
                when (selectIndex) {
                    NotifyTab.NOTIFY_ACTIVATE -> {
                        unRead.refresh()
                        read.refresh()
                    }

                    NotifyTab.NOTIFY_SERVICE -> {
                        notices.refresh()
                    }
                }
            }
            DeletedCardDialog(
                onDismiss = onDialogHandled,
                onConfirm = onDialogHandled
            )
        }
    }


@Composable
private fun NoticeViewTopBar(
    onBackClick: () -> Unit,
    snackBarHostState: SnackbarHostState,
    selectIndex: NotifyTab,
    onTabClick: (NotifyTab) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        snackbarHost = {
            DialogComponent.CustomAnimationSnackBarHost(hostState = snackBarHostState)
        },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = NeutralColor.WHITE)
            ) {
                AppBar.IconLeftAppBar(
                    onClick = onBackClick,
                    appBarText = stringResource(R.string.home_notice_top_bar)
                )
                Spacer(modifier = Modifier.height(9.5.dp))
                NotificationUi.NotifyTabBar(
                    selectData = selectIndex,
                    onClick = onTabClick
                )
            }
        },
        content = content
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotifyViewContent(
    data: LazyPagingItems<Notice>,
    snackBarHostState: SnackbarHostState,
    onNoticeClick: (String) -> Unit,
) {
    val context = LocalContext.current
    val nestedScrollConnection = remember { object : NestedScrollConnection {} }
    val refreshState = rememberPullToRefreshState()
    when (val dataState = data.loadState.refresh) {
        is LoadState.Error -> {
            EmptyNotifyView()
            LaunchedEffect(dataState.error.message) {
                snackBarHostState.showSnackbar(
                    message = context.getString(com.phew.core_design.R.string.error_network),
                    duration = SnackbarDuration.Short
                )
            }
        }

        LoadState.Loading -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = NeutralColor.WHITE),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {}
        }

        is LoadState.NotLoading -> {
            when (data.itemCount) {
                0 -> EmptyNotifyView()
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(nestedScrollConnection)
                            .graphicsLayer {
                                translationY =
                                    refreshState.distanceFraction * (56.dp.toPx())
                            },
                        state = rememberLazyListState()
                    ) {
                        items(
                            count = data.itemCount,
                        ) { index ->
                            val item = data[index] ?: return@items
                            NotificationUi.NoticeComponentView(data = item, onClick = onNoticeClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivateAlarm(
    unReadAlarm: LazyPagingItems<Notification>,
    readAlarm: LazyPagingItems<Notification>,
    snackBarHostState: SnackbarHostState,
    onItemExpose: (Long) -> Unit,
    context: Context,
    onCardClick: (Long) -> Unit,
) {
    if (unReadAlarm.itemCount == 0 && readAlarm.itemCount == 0) {
        EmptyNotifyView()
        return
    }
    when {
        unReadAlarm.itemCount != 0 && readAlarm.itemCount == 0 -> {
            HandleUnReadAlarm(
                unReadAlarm = unReadAlarm,
                lazyListState = rememberLazyListState(),
                snackBarHostState = snackBarHostState,
                onItemExpose = onItemExpose,
                context = context,
                onCardClick = onCardClick
            )
        }

        unReadAlarm.itemCount == 0 && readAlarm.itemCount != 0 -> {
            HandleReadAlarm(
                readAlarm = readAlarm,
                lazyListState = rememberLazyListState(),
                snackBarHostState = snackBarHostState,
                context = context,
                onCardClick = onCardClick
            )
        }

        unReadAlarm.itemCount != 0 && readAlarm.itemCount != 0 -> {
            HandleActivateAlarm(
                readAlarm = readAlarm,
                unReadAlarm = unReadAlarm,
                onItemExpose = onItemExpose,
                onCardClick = onCardClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HandleUnReadAlarm(
    unReadAlarm: LazyPagingItems<Notification>,
    lazyListState: LazyListState,
    snackBarHostState: SnackbarHostState,
    onItemExpose: (Long) -> Unit,
    context: Context,
    onCardClick: (Long) -> Unit
) {
    val nestedScrollConnection = remember { object : NestedScrollConnection {} }
    val refreshState = rememberPullToRefreshState()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
            .graphicsLayer {
                translationY =
                    refreshState.distanceFraction * (56.dp.toPx())
            },
        state = lazyListState
    ) {
        when (val dataState = unReadAlarm.loadState.append) {
            is LoadState.Error -> {
                item {
                    LaunchedEffect(dataState.error.message) {
                        snackBarHostState.showSnackbar(
                            message = context.getString(com.phew.core_design.R.string.error_network),
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }

            LoadState.Loading -> {
                item {
                    LoadingAnimation.LoadingView()
                }
            }

            is LoadState.NotLoading -> {
                items(
                    count = unReadAlarm.itemCount,
                ) { index ->
                    val item = unReadAlarm[index] ?: return@items
                    NotificationUi.NotifyViewUnread(
                        data = item,
                        onItemExpose = onItemExpose,
                        onCardClick = onCardClick
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HandleReadAlarm(
    readAlarm: LazyPagingItems<Notification>,
    lazyListState: LazyListState,
    snackBarHostState: SnackbarHostState,
    context: Context,
    onCardClick: (Long) -> Unit
) {
    val nestedScrollConnection = remember { object : NestedScrollConnection {} }
    val refreshState = rememberPullToRefreshState()
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
                .graphicsLayer {
                    translationY =
                        refreshState.distanceFraction * (56.dp.toPx())
                },
            state = lazyListState
        ) {
            when (val dataState = readAlarm.loadState.append) {
                is LoadState.Error -> {
                    item {
                        LaunchedEffect(dataState.error.message) {
                            snackBarHostState.showSnackbar(
                                message = context.getString(com.phew.core_design.R.string.error_network),
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                }

                LoadState.Loading -> {
                    item {
                        LoadingAnimation.LoadingView()
                    }
                }

                is LoadState.NotLoading -> {
                    item {
                        Text(
                            text = stringResource(R.string.home_notice_last_alarm),
                            style = TextComponent.SUBTITLE_2_SB_14,
                            color = NeutralColor.BLACK,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )
                    }
                    items(
                        count = readAlarm.itemCount,
                    ) { index ->
                        val item = readAlarm[index] ?: return@items
                        NotificationUi.NotifyViewRead(data = item, onCardClick = onCardClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun HandleActivateAlarm(
    readAlarm: LazyPagingItems<Notification>,
    unReadAlarm: LazyPagingItems<Notification>,
    onItemExpose: (Long) -> Unit,
    onCardClick: (Long) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        modifier = Modifier.fillMaxSize()
    ) {
        when (unReadAlarm.loadState.refresh) {
            is LoadState.NotLoading -> {
                items(
                    count = unReadAlarm.itemCount,
                    key = { index ->
                        val id = unReadAlarm.peek(index)?.notificationId ?: "loading"
                        "${id}_$index"
                    }
                ) { index ->
                    val item = unReadAlarm[index]
                    if (item != null) {
                        NotificationUi.NotifyViewUnread(
                            data = item,
                            onItemExpose = onItemExpose,
                            onCardClick = onCardClick
                        )
                    }
                }
            }

            else -> Unit
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(modifier = Modifier.padding(top = 32.dp))
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = stringResource(R.string.home_notice_last_alarm),
                style = TextComponent.SUBTITLE_2_SB_14,
                color = NeutralColor.BLACK,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }
        when (readAlarm.loadState.refresh) {
            is LoadState.NotLoading -> {
                items(
                    count = readAlarm.itemCount,
                    key = { index ->
                        val id = readAlarm.peek(index)?.notificationId ?: "loading"
                        "${id}_$index"
                    }
                ) { index ->
                    val item = readAlarm[index]
                    if (item != null) {
                        NotificationUi.NotifyViewRead(data = item, onCardClick = onCardClick)
                    }
                }
            }

            else -> Unit
        }
    }
}

@Composable
fun HandleReadActivateError(
    activateError: UiState<Unit>,
    snackBarHostState: SnackbarHostState,
    context: Context
) {
    LaunchedEffect(activateError) {
        if (activateError is UiState.Fail) {
            snackBarHostState.showSnackbar(
                message = context.getString(com.phew.core_design.R.string.error_network),
                duration = SnackbarDuration.Short
            )
        }
    }
}

@Composable
private fun EmptyNotifyView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
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
}
