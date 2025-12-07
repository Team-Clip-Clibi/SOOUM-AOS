package com.phew.profile.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_design.AppBar
import com.phew.core_design.MediumButton
import com.phew.core_design.NeutralColor
import com.phew.core_design.TextComponent
import com.phew.domain.dto.ProfileInfo
import com.phew.profile.ProfileViewModel
import com.phew.profile.R
import com.phew.profile.TAB_MY_COMMENT_CARD
import com.phew.profile.TAB_MY_FEED_CARD
import com.phew.profile.UiState
import com.phew.core_design.component.card.CommentBodyContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Alignment
import androidx.paging.compose.LazyPagingItems
import com.phew.core_common.BOTTOM_NAVIGATION_HEIGHT
import com.phew.core_design.CustomFont
import com.phew.core_design.DialogComponent
import com.phew.core_design.LoadingAnimation
import com.phew.core_design.component.refresh.RefreshBox
import com.phew.domain.dto.ProfileCard
import com.phew.profile.component.ProfileComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MyProfile(
    viewModel: ProfileViewModel,
    onClickFollower: () -> Unit,
    onClickFollowing: () -> Unit,
    onClickSetting: () -> Unit,
    onClickCard: (Long) -> Unit,
    onLogout: () -> Unit,
    onEditProfileClick: () -> Unit,
) {
    LaunchedEffect(Unit) {
        viewModel.myProfile()
    }
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var selectIndex by remember { mutableIntStateOf(TAB_MY_FEED_CARD) }
    val snackBarHostState = remember { SnackbarHostState() }
    val refreshState = rememberPullToRefreshState()
    when (val profileState = uiState.profileInfo) {
        is UiState.Fail -> {
            MyProfileScaffold(
                onClickSetting = onClickSetting,
                snackBarHostState = snackBarHostState
            ) {
                LaunchedEffect(profileState.errorMessage) {
                    when (profileState.errorMessage) {
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = NeutralColor.WHITE)
                ) {
                    Image(
                        painter = painterResource(com.phew.core_design.R.drawable.ic_deleted_card),
                        contentDescription = profileState.errorMessage,
                        modifier = Modifier
                            .height(130.dp)
                            .width(220.dp)
                            .align(Alignment.Center)
                    )
                }
            }
        }

        UiState.Loading -> {
            LoadingAnimation.LoadingView()
        }

        is UiState.Success -> {
            val feedCardData = uiState.profileFeedCard.collectAsLazyPagingItems()
            val commentCardData = uiState.profileCommentCard.collectAsLazyPagingItems()
            val cardData = if (selectIndex == TAB_MY_FEED_CARD) feedCardData else commentCardData
            MyProfileScaffold(onClickSetting = onClickSetting) { paddingValues ->
                RefreshBox(
                    isRefresh = uiState.isRefreshing,
                    onRefresh = viewModel::refreshMyProfile,
                    state = refreshState,
                    paddingValues = paddingValues
                ) {
                    ProfileCardView(
                        profile = profileState.data,
                        cardData = cardData,
                        selectIndex = selectIndex,
                        onFollowerClick = onClickFollower,
                        onFollowingClick = onClickFollowing,
                        onEditProfileClick = onEditProfileClick,
                        onFeedCardClick = { selectIndex = TAB_MY_FEED_CARD },
                        onCommentCardClick = { selectIndex = TAB_MY_COMMENT_CARD },
                        onClickCard = onClickCard,
                        onLogout = onLogout,
                        snackBarHostState = snackBarHostState,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(NeutralColor.WHITE)
                            .graphicsLayer {
                                this.translationY = refreshState.distanceFraction * 72.dp.toPx()
                            }
                            .padding(top = paddingValues.calculateTopPadding()),
                        paddingValues = paddingValues,
                        onCardClick = { selectIndex = TAB_MY_FEED_CARD },
                    )
                }
            }
        }
    }
}

@Composable
private fun MyProfileScaffold(
    onClickSetting: () -> Unit,
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        snackbarHost = {
            DialogComponent.CustomAnimationSnackBarHost(hostState = snackBarHostState)
        },
        topBar = {
            AppBar.IconRightAppBar(
                title = stringResource(R.string.profile_top_bar),
                onClick = remember(onClickSetting) { onClickSetting }
            )
        },
        content = content
    )
}

