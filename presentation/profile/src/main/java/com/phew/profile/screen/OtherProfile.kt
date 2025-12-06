package com.phew.profile.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_design.AppBar
import com.phew.core_design.CustomFont
import com.phew.core_design.Danger
import com.phew.core_design.DialogComponent
import com.phew.core_design.LoadingAnimation
import com.phew.core_design.MediumButton
import com.phew.core_design.NeutralColor
import com.phew.core_design.TextComponent
import com.phew.core_design.component.card.CommentBodyContent
import com.phew.core_design.component.refresh.RefreshBox
import com.phew.core_design.component.refresh.TOP_CONTENT_OFFSET
import com.phew.core_design.component.refresh.pullToRefreshOffset
import com.phew.domain.dto.ProfileCard
import com.phew.domain.dto.ProfileInfo
import com.phew.profile.ProfileViewModel
import com.phew.profile.R
import com.phew.profile.UiState
import com.phew.profile.component.ProfileComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OtherProfile(
    viewModel: ProfileViewModel,
    userId: Long,
    onLogOut: () -> Unit,
    onBackPress: () -> Unit,
    onClickFollower: () -> Unit,
    onClickFollowing: () -> Unit,
    onClickCard: (Long) -> Unit,
) {
    if (userId == 0L) {
        ErrorView(errorMessage = "Fail to get Profile")
        return
    }
    LaunchedEffect(userId) {
        viewModel.otherProfile(profileId = userId)
    }
    val uiState by viewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val refreshState = rememberPullToRefreshState()
    var showBlockDialog by remember { mutableStateOf(false) }
    HandelEventError(
        event = uiState.event,
        onLogout = onLogOut,
        snackBarHostState = snackBarHostState
    )
    when (val profileState = uiState.profileInfo) {
        is UiState.Fail -> {
            LaunchedEffect(profileState.errorMessage) {
                when (profileState.errorMessage) {
                    ERROR_LOGOUT -> {
                        snackBarHostState.showSnackbar(
                            message = context.getString(com.phew.core_design.R.string.error_log_out),
                            duration = SnackbarDuration.Short
                        )
                        onLogOut()
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
            OtherProfileScaffold(
                onClickBack = remember(onBackPress) { onBackPress },
                snackBarHostState = snackBarHostState,
                isBlock = true,
                showBlockDialog = {}
            ) {
                ErrorView(errorMessage = profileState.errorMessage)
            }
        }

        UiState.Loading -> {
            LoadingAnimation.LoadingView()
        }

        is UiState.Success -> {
            OtherProfileScaffold(
                onClickBack = remember(onBackPress) { onBackPress },
                snackBarHostState = snackBarHostState,
                isBlock = profileState.data.isBlocked,
                showBlockDialog = remember(viewModel::block) { { viewModel.block(userId = userId) } }
            ) { paddingValues ->
                RefreshBox(
                    isRefresh = uiState.isRefreshing,
                    onRefresh = remember(viewModel::refreshOtherProfile) {
                        {
                            viewModel.refreshOtherProfile(
                                profileId = userId
                            )
                        }
                    },
                    state = refreshState,
                    paddingValues = paddingValues
                ){
                    val feedCardData = uiState.profileFeedCard.collectAsLazyPagingItems()
                    ProfileContentView(
                        profile = profileState.data,
                        cardData = feedCardData,
                        onFollowerClick = onClickFollower,
                        onFollowingClick = onClickFollowing,
                        onClickFollow = remember(viewModel) {
                            { userId: Long ->
                                val currentState = viewModel.uiState.value.profileInfo
                                if (currentState is UiState.Success) {
                                    when {
                                        currentState.data.isBlocked -> {
                                            showBlockDialog = true
                                        }

                                        currentState.data.isAlreadyFollowing -> {
                                            viewModel.unFollowUser(
                                                userId = userId,
                                                isRefresh = true,
                                                isMyProfile = false
                                            )
                                        }

                                        !currentState.data.isAlreadyFollowing -> {
                                            viewModel.followUser(
                                                userId = userId,
                                                isRefresh = true,
                                                isMyProfile = false
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        onClickCard = onClickCard,
                        onLogout = onLogOut,
                        snackBarHostState = snackBarHostState,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(NeutralColor.WHITE)
                            .pullToRefreshOffset(
                                state = refreshState,
                                baseOffset = TOP_CONTENT_OFFSET
                            )
                            .padding(
                                top = paddingValues.calculateTopPadding(),
                                bottom = paddingValues.calculateBottomPadding()
                            ),
                        buttonIsEnable = uiState.event !is UiState.Loading
                    )
                    if (showBlockDialog) {
                        DialogComponent.DefaultButtonTwo(
                            title = stringResource(R.string.profile_dialog_un_block_title),
                            description = stringResource(R.string.profile_dialog_un_block_content , profileState.data.nickname),
                            buttonTextStart = stringResource(com.phew.core_design.R.string.common_cancel),
                            buttonTextEnd = stringResource(R.string.profile_dialog_un_block_btn_okay),
                            onClick = remember(viewModel::unBlock) {
                                {
                                    viewModel.unBlock(userId = userId)
                                    showBlockDialog = false
                                }
                            },
                            onDismiss = {
                                showBlockDialog = false
                            },
                            rightButtonBaseColor = Danger.M_RED,
                            rightButtonClickColor = Danger.D_RED,
                            startButtonTextColor = NeutralColor.BLACK,
                            endButtonTextColor = NeutralColor.WHITE
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OtherProfileScaffold(
    onClickBack: () -> Unit,
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
    isBlock: Boolean = true,
    showBlockDialog: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        snackbarHost = {
            DialogComponent.CustomAnimationSnackBarHost(hostState = snackBarHostState)
        },
        topBar = {
            if (isBlock) {
                AppBar.IconLeftAppBar(
                    onClick = onClickBack,
                    appBarText = ""
                )
            } else {
                AppBar.TextButtonAppBarText(
                    onClick = onClickBack,
                    appBarText = "",
                    buttonText = stringResource(R.string.profile_top_block),
                    buttonTextColor = NeutralColor.BLACK,
                    onButtonClick = showBlockDialog,
                )
            }
        },
        content = content
    )
}

@Composable
private fun ProfileContentView(
    profile: ProfileInfo,
    cardData: LazyPagingItems<ProfileCard>,
    onFollowerClick: () -> Unit,
    onFollowingClick: () -> Unit,
    onClickFollow: (userId: Long) -> Unit,
    onClickCard: (Long) -> Unit,
    onLogout: () -> Unit,
    snackBarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    buttonIsEnable: Boolean,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            ProfileView(
                profile = profile,
                onFollowerClick = onFollowerClick,
                onFollowingClick = onFollowingClick,
                onClickFollow = onClickFollow,
                buttonIsEnable = buttonIsEnable
            )
        }
        when (cardData.loadState.refresh) {
            is LoadState.Error -> {
                val error = (cardData.loadState.refresh as LoadState.Error).error
                item(span = { GridItemSpan(maxLineSpan) }) {
                    HandleErrorMessage(
                        error.message ?: ERROR_NETWORK,
                        onLogout = onLogout,
                        snackBarHostState = snackBarHostState
                    )
                }
            }

            LoadState.Loading -> {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LoadingAnimation.LoadingView()
                }
            }

            is LoadState.NotLoading -> {
                when (cardData.itemCount) {
                    0 -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            EmptyCardView(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .heightIn(382.dp)
                            )
                        }
                    }

                    else -> {
                        items(
                            count = cardData.itemCount,
                            key = { index ->
                                val id = cardData.peek(index)?.cardId ?: "loading"
                                "${id}_$index"
                            }
                        ) { index ->
                            val item = cardData[index]
                            if (item != null) {
                                CommentBodyContent(
                                    contentText = item.cardContent,
                                    imgUrl = item.cardImgUrl,
                                    fontFamily = CustomFont.findFontValueByServerName(item.font).data.previewTypeface,
                                    textMaxLines = 4,
                                    cardId = item.cardId,
                                    onClick = onClickCard
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
private fun HandleErrorMessage(
    msg: String,
    snackBarHostState: SnackbarHostState,
    onLogout: () -> Unit,
) {
    val networkErrorMsg =
        stringResource(com.phew.core_design.R.string.error_network)
    val appErrorMsg =
        stringResource(com.phew.core_design.R.string.error_app)
    LaunchedEffect(msg) {
        when (msg) {
            ERROR_NETWORK -> {
                snackBarHostState.showSnackbar(
                    message = networkErrorMsg,
                    duration = SnackbarDuration.Short
                )
            }

            ERROR_LOGOUT -> onLogout()

            else -> {
                snackBarHostState.showSnackbar(
                    message = appErrorMsg,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }
}

@Composable
private fun ProfileView(
    profile: ProfileInfo,
    onFollowerClick: () -> Unit,
    onFollowingClick: () -> Unit,
    onClickFollow: (userId: Long) -> Unit,
    buttonIsEnable: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = NeutralColor.WHITE)
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.profile_txt_visit_total),
                        style = TextComponent.CAPTION_2_M_12,
                        color = NeutralColor.GRAY_400,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = profile.totalVisitCnt.toString(),
                        style = TextComponent.CAPTION_2_M_12,
                        color = NeutralColor.GRAY_400,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Image(
                        painter = painterResource(com.phew.core_design.R.drawable.ic_spot),
                        modifier = Modifier
                            .size(3.dp),
                        colorFilter = ColorFilter.tint(color = NeutralColor.GRAY_400),
                        contentDescription = stringResource(R.string.profile_txt_visit_total) + profile.totalVisitCnt.toString() + stringResource(
                            R.string.profile_txt_visit_today
                        ) + profile.todayVisitCnt.toString()
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.profile_txt_visit_today),
                        style = TextComponent.CAPTION_2_M_12,
                        color = NeutralColor.GRAY_400,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = profile.todayVisitCnt.toString(),
                        style = TextComponent.CAPTION_2_M_12,
                        color = NeutralColor.GRAY_400,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
                Text(
                    text = profile.nickname,
                    style = TextComponent.HEAD_3_B_20,
                    color = NeutralColor.BLACK
                )
            }
            AsyncImage(
                model = profile.profileImageUrl.ifEmpty { com.phew.core_design.R.drawable.ic_profile },
                contentDescription = "profile image",
                modifier = Modifier
                    .size(60.dp)
                    .border(
                        width = 1.dp,
                        color = NeutralColor.GRAY_300,
                        shape = RoundedCornerShape(100.dp)
                    )
                    .clip(shape = RoundedCornerShape(size = 100.dp)),
                contentScale = ContentScale.Crop
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProfileComponent.CardFollowerView(
                title = stringResource(R.string.profile_txt_card),
                data = profile.cardCnt.toString(),
                onClick = { }
            )
            ProfileComponent.CardFollowerView(
                title = stringResource(R.string.profile_txt_follower),
                data = profile.followerCnt.toString(),
                onClick = onFollowerClick
            )
            ProfileComponent.CardFollowerView(
                title = stringResource(R.string.profile_txt_following),
                data = profile.followingCnt.toString(),
                onClick = onFollowingClick
            )
        }
        MediumButton.NoIconPrimary(
            onClick = {
                onClickFollow(profile.userId)
            },
            buttonText = when {
                profile.isBlocked -> {
                    stringResource(R.string.profile_btn_block_release)
                }

                profile.isAlreadyFollowing -> {
                    stringResource(R.string.profile_txt_following)
                }

                else -> {
                    stringResource(R.string.follow_btn_follow)
                }
            },
            textColor = if(profile.isAlreadyFollowing) NeutralColor.BLACK else NeutralColor.WHITE,
            baseColor =if(profile.isAlreadyFollowing) NeutralColor.GRAY_100 else NeutralColor.BLACK,
            isEnable = buttonIsEnable
        )
    }
}

@Composable
private fun EmptyCardView(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(com.phew.core_design.R.drawable.ic_card_filled),
            colorFilter = ColorFilter.tint(NeutralColor.GRAY_200),
            contentDescription = "no card",
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = stringResource(
                R.string.profile_txt_no_card
            ),
            style = TextComponent.BODY_1_M_14,
            color = NeutralColor.GRAY_400
        )
    }
}

@Composable
private fun ErrorView(errorMessage: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = NeutralColor.WHITE)
    ) {
        Image(
            painter = painterResource(com.phew.core_design.R.drawable.ic_deleted_card),
            contentDescription = errorMessage,
            modifier = Modifier
                .height(130.dp)
                .width(220.dp)
                .align(Alignment.Center)
        )
    }
}

@Composable
private fun HandelEventError(
    event: UiState<Unit>,
    onLogout: () -> Unit,
    snackBarHostState: SnackbarHostState,
) {
    if (event is UiState.Fail) {
        val networkErrorMsg =
            stringResource(com.phew.core_design.R.string.error_network)
        val appErrorMsg =
            stringResource(com.phew.core_design.R.string.error_app)
        LaunchedEffect(event.errorMessage) {
            when (event.errorMessage) {
                ERROR_NETWORK -> {
                    snackBarHostState.showSnackbar(
                        message = networkErrorMsg,
                        duration = SnackbarDuration.Short
                    )
                }

                ERROR_LOGOUT -> onLogout()

                else -> {
                    snackBarHostState.showSnackbar(
                        message = appErrorMsg,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }
}
