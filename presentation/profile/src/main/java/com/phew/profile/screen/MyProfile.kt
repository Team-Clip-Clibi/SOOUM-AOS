package com.phew.profile.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.phew.core_common.ERROR_LOGOUT
import com.phew.core_common.ERROR_NETWORK
import com.phew.core_design.AppBar
import com.phew.core_design.DialogComponent.SnackBar
import com.phew.core_design.MediumButton
import com.phew.core_design.NeutralColor
import com.phew.core_design.TextComponent
import com.phew.domain.dto.MyProfileInfo
import com.phew.domain.dto.ProfileCard
import com.phew.profile.ProfileViewModel
import com.phew.profile.R
import com.phew.profile.TAB_MY_COMMENT_CARD
import com.phew.profile.TAB_MY_FEED_CARD
import com.phew.profile.UiState
import com.phew.profile.component.ProfileTab
import kotlinx.coroutines.flow.Flow
import com.phew.core_design.component.card.CommentBodyContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
internal fun MyProfile(
    viewModel: ProfileViewModel = hiltViewModel(),
    onClickFollower: () -> Unit,
    onClickFollowing: () -> Unit,
    onClickSetting: () -> Unit,
    onClickCard: (Long) -> Unit,
    onLogout: () -> Unit,
    onEditProfileClick: () -> Unit,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val selectIndex by remember { mutableIntStateOf(TAB_MY_FEED_CARD) }
    val snackBarHostState = remember { SnackbarHostState() }
    when (val profileState = uiState.myProfileInfo) {
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
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        UiState.Loading -> {
            LoadingView()
        }

        is UiState.Success -> {
            MyProfileScaffold(onClickSetting = onClickSetting) { paddingValues ->
                Column {
                    MyProfileView(
                        paddingValues = paddingValues,
                        profile = profileState.data,
                        onFollowerClick = onClickFollower,
                        onFollowingClick = onClickFollowing,
                        onCommentCardClick = {

                        },
                        onFeedCardClick = {

                        },
                        onEditProfileClick = onEditProfileClick
                    )
                }
                when (selectIndex) {
                    TAB_MY_FEED_CARD -> {
                        ProfileFeedCardView(
                            profileFeedCard = uiState.profileFeedCard,
                            snackBarHostState = snackBarHostState,
                            onLogout = onLogout
                        )
                    }

                    TAB_MY_COMMENT_CARD -> {
                        ProfileFeedCardView(
                            profileFeedCard = uiState.profileCommentCard,
                            snackBarHostState = snackBarHostState,
                            onLogout = onLogout
                        )
                    }
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
            SnackbarHost(
                hostState = snackBarHostState,
                snackbar = { snackBarData ->
                    SnackBar(data = snackBarData)
                }
            )
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
private fun LoadingView() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(com.phew.core_design.R.raw.ic_refresh)
    )
    val refreshProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { refreshProgress },
            modifier = Modifier.size(44.dp)
        )
    }
}

@Composable
private fun MyProfileView(
    paddingValues: PaddingValues,
    profile: MyProfileInfo,
    onFollowerClick: () -> Unit,
    onFollowingClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onFeedCardClick: () -> Unit,
    onCommentCardClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = NeutralColor.WHITE)
            .padding(top = paddingValues.calculateTopPadding() + 12.dp, start = 16.dp, end = 16.dp)
    ) {
        //방문자 수 + 닉네임 + 프로필 이미지
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Row {
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
                contentScale = ContentScale.FillBounds
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
            CardFollowerView(
                title = stringResource(R.string.profile_txt_card),
                data = profile.cardCnt.toString(),
                onClick = {

                }
            )
            CardFollowerView(
                title = stringResource(R.string.profile_txt_follower),
                data = profile.followerCnt.toString(),
                onClick = remember(onFollowerClick) { onFollowerClick }
            )
            CardFollowerView(
                title = stringResource(R.string.profile_txt_following),
                data = profile.followingCnt.toString(),
                onClick = remember(onFollowingClick) { onFollowingClick }
            )
        }
        MediumButton.NoIconSecondary(
            buttonText = stringResource(R.string.profile_btn_edit_profile),
            onClick = remember(onEditProfileClick) { onEditProfileClick },
        )
        Spacer(modifier = Modifier.height(16.dp))
        val selectTabRow = remember { mutableIntStateOf(TAB_MY_FEED_CARD) }
        ProfileTab(
            selectTabData = selectTabRow.intValue,
            onFeedCardClick = {
                selectTabRow.intValue = TAB_MY_FEED_CARD
                onFeedCardClick()
            },
            onCommentCardClick = {
                selectTabRow.intValue = TAB_MY_COMMENT_CARD
                onCommentCardClick()
            }
        )
    }
}

@Composable
private fun ProfileFeedCardView(
    profileFeedCard: Flow<PagingData<ProfileCard>>,
    snackBarHostState: SnackbarHostState,
    onLogout: () -> Unit,
) {
    val cardData = profileFeedCard.collectAsLazyPagingItems()
    val networkErrorMsg = stringResource(com.phew.core_design.R.string.error_network)
    val appErrorMsg = stringResource(com.phew.core_design.R.string.error_app)
    when {
        cardData.loadState.refresh is LoadState.Loading -> {
            LoadingView()
        }

        cardData.loadState.refresh is LoadState.Error -> {
            val error = (cardData.loadState.refresh as LoadState.Error).error
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

        cardData.itemCount == 0 -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = NeutralColor.WHITE)
            ) {
                Image(
                    painter = painterResource(com.phew.core_design.R.drawable.ic_deleted_card),
                    contentDescription = "no data",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = NeutralColor.WHITE)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(1.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    items(
                        count = cardData.itemCount,
                        key = cardData.itemKey { data -> data.cardId }
                    ) { index ->
                        val item = cardData[index]
                        if (item != null) {
                            CommentBodyContent(
                                contentText = item.cardContent,
                                imgUrl = item.cardImgUrl,
                                fontFamily = FontFamily(Font(com.phew.core_design.R.font.medium)),
                                textMaxLines = 3
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardFollowerView(
    title: String,
    data: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(72.dp)
            .height(64.dp)
            .padding(top = 8.dp, bottom = 8.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = title,
            style = TextComponent.BODY_1_M_14,
            color = NeutralColor.GRAY_500
        )
        Text(
            text = data,
            style = TextComponent.TITLE_1_SB_18,
            color = NeutralColor.BLACK
        )
    }
}
