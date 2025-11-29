package com.phew.profile.screen

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_design.AppBar
import com.phew.core_design.Danger
import com.phew.core_design.DialogComponent
import com.phew.core_design.LoadingAnimation
import com.phew.core_design.NeutralColor
import com.phew.core_design.TabBar
import com.phew.core_design.TextComponent
import com.phew.core_design.component.refresh.RefreshBox
import com.phew.domain.dto.FollowData
import com.phew.domain.dto.ProfileInfo
import com.phew.profile.ProfileViewModel
import com.phew.profile.R
import com.phew.profile.TAB_FOLLOWER
import com.phew.profile.TAB_FOLLOWING
import com.phew.profile.UiState
import com.phew.profile.component.ProfileComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FollowerScreen(
    viewModel: ProfileViewModel,
    onBackPressed: () -> Unit,
    onLogout: () -> Unit,
    isMyProfileView: Boolean,
    selectTab: Int,
) {
    val uiState by viewModel.uiState.collectAsState()
    val follower = uiState.follow.collectAsLazyPagingItems()
    val following = uiState.following.collectAsLazyPagingItems()
    val context = LocalContext.current
    val snackBarHostState = remember { SnackbarHostState() }
    val refreshState = rememberPullToRefreshState()
    var selectIndex by remember { mutableIntStateOf(selectTab) }
    var unFollowDialogShow by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.event) {
        if (uiState.event is UiState.Success) {
            follower.refresh()
            following.refresh()
        }
    }
    when (val result = uiState.profileInfo) {
        is UiState.Fail -> {
            FollowerTopBar(
                myProfileInfo = uiState.profileInfo,
                onBackPressed = onBackPressed,
                snackBarHostState = snackBarHostState
            ) { paddingValues ->
                LaunchedEffect(result.errorMessage) {
                    when (result.errorMessage) {
                        ERROR_LOGOUT -> {
                            snackBarHostState.showSnackbar(
                                message = context.getString(com.phew.core_design.R.string.error_log_out),
                                duration = SnackbarDuration.Short
                            )
                            onLogout()
                        }

                        ERROR_NETWORK -> {
                            snackBarHostState.showSnackbar(
                                message = context.getString(com.phew.core_design.R.string.error_network),
                                duration = SnackbarDuration.Short
                            )
                        }

                        else -> {
                            snackBarHostState.showSnackbar(
                                message = context.getString(com.phew.core_design.R.string.error_app),
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = NeutralColor.WHITE)
                        .padding(
                            top = paddingValues.calculateTopPadding(),
                            bottom = paddingValues.calculateBottomPadding()
                        ),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(com.phew.core_design.R.drawable.ic_deleted_card),
                        contentDescription = result.errorMessage,
                        modifier = Modifier
                            .height(130.dp)
                            .width(220.dp)
                    )
                }
            }
        }

        UiState.Loading -> {
            LoadingAnimation.LoadingView()
        }

        is UiState.Success -> {
            FollowerTopBar(
                myProfileInfo = uiState.profileInfo,
                onBackPressed = onBackPressed,
                snackBarHostState = snackBarHostState
            ) { paddingValues ->
                RefreshBox(
                    isRefresh = uiState.isRefreshing,
                    onRefresh = remember(viewModel, isMyProfileView, result.data.userId) {
                        {
                            if (isMyProfileView) viewModel.refreshMyProfile() else viewModel.refreshOtherProfile(
                                result.data.userId
                            )
                        }
                    },
                    state = refreshState,
                    paddingValues = paddingValues
                ) {
                    ContentView(
                        followerCnt = follower.itemCount,
                        followIngCnt = following.itemCount,
                        paddingValues = paddingValues,
                        selectIndex = selectIndex,
                        following = following,
                        follow = follower,
                        onFollowingClick = { selectIndex = TAB_FOLLOWING },
                        onFollowerClick = { selectIndex = TAB_FOLLOWER },
                        distanceFraction = refreshState.distanceFraction,
                        density = LocalDensity.current,
                        onChangeFollowClick = remember(selectIndex, isMyProfileView, viewModel) {
                            { data ->
                                when (selectIndex) {
                                    TAB_FOLLOWING -> {
                                        when (isMyProfileView) {
                                            true -> {
                                                unFollowDialogShow = true
                                                viewModel.setFollowUserId(data)
                                            }

                                            false -> {
                                                if (data.isFollowing) viewModel.unFollowUser(data.memberId) else viewModel.followUser(
                                                    data.memberId
                                                )
                                            }
                                        }
                                    }

                                    TAB_FOLLOWER -> {
                                        if (data.isFollowing) viewModel.unFollowUser(data.memberId) else viewModel.followUser(
                                            data.memberId
                                        )
                                    }
                                }
                            }
                        }
                    )
                    if (unFollowDialogShow) {
                        DialogComponent.NoDescriptionButtonTwo(
                            title = stringResource(
                                R.string.follow_dialog_un_follow_content,
                                uiState.nickname
                            ),
                            buttonTextStart = stringResource(com.phew.core_design.R.string.common_close),
                            buttonTextEnd = stringResource(com.phew.core_design.R.string.common_cancel_polite),
                            baseColor = Danger.M_RED,
                            blinkColor = Danger.D_RED,
                            onClick = remember(
                                viewModel::unFollowUser,
                                uiState.userId,
                                isMyProfileView
                            ) {
                                {
                                    viewModel.unFollowUser(
                                        userId = uiState.userId,
                                        isMyProfile = isMyProfileView
                                    )
                                    unFollowDialogShow = false
                                }
                            },
                            onDismiss = { unFollowDialogShow = false }
                        )
                    }
                    HandleErrorView(
                        event = uiState.event,
                        snackBarHostState = snackBarHostState,
                        onLogout = onLogout,
                        context = context
                    )
                }
            }
        }
    }
}

