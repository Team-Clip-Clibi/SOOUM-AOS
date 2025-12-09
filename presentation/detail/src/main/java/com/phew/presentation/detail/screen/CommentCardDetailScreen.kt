package com.phew.presentation.detail.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
import com.phew.core_design.CustomFont
import com.phew.presentation.detail.component.CardDetailTopBar
import kotlinx.coroutines.delay
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarResult
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import kotlinx.coroutines.launch
import com.airbnb.lottie.compose.LottieConstants
import com.phew.core_design.LoadingAnimation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CommentCardDetailScreen(
    args: CardDetailCommentArgs,
    viewModel: CardDetailViewModel = hiltViewModel(),
    onNavigateToComment: (CardDetailCommentArgs) -> Unit,
    onNavigateToWrite: (Long) -> Unit,
    onNavigateToReport: (Long) -> Unit,
    onNavigateToViewTags: (com.phew.core.ui.model.navigation.TagViewArgs) -> Unit,
    onBackPressed: (Long) -> Unit,
    onFeedPressed: () -> Unit,
    onTagPressed: () -> Unit,
    onProfileClick: (Long) -> Unit,
    onCardChanged: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val comments: LazyPagingItems<CardComment> = viewModel.commentsPagingData
        .collectAsLazyPagingItems()
    val cardDetail = uiState.cardDetail
    TrackCommentCardInteraction(cardDetail = cardDetail, onCardChanged = onCardChanged)
    LaunchedEffect(args.cardId) {
        SooumLog.d(TAG, "CardId : ${args.cardId}")
        viewModel.loadCardDetail(args.cardId)
        viewModel.requestComment(args.cardId)
    }

    // WriteScreen에서 복귀 시에만 새로고침 처리
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasResumed by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    // 화면을 벗어날 때 flag 설정
                    hasResumed = false
                }

                Lifecycle.Event.ON_RESUME -> {
                    // 두 번째 Resume부터만 새로고침 (WriteScreen에서 복귀 시)
                    if (hasResumed) {
                        SooumLog.d(
                            TAG,
                            "CommentCardDetailScreen resumed from WriteScreen - refreshing data"
                        )
                        viewModel.loadCardDetail(args.cardId)
                        viewModel.requestComment(args.cardId)
                    } else {
                        hasResumed = true
                    }
                }

                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    val isRefreshing by remember(uiState.isLoading) {
        derivedStateOf { uiState.isLoading }
    }
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(com.phew.core_design.R.raw.ic_refresh)
    )
    val refreshProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        restartOnPlay = isRefreshing
    )
    if (uiState.isLoading && cardDetail == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars),
            contentAlignment = Alignment.Center
        ) {
            LoadingAnimation.LoadingView()
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
    val onBackPressedLambda = remember(cardDetail.previousCardId) { {
        val parentId = cardDetail.previousCardId?.toLongOrNull() ?: 0L
        SooumLog.d(TAG, "parentId : $parentId")
        onBackPressed(parentId)
    } }
    val showBottomSheetLambda = remember { { showBottomSheet = true } }
    val onExpireLambda = remember { { isTimerExpired = true } }
    val onClickLikeLambda = remember(args.cardId) { { viewModel.toggleLike(args.cardId) } }
    val onClickPreviousCard = remember(cardDetail.previousCardId) { {
        val parentId = cardDetail.previousCardId?.toLongOrNull() ?: 0L
        onBackPressed(parentId)
    } }
    val onCommentClickLambda: (Long) -> Unit = remember(args.cardId) {
        { childId ->
            onNavigateToComment(
                CardDetailCommentArgs(
                    cardId = childId,
                    parentId = cardDetail.cardId
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
    var isManualRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val onRefresh = remember(args.cardId) {
        {
            isManualRefreshing = true
            viewModel.loadCardDetail(args.cardId)
            viewModel.requestComment(args.cardId)
            // 새로고침 완료를 기다린 후 상태 초기화
            coroutineScope.launch {
                delay(500) // 최소 500ms 표시
                isManualRefreshing = false
            }
            Unit // 명시적으로 Unit 반환
        }
    }
    LaunchedEffect(uiState.error) {
      if(uiState.error == CardDetailError.CARD_DELETE){
          viewModel.setDeleteDialog()
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
    if (errorType != null && errorType != CardDetailError.CARD_DELETE) {
        HandleError(
            errorType = errorType,
            snackBarHostState = snackBarHostState,
            onDismissError = clearErrorLambda
        )
    }
    val refreshState = rememberPullToRefreshState()
    val density = LocalDensity.current
    val showDetailTopBar = cardDetail.previousCardId.isNullOrEmpty()
    Scaffold(
        topBar = {
            if (showDetailTopBar) {
                CardDetailTopBar(
                    remainingTimeMillis = cardDetail.endTime,
                    onBackPressed = onBackPressedLambda,
                    onMoreClick = showBottomSheetLambda
                )
            } else {
                TopLayout(
                    storyRemainingMillis = cardDetail.endTime,
                    onFeedPressed = when (args.backTo) {
                        "tag" -> onTagPressed
                        else -> onFeedPressed
                    },
                    onBackPressed = onBackPressedLambda,
                    showBottomSheet = showBottomSheetLambda,
                    onExpire = onExpireLambda,
                    memberId = cardDetail.memberId
                )
            }
        },
        snackbarHost = {
            DialogComponent.CustomAnimationSnackBarHost(hostState = snackBarHostState)
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isManualRefreshing,
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
                        if (isManualRefreshing) refreshProgress else refreshState.distanceFraction
                    if (isManualRefreshing || refreshState.distanceFraction > 0f) {
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
                    onNavigateToViewTags = onNavigateToViewTags,
                    playProgression = {
                        LottieAnimation(
                            composition = composition,
                            progress = { refreshProgress },
                            modifier = Modifier.size(44.dp)
                        )
                    },
                    onProfileClick = onProfileClick,
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
                if (uiState.deleteErrorDialog) {
                    DeleteDialog(onClick = {
                        viewModel.clearError()
                        onFeedPressed()
                    })
                }
            }
        }
    }
}

@Composable
private fun TopLayout(
    storyRemainingMillis: Long,
    memberId: Long,
    onFeedPressed: () -> Unit,
    onBackPressed: () -> Unit,
    showBottomSheet: () -> Unit,
    onExpire: () -> Unit,
) {
    var remainingTimeMillis by remember { mutableLongStateOf(storyRemainingMillis) }
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

@SuppressLint("UnusedBoxWithConstraintsScope")
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
    playProgression: @Composable () -> Unit,
    onProfileClick : (Long) -> Unit,
    onNavigateToViewTags: (com.phew.core.ui.model.navigation.TagViewArgs) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(color = NeutralColor.WHITE)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                CardDetailComponent(
                    modifier = Modifier.fillMaxWidth(),
                    previousCommentThumbnailUri = cardDetail.previousCardImgUrl,
                    cardContent = cardDetail.cardContent,
                    cardThumbnailUri = cardDetail.cardImgUrl,
                    cardTags = cardDetail.tags.map { data -> data.name },
                    isDeleted = isExpire,
                    backgroundImageUrl = cardDetail.cardImgUrl.toUri(),
                    fontFamily = CustomFont.findFontValueByServerName(cardDetail.font).data.previewTypeface,
                    onTagClick = { tagName ->
                        val tag = cardDetail.tags.find { it.name == tagName }
                        if (tag != null) {
                            onNavigateToViewTags(com.phew.core.ui.model.navigation.TagViewArgs(tagName = tag.name, tagId = tag.tagId))
                        }
                    },
                    header = {
                        CardDetailHeader(
                            profileUri = cardDetail.profileImgUrl ?: "",
                            nickName = cardDetail.nickname,
                            distance = cardDetail.distance ?: "",
                            createAt = cardDetail.createdAt,
                            memberId = cardDetail.memberId,
                            onClick = onProfileClick
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
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(color = NeutralColor.GRAY_100),
                contentAlignment = Alignment.Center,
            ) {
                val loadState = comments.loadState
                when (loadState.refresh) {
                    is LoadState.Loading -> {
                        playProgression()
                    }

                    is LoadState.NotLoading -> {
                        when (comments.itemCount) {
                            0 -> {
                                Text(
                                    text = stringResource(R.string.card_no_comment),
                                    style = TextComponent.BODY_1_M_14,
                                    color = NeutralColor.GRAY_400,
                                    textAlign = TextAlign.Center
                                )
                            }

                            else -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color = NeutralColor.GRAY_100)
                                        .padding(top = 10.dp, bottom = 10.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    LazyRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
                                    ) {
                                        items(
                                            count = comments.itemCount,
                                            key = comments.itemKey { it.cardId }
                                        ) { index ->
                                            val comment = comments[index] ?: return@items
                                            Box(
                                                modifier = Modifier
                                                    .fillParentMaxHeight()
                                                    .aspectRatio(1f)
                                            ) {
                                                CardViewComment(
                                                    contentText = comment.cardContent,
                                                    thumbnailUri = comment.cardImgUrl,
                                                    distance = comment.distance ?: "",
                                                    createAt = TimeUtils.getRelativeTimeString(
                                                        comment.createdAt
                                                    ),
                                                    likeCnt = comment.likeCount.toString(),
                                                    commentCnt = comment.commentCardCount.toString(),
                                                    font = comment.font,
                                                    onClick = {
                                                        onCommentClick(comment.cardId)
                                                    }
                                                )
                                            }
                                        }
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
                    }

                    is LoadState.Error -> {
                        Text(
                            text = stringResource(R.string.card_error_comment),
                            style = TextComponent.BODY_1_M_14,
                            color = NeutralColor.GRAY_400,
                            textAlign = TextAlign.Center
                        )
                    }
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
            rightButtonClickColor = Danger.D_RED,
            startButtonTextColor = NeutralColor.GRAY_600
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
            onDismiss = { showBlockDialog = false },
            startButtonTextColor = NeutralColor.GRAY_600
        )
    }
}

@Composable
private fun TrackCommentCardInteraction(
    cardDetail: CardDetail?,
    onCardChanged: () -> Unit
) {
    var lastSnapshot by remember { mutableStateOf<CommentCardSnapshot?>(null) }
    LaunchedEffect(cardDetail?.cardId, cardDetail?.likeCount, cardDetail?.commentCardCount) {
        if (cardDetail == null) return@LaunchedEffect
        val snapshot = CommentCardSnapshot(
            cardId = cardDetail.cardId,
            likeCount = cardDetail.likeCount,
            commentCount = cardDetail.commentCardCount
        )
        val previous = lastSnapshot
        if (previous == null || previous.cardId != snapshot.cardId) {
            lastSnapshot = snapshot
        } else if (previous != snapshot) {
            lastSnapshot = snapshot
            onCardChanged()
        }
    }
}

private data class CommentCardSnapshot(
    val cardId: Long,
    val likeCount: Int,
    val commentCount: Int
)

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
            if (result == SnackbarResult.ActionPerformed) {
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
        else -> ""
    }
    LaunchedEffect(errorType) {
        snackBarHostState.showSnackbar(
            message = errorMessage,
            duration = SnackbarDuration.Short
        )
        onDismissError()
    }
}

@Composable
private fun DeleteDialog(onClick: () -> Unit){
    DialogComponent.NoDescriptionButtonOne(
        title = stringResource(com.phew.presentation.detail.R.string.card_detail_dialog_delete_title),
        buttonText = stringResource(com.phew.core_design.R.string.common_okay),
        onClick = onClick,
        onDismiss = onClick
    )
}

private const val TAG = "CardCommentDetailScreen"