@Composable
private fun MyProfileView(
    profile: ProfileInfo,
    onFollowerClick: () -> Unit,
    onFollowingClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onCardClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = NeutralColor.WHITE)
            .padding(start = 16.dp, end = 16.dp, top = 12.dp)
    ) {
        //방문자 수 + 닉네임 + 프로필 이미지
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
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
                            .size(3.dp)
                            .padding(1.dp),
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
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = profile.nickname,
                    style = TextComponent.HEAD_3_B_20,
                    color = NeutralColor.BLACK
                )
            }
            AsyncImage(
                model = if (profile.profileImgName.isEmpty() || profile.profileImageUrl.isEmpty()) com.phew.core_design.R.drawable.ic_profile else profile.profileImageUrl,
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
        // 카드 , 팔로워, 팔로잉 숫자
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
                onClick = {
                    if(profile.cardCnt != 0){
                        onCardClick()
                    }
                }
            )
            ProfileComponent.CardFollowerView(
                title = stringResource(R.string.profile_txt_follower),
                data = profile.followerCnt.toString(),
                onClick = remember(onFollowerClick) { onFollowerClick }
            )
            ProfileComponent.CardFollowerView(
                title = stringResource(R.string.profile_txt_following),
                data = profile.followingCnt.toString(),
                onClick = remember(onFollowingClick) { onFollowingClick }
            )
        }
        MediumButton.NoIconSecondary(
            buttonText = stringResource(R.string.profile_btn_edit_profile),
            onClick = remember(onEditProfileClick) { onEditProfileClick },
        )
    }
}

@Composable
private fun EmptyCardView(
    selectIndex: Int,
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
            text = if (selectIndex == TAB_MY_FEED_CARD) stringResource(
                R.string.profile_txt_no_card
            ) else stringResource(
                R.string.profile_txt_no_comment_card
            ),
            style = TextComponent.BODY_1_M_14,
            color = NeutralColor.GRAY_400
        )
    }
}

@Composable
private fun ProfileCardView(
    profile: ProfileInfo,
    cardData: LazyPagingItems<ProfileCard>,
    selectIndex: Int,
    onFollowerClick: () -> Unit,
    onFollowingClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onFeedCardClick: () -> Unit,
    onCommentCardClick: () -> Unit,
    onClickCard: (Long) -> Unit,
    onLogout: () -> Unit,
    snackBarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    onCardClick: () -> Unit
) {
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier,
        state = gridState,
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
        contentPadding = PaddingValues(
            top = 0.dp,
            bottom = paddingValues.calculateBottomPadding() + BOTTOM_NAVIGATION_HEIGHT.dp
        )
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            MyProfileView(
                profile = profile,
                onEditProfileClick = onEditProfileClick,
                onFollowingClick = onFollowingClick,
                onFollowerClick = onFollowerClick,
                onCardClick = {
                    onCardClick()
                    coroutineScope.launch {
                        delay(100)
                        gridState.animateScrollToItem(index = 1)
                    }
                }
            )
        }
        stickyHeader {
            ProfileComponent.CardTabView(
                onCommentCardClick = onCommentCardClick,
                onFeedCardClick = onFeedCardClick,
                selectIndex = selectIndex
            )
        }
        when (cardData.loadState.refresh) {
            LoadState.Loading -> {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LoadingAnimation.LoadingView(modifier = Modifier.padding(top = 80.dp))
                }
            }

            is LoadState.Error -> {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    val networkErrorMsg =
                        stringResource(com.phew.core_design.R.string.error_network)
                    val appErrorMsg =
                        stringResource(com.phew.core_design.R.string.error_app)
                    val error =
                        (cardData.loadState.refresh as LoadState.Error).error
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
                        else -> {
                            LaunchedEffect(error.message) {
                                snackBarHostState.showSnackbar(
                                    message = appErrorMsg,
                                    withDismissAction = true
                                )
                            }
                        }
                    }
                }
            }

            else -> {
                when (cardData.itemCount) {
                    0 -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            EmptyCardView(
                                selectIndex = selectIndex,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 318.dp)
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
                                Box(modifier = Modifier.padding(bottom = 1.dp)) {
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
        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight)
                    .background(NeutralColor.WHITE) // 배경색 맞춤
            )
        }
    }
}