@Composable
private fun FollowerTopBar(
    myProfileInfo: UiState<ProfileInfo>,
    onBackPressed: () -> Unit,
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            AppBar.IconLeftAppBar(
                onClick = onBackPressed,
                appBarText = if (myProfileInfo is UiState.Success) myProfileInfo.data.nickname else ""
            )
        },
        snackbarHost = {
            DialogComponent.CustomAnimationSnackBarHost(hostState = snackBarHostState)
        },
        content = content,
    )
}

@Composable
private fun ContentView(
    paddingValues: PaddingValues,
    selectIndex: Int,
    distanceFraction: Float,
    onFollowerClick: () -> Unit,
    onFollowingClick: () -> Unit,
    following: LazyPagingItems<FollowData>,
    follow: LazyPagingItems<FollowData>,
    density: Density,
    onChangeFollowClick: (FollowData) -> Unit,
    followerCnt: Int,
    followIngCnt: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = NeutralColor.WHITE)
            .graphicsLayer {
                translationY = distanceFraction * with(density) { 56.dp.toPx() }
            }
            .padding(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding(),
                start = 16.dp,
                end = 16.dp
            )
    ) {
        TabBar.TabBarTwo(
            data = listOf(
                stringResource(R.string.follow_txt_tab_follower, followerCnt),
                stringResource(R.string.follow_txt_tab_following, followIngCnt)
            ),
            onFirstItemClick = onFollowerClick,
            onSecondItemClick = onFollowingClick,
            selectTabData = selectIndex
        )
        when (selectIndex) {
            TAB_FOLLOWER -> FollowerView(
                data = follow,
                selectIndex = selectIndex,
                profileClick = onChangeFollowClick,
            )

            TAB_FOLLOWING -> FollowerView(
                data = following,
                selectIndex = selectIndex,
                profileClick = onChangeFollowClick,
            )
        }
    }
}

@Composable
private fun FollowerView(
    data: LazyPagingItems<FollowData>,
    selectIndex: Int,
    profileClick: (FollowData) -> Unit,
) {
    when (val refreshState = data.loadState.refresh) {
        is LoadState.Error -> {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(com.phew.core_design.R.drawable.ic_deleted_card),
                    contentDescription = refreshState.error.message,
                    modifier = Modifier
                        .height(130.dp)
                        .width(220.dp)
                        .padding(bottom = 10.dp)
                )
                Text(
                    text = stringResource(com.phew.core_design.R.string.error_network),
                    style = TextComponent.BODY_1_M_14,
                    color = NeutralColor.GRAY_400
                )
            }
        }

        LoadState.Loading -> {
            LoadingAnimation.LoadingView()
        }

        is LoadState.NotLoading -> {
            when (data.itemCount) {
                0 -> {
                    EmptyView(selectIndex = selectIndex)
                }

                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(
                            count = data.itemCount,
                            key = data.itemKey { user -> user.memberId }
                        ) { index ->
                            val item = data[index]
                            if (item != null) {
                                ProfileComponent.FollowView(
                                    data = item,
                                    onClick = {
                                        profileClick(item)
                                    },
                                    isGrayColor = item.isFollowing,
                                    isButtonShow = !item.isRequester
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyView(selectIndex: Int) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(com.phew.core_design.R.drawable.ic_user_filled),
            contentDescription = "No data",
            colorFilter = ColorFilter.tint(NeutralColor.GRAY_200)
        )
        Text(
            text = if (selectIndex == TAB_FOLLOWER) stringResource(R.string.follow_txt_empty_follower) else stringResource(
                R.string.follow_txt_empty_following
            ),
            style = TextComponent.BODY_1_M_14,
            color = NeutralColor.GRAY_400
        )
    }
}

@Composable
private fun HandleErrorView(
    event: UiState<Unit>,
    snackBarHostState: SnackbarHostState,
    context: Context,
    onLogout: () -> Unit,
) {
    if (event is UiState.Fail) {
        LaunchedEffect(event.errorMessage) {
            when (event.errorMessage) {
                ERROR_LOGOUT -> {
                    snackBarHostState.showSnackbar(
                        message = context.getString(com.phew.core_design.R.string.error_log_out),
                        duration = SnackbarDuration.Short
                    )
                    onLogout()
                }

                ERROR_NETWORK -> {
                    snackBarHostState.showSnackbar(
                        message = context.getString(com.phew.core_design.R.string.error_network),
                        duration = SnackbarDuration.Short
                    )
                }

                else -> {
                    snackBarHostState.showSnackbar(
                        message = context.getString(com.phew.core_design.R.string.error_app),
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }
}