package com.phew.presentation.detail.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.phew.core.ui.model.navigation.CardDetailCommentArgs
import com.phew.core_common.TimeUtils
import com.phew.core_common.log.SooumLog
import com.phew.core_design.AppBar
import com.phew.core_design.BottomSheetComponent
import com.phew.core_design.BottomSheetItem
import com.phew.core_design.Danger
import com.phew.core_design.DialogComponent
import com.phew.core_design.NeutralColor
import com.phew.core_design.Primary
import com.phew.core_design.TextComponent
import com.phew.core_design.UnKnowColor
import com.phew.core_design.component.card.CardViewComment
import com.phew.domain.dto.CardComment
import com.phew.domain.dto.CardDetail
import com.phew.core_design.component.card.CardDetail as CardDetailComponent
import com.phew.presentation.detail.R
import com.phew.presentation.detail.component.CardDetailBottom
import com.phew.presentation.detail.component.CardDetailHeader
import com.phew.presentation.detail.model.MoreAction
import com.phew.presentation.detail.viewmodel.CardDetailError
import com.phew.presentation.detail.viewmodel.CardDetailViewModel
import kotlinx.coroutines.delay
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CommentCardDetailScreen(
    args: CardDetailCommentArgs,
    viewModel: CardDetailViewModel = hiltViewModel(),
    onNavigateToComment: (CardDetailCommentArgs) -> Unit,
    onNavigateToWrite: (Long) -> Unit,
    onNavigateToReport: (Long) -> Unit,
    onBackPressed: (Long) -> Unit,
    onFeedPressed: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val comments: LazyPagingItems<CardComment> = viewModel.commentsPagingData
        .collectAsLazyPagingItems()
    val cardDetail = uiState.cardDetail
    LaunchedEffect(args.cardId) {
        SooumLog.d(TAG, "CardId : ${args.cardId}")
        viewModel.loadCardDetail(args.cardId)
    }
    val isRefreshing by remember(uiState.isLoading) {
        derivedStateOf { uiState.isLoading }
    }
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(com.phew.core_design.R.raw.ic_refresh)
    )
    val refreshProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = com.airbnb.lottie.compose.LottieConstants.IterateForever,
        restartOnPlay = isRefreshing
    )
    if (uiState.isLoading && cardDetail == null) {
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
        return
    }
    if (cardDetail == null) {
        return
    }
    var isTimerExpired by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val isDelete = uiState.deleteSuccess || isTimerExpired
    val snackBarHostState = remember { SnackbarHostState() }
    val onBackPressedLambda = remember(args.parentId) { { onBackPressed(args.parentId) } }
    val showBottomSheetLambda = remember { { showBottomSheet = true } }
    val onExpireLambda = remember { { isTimerExpired = true } }
    val onClickLikeLambda = remember(args.cardId) { { viewModel.toggleLike(args.cardId) } }
    val onClickPreviousCard = remember { { onBackPressed(args.parentId) } }
    val onCommentClickLambda: (Long) -> Unit = remember(args.cardId) {
        { childId ->
            onNavigateToComment(
                CardDetailCommentArgs(
                    parentId = args.cardId,
                    cardId = childId
                )
            )
        }
    }
    val onWriteClickLambda = remember(args.cardId) { { onNavigateToWrite(args.cardId) } }
    val closeBottomSheetLambda = remember { { showBottomSheet = false } }
    val onNavigateToReportLambda =
        remember(cardDetail.memberId) { { onNavigateToReport(cardDetail.memberId) } }
    val onBlockMemberLambda = remember(cardDetail.memberId, cardDetail.nickname) {
        { viewModel.blockMember(cardDetail.memberId, cardDetail.nickname) }
    }
    val deleteCardLambda = remember(args.cardId) { { viewModel.requestDeleteCard(args.cardId) } }
    val clearBlockSuccessLambda = remember { { viewModel.clearBlockSuccess() } }
    val unBlockMemberLambda = remember { { viewModel.unblockMember() } }
    val clearErrorLambda =
        remember { { viewModel.clearError() } }
    val onRefresh = remember(args.cardId) {
        {
            viewModel.loadCardDetail(args.cardId)
            viewModel.requestComment(args.cardId)
        }
    }
    HandleBlockUser(
        blockSuccess = uiState.blockSuccess,
        nickName = cardDetail.nickname,
        clearBlockSuccess = clearBlockSuccessLambda,
        unBlockMember = unBlockMemberLambda,
        snackBarHostState = snackBarHostState
    )
    val errorType = uiState.error
    if (errorType != null) {
        HandleError(
            errorType = errorType,
            snackBarHostState = snackBarHostState,
            onDismissError = clearErrorLambda
        )
    }
    val refreshState = rememberPullToRefreshState()
    val density = LocalDensity.current
    Scaffold(
        topBar = {
            TopLayout(
                storyExpirationTime = if (cardDetail.storyExpirationTime == null) "0" else cardDetail.endTime.toString(),
                onFeedPressed = onFeedPressed,
                onBackPressed = onBackPressedLambda,
                showBottomSheet = showBottomSheetLambda,
                onExpire = onExpireLambda,
                memberId = cardDetail.memberId
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState) { data ->
                DialogComponent.SnackBar(data)
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefresh,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxWidth(),
            state = refreshState,
            indicator = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(top = paddingValues.calculateTopPadding()),
                    contentAlignment = Alignment.Center
                ) {
                    val progress =
                        if (isRefreshing) refreshProgress else refreshState.distanceFraction
                    if (isRefreshing || refreshState.distanceFraction > 0f) {
                        LottieAnimation(
                            composition = composition,
                            progress = { progress },
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = NeutralColor.WHITE)
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding()
                    )
            ) {
                CardView(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationY =
                                refreshState.distanceFraction * with(density) { 72.dp.toPx() }
                        },
                    cardDetail = cardDetail,
                    isExpire = isDelete,
                    onClickLike = onClickLikeLambda,
                    onClickCommentIcon = onWriteClickLambda,
                    comments = comments,
                    onCommentClick = onCommentClickLambda,
                    onPreviousCardClick = onClickPreviousCard,
                    playProgression = {
                        LottieAnimation(
                            composition = composition,
                            progress = { refreshProgress },
                            modifier = Modifier.size(44.dp)
                        )
                    }
                )
                PlusButton(
                    modifier = Modifier.align(Alignment.BottomEnd),
                    onWriteClick = onWriteClickLambda,
                    paddingValues = paddingValues
                )
                if (showBottomSheet) {
                    BottomSheetView(
                        isOwnCard = cardDetail.isOwnCard,
                        nickName = cardDetail.nickname,
                        closeBottomSheet = closeBottomSheetLambda,
                        onNavigateToReport = onNavigateToReportLambda,
                        onBlockMember = onBlockMemberLambda,
                        deleteCard = deleteCardLambda
                    )
                }
            }
        }
    }
}

@Composable
private fun TopLayout(
    storyExpirationTime: String,
    memberId: Long,
    onFeedPressed: () -> Unit,
    onBackPressed: () -> Unit,
    showBottomSheet: () -> Unit,
    onExpire: () -> Unit,
) {
    var remainingTimeMillis by remember {
        mutableLongStateOf(storyExpirationTime.toLong())
    }
    var isExpired by remember { mutableStateOf(false) }
    LaunchedEffect(memberId) {
        if (remainingTimeMillis > 0) {
            while (remainingTimeMillis > 0) {
                delay(1000L)
                remainingTimeMillis -= 1000L
            }
            isExpired = true
        }
    }
    if (isExpired) onExpire()
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        AppBar.IconBothAppBar(
            topAppBarText = stringResource(R.string.card_detail_comment_app_bar_title),
            onBackClick = onBackPressed,
            onSecClick = onFeedPressed,
            onLastClick = showBottomSheet
        )
        Text(
            text = if (remainingTimeMillis.toString().trim() == "0") {
                ""
            } else {
                TimeUtils.formatMillisToTimer(remainingTimeMillis)
            },
            color = Primary.DARK,
            style = TextComponent.CAPTION_3_M_10,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}


@Composable
private fun CardView(
    modifier: Modifier,
    cardDetail: CardDetail,
    isExpire: Boolean,
    onClickLike: () -> Unit,
    onClickCommentIcon: () -> Unit,
    comments: LazyPagingItems<CardComment>,
    onCommentClick: (Long) -> Unit,
    onPreviousCardClick: () -> Unit,
    playProgression : @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(color = NeutralColor.WHITE)
    ) {
        CardDetailComponent(
            modifier = Modifier.weight(1f),
            previousCommentThumbnailUri = cardDetail.previousCardImgUrl,
            cardContent = cardDetail.cardContent,
            cardThumbnailUri = cardDetail.cardImgUrl,
            cardTags = cardDetail.tags.map { data -> data.name },
            isDeleted = isExpire,
            backgroundImageUrl = cardDetail.cardImgUrl.toUri(),
            header = {
                CardDetailHeader(
                    profileUri = cardDetail.profileImgUrl ?: "",
                    nickName = cardDetail.nickname,
                    distance = cardDetail.distance ?: "",
                    createAt = cardDetail.createdAt
                )
            },
            bottom = {
                CardDetailBottom(
                    likeCnt = cardDetail.likeCount,
                    commentCnt = cardDetail.commentCardCount,
                    searchCnt = cardDetail.visitedCnt,
                    isLikeCard = cardDetail.isLike,
                    onClickLike = onClickLike,
                    onClickComment = onClickCommentIcon
                )
            },
            onPreviousCardClick = onPreviousCardClick
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(236.dp) // 높이 고정
                .background(color = NeutralColor.GRAY_100),
            contentAlignment = Alignment.Center // 기본 정렬을 Center로
        ) {
            val loadState = comments.loadState

            // Paging의 첫 로드 상태 (refresh) 확인
            when (loadState.refresh) {
                // (A) 로딩 중
                is LoadState.Loading -> {
                    playProgression()
                }

                // (B) 로드 성공
                is LoadState.NotLoading -> {
                    // 로드 성공 후 아이템이 0개일 때 (빈 상태)
                    if (comments.itemCount == 0) {
                        Text(
                            text = stringResource(R.string.card_no_comment),
                            style = TextComponent.BODY_1_M_14,
                            color = NeutralColor.GRAY_400,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // (C) 아이템이 1개 이상 있을 때 (리스트 표시)
                        Column(
                            modifier = Modifier
                                .fillMaxSize() // Box 안을 꽉 채움
                                .background(color = NeutralColor.GRAY_100)
                                .padding(top = 10.dp, bottom = 10.dp),
                        ) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
                            ) {
                                // 3. Paging 전용 items 확장 함수 사용
                                items(
                                    count = comments.itemCount,
                                    key = comments.itemKey { it.cardId } // 키 설정
                                ) { index ->
                                    val comment = comments[index] // 인덱스로 아이템 가져오기

                                    if (comment != null) {
                                        CardViewComment(
                                            contentText = comment.cardContent,
                                            thumbnailUri = comment.cardImgUrl,
                                            distance = comment.distance ?: "",
                                            createAt = TimeUtils.getRelativeTimeString(comment.createdAt),
                                            likeCnt = comment.likeCount.toString(),
                                            commentCnt = comment.commentCardCount.toString(),
                                            font = comment.font,
                                            onClick = {
                                                onCommentClick(comment.cardId)
                                            }
                                        )
                                    } else {
                                        // (선택 사항) Placeholder UI
                                    }
                                }

                                // (선택 사항) 다음 페이지 로드 중일 때 인디케이터 표시
                                if (loadState.append is LoadState.Loading) {
                                    item {
                                        Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                                            playProgression()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // (D) 첫 로드 실패
                is LoadState.Error -> {
                    // (선택 사항) 에러 메시지 또는 재시도 버튼 표시
                    Text(
                        text = "댓글을 불러오는데 실패했습니다.",
                        style = TextComponent.BODY_1_M_14,
                        color = NeutralColor.GRAY_400,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


@Composable
private fun BottomSheetView(
    isOwnCard: Boolean,
    nickName: String,
    closeBottomSheet: () -> Unit,
    onNavigateToReport: () -> Unit,
    onBlockMember: () -> Unit,
    deleteCard: () -> Unit,
) {
    var showBlockDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    BottomSheetComponent.BottomSheet(
        data = if (isOwnCard) {
            arrayListOf(
                BottomSheetItem(
                    id = MoreAction.DELETE.ordinal,
                    title = stringResource(id = R.string.card_detail_delete),
                    image = com.phew.core_design.R.drawable.ic_trash_stoke,
                    imageColor = Danger.M_RED,
                    textColor = Danger.M_RED,
                )
            )
        } else {
            arrayListOf(
                BottomSheetItem(
                    id = MoreAction.BLOCK.ordinal,
                    title = stringResource(id = R.string.card_detail_block),
                    image = com.phew.core_design.R.drawable.ic_eye,
                    textColor = NeutralColor.GRAY_500,
                    imageColor = NeutralColor.BLACK
                ),
                BottomSheetItem(
                    id = MoreAction.DANGER.ordinal,
                    title = stringResource(id = R.string.card_detail_report),
                    image = com.phew.core_design.R.drawable.ic_flag_stoke,
                    imageColor = Danger.M_RED,
                    textColor = Danger.M_RED,
                )
            )
        },
        onItemClick = { id ->
            when (id) {
                MoreAction.BLOCK.ordinal -> {
                    closeBottomSheet()
                    showBlockDialog = true
                }

                MoreAction.DANGER.ordinal -> {
                    closeBottomSheet()
                    onNavigateToReport()
                }

                MoreAction.DELETE.ordinal -> {
                    closeBottomSheet()
                    showDeleteDialog = true
                }
            }
        },
        onDismiss = {
            closeBottomSheet()
        }
    )

    if (showDeleteDialog) {
        DialogComponent.DefaultButtonTwo(
            title = stringResource(R.string.card_detail_delete_dialog_title),
            description = stringResource(R.string.card_detail_delete_dialog_content),
            buttonTextStart = stringResource(R.string.card_detail_cancel),
            buttonTextEnd = stringResource(R.string.card_detail_delete),
            onClick = {
                showDeleteDialog = false
                deleteCard()
            },
            onDismiss = {
                showDeleteDialog = false
            },
            rightButtonBaseColor = Danger.M_RED,
            rightButtonClickColor = Danger.D_RED
        )
    }

    // Block Dialog
    if (showBlockDialog) {
        DialogComponent.DefaultButtonTwo(
            title = stringResource(R.string.card_detail_block_dialog_title),
            description = stringResource(
                R.string.card_detail_block_dialog_subtitle,
                nickName
            ),
            buttonTextStart = stringResource(R.string.card_detail_cancel),
            buttonTextEnd = stringResource(R.string.card_detail_block),
            onClick = {
                showBlockDialog = false
                onBlockMember()
            },
            onDismiss = { showBlockDialog = false }
        )
    }
}

@Composable
private fun PlusButton(modifier: Modifier, onWriteClick: () -> Unit, paddingValues: PaddingValues) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .padding(bottom = paddingValues.calculateBottomPadding() + 10.dp, end = 16.dp)
            .size(54.dp)
            .shadow(
                elevation = 12.dp,
                spotColor = UnKnowColor.color2,
                ambientColor = UnKnowColor.color2
            )
            .background(color = NeutralColor.GRAY_600, shape = RoundedCornerShape(27.dp))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onWriteClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(com.phew.core_design.R.drawable.ic_plus),
            contentDescription = "write comment",
            colorFilter = ColorFilter.tint(NeutralColor.WHITE)
        )
    }
}

@Composable
private fun HandleBlockUser(
    blockSuccess: Boolean,
    nickName: String,
    snackBarHostState: SnackbarHostState,
    unBlockMember: () -> Unit,
    clearBlockSuccess: () -> Unit,
) {
    val message = stringResource(R.string.card_detail_block_success, nickName)
    val cancelText = stringResource(R.string.card_detail_cancel)
    LaunchedEffect(blockSuccess) {
        if (blockSuccess) {
            val result = snackBarHostState.showSnackbar(
                message = message,
                actionLabel = cancelText
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                unBlockMember()
            }
            clearBlockSuccess()
        }
    }
}

@Composable
private fun HandleError(
    snackBarHostState: SnackbarHostState,
    errorType: CardDetailError,
    onDismissError: () -> Unit,
) {
    val errorMessage = when (errorType) {
        CardDetailError.COMMENTS_LOAD_FAILED -> stringResource(R.string.card_detail_error_comments)
        CardDetailError.CARD_LOAD_FAILED -> stringResource(R.string.card_detail_error_load_card)
        CardDetailError.NETWORK_ERROR -> stringResource(R.string.card_detail_error_load_card)
    }
    LaunchedEffect(errorType) {
        snackBarHostState.showSnackbar(
            message = errorMessage,
            duration = SnackbarDuration.Short
        )
        onDismissError()
    }
}

private const val TAG = "CardCommentDetailScreen"