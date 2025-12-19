package com.phew.presentation.detail.screen

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.flowWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.phew.core.ui.model.navigation.CardDetailArgs
import com.phew.core.ui.model.navigation.CardDetailCommentArgs
import com.phew.core.ui.model.navigation.TagViewArgs
import com.phew.core_common.CardDetailTrace
import com.phew.core_common.MoveDetail
import com.phew.domain.dto.CardDetailTag
import com.phew.core_common.TimeUtils
import com.phew.core_common.isEventCard
import com.phew.core_common.log.SooumLog
import com.phew.core_design.BottomSheetComponent
import com.phew.core_design.BottomSheetItem
import com.phew.core_design.Danger
import com.phew.core_design.DialogComponent
import com.phew.core_design.NeutralColor
import com.phew.core_design.R
import com.phew.core_design.TextComponent
import com.phew.core_design.UnKnowColor
import com.phew.presentation.detail.R as DetailR
import com.phew.core_design.component.card.CardDetail
import com.phew.core_design.component.card.CardViewComment
import com.phew.domain.dto.CardComment
import com.phew.presentation.detail.component.CardDetailBottom
import com.phew.presentation.detail.component.CardDetailHeader
import com.phew.presentation.detail.component.CardDetailTopBar
import com.phew.presentation.detail.model.MoreAction
import com.phew.presentation.detail.viewmodel.CardDetailError
import com.phew.presentation.detail.viewmodel.CardDetailViewModel
import com.phew.core_design.NeutralColor.GRAY_200
import com.phew.core_design.component.toast.SooumToast
import com.phew.core_design.typography.FontType
import com.phew.presentation.detail.viewmodel.CardDetailUiEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CardDetailRoute(
    modifier: Modifier = Modifier,
    args: CardDetailArgs,
    viewModel: CardDetailViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit,
    onNavigateToComment: (CardDetailCommentArgs) -> Unit,
    onNavigateToWrite: (Long) -> Unit,
    onNavigateToReport: (Long) -> Unit,
    onNavigateToViewTags: (TagViewArgs) -> Unit,
    onBackPressed: () -> Unit,
    onPreviousCardClick: () -> Unit = { },
    profileClick: (Long) -> Unit,
    onCardChanged: () -> Unit,
    cardDetailTrace: CardDetailTrace
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val isRefreshing = uiState.isRefresh
    val lazyListState = rememberLazyListState()
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                return Offset.Zero
            }
        }
    }

    val commentsPagingItems = viewModel.commentsPagingData.collectAsLazyPagingItems()

    var showBottomSheet by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val refreshingOffset = 56.dp
    val refreshState = rememberPullToRefreshState()
    val density = LocalDensity.current

    LaunchedEffect(args.cardId) {
        SooumLog.d(TAG, "cardId=${args.cardId}")
        viewModel.loadCardDetail(args.cardId)
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
                        SooumLog.d(TAG, "CardDetailScreen resumed from WriteScreen - refreshing data")
                        viewModel.loadCardDetail(args.cardId)
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
    var isDelete by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEffect
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { effect ->
                when (effect) {
                    is CardDetailUiEffect.NavigationHome -> onNavigateToHome()
                }
            }
    }

    val context = LocalContext.current
    // 에러 처리
    LaunchedEffect(uiState.error) {
        when (uiState.error) {
            CardDetailError.COMMENTS_LOAD_FAILED, CardDetailError.NETWORK_ERROR, CardDetailError.CARD_LOAD_FAILED , CardDetailError.FAIL -> {
                val message = when(uiState.error){
                    CardDetailError.COMMENTS_LOAD_FAILED -> context.getString(DetailR.string.card_detail_error_comments)
                    CardDetailError.CARD_LOAD_FAILED -> context.getString(DetailR.string.card_detail_error_load_card)
                    CardDetailError.NETWORK_ERROR -> context.getString(DetailR.string.card_detail_error_load_card)
                    CardDetailError.FAIL -> context.getString(R.string.error_app)
                    else -> ""
                }
                SooumToast.makeToast(context , message , SooumToast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
            CardDetailError.CARD_DELETE -> {
                viewModel.setDeleteDialog()
            }
            else -> Unit
        }
    }

    if (uiState.blockSuccess) {
        val nickname = uiState.blockedNickname ?: ""
        val message = stringResource(DetailR.string.card_detail_block_success, nickname)
        val cancelText = stringResource(DetailR.string.card_detail_cancel)
        
        LaunchedEffect(uiState.blockSuccess) {
            coroutineScope.launch {
                val result = snackBarHostState.showSnackbar(
                    message = message,
                    actionLabel = cancelText
                )
                if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                    viewModel.unblockMember()
                }
            }
            viewModel.clearBlockSuccess()
        }
    }
    if(uiState.deleteSuccess) {
        isDelete = true
    }

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.ic_refresh)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = isRefreshing
    )

    // 로딩 중일 때는 로딩 화면 표시
    val isInitialLoading = uiState.isLoading && uiState.cardDetail == null
    if (isInitialLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(44.dp)
            )
        }
        return
    }

    // cardDetail이 없으면 빈 화면 또는 에러 표시
    val cardDetail = uiState.cardDetail
    TrackCardInteraction(cardDetail = cardDetail, onCardChanged = onCardChanged)
    if (cardDetail == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(DetailR.string.card_detail_not_found),
                    style = TextComponent.BODY_1_M_14,
                    color = NeutralColor.GRAY_500,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onBackPressed,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeutralColor.GRAY_600
                    )
                ) {
                    Text(
                        text = stringResource(DetailR.string.card_detail_go_back),
                        style = TextComponent.BODY_1_M_14,
                        color = NeutralColor.WHITE
                    )
                }
            }
        }
        return
    }
    val storyExpirationTime = if(cardDetail.storyExpirationTime == null) "0" else cardDetail.endTime.toString()
    var remainingTimeMillis by remember { mutableLongStateOf(storyExpirationTime.toLong()) }

    LaunchedEffect(cardDetail.memberId) {
        while (remainingTimeMillis > 0) {
            delay(1000L)
            remainingTimeMillis -= 1000L
        }
    }
    LaunchedEffect(Unit) {
        viewModel.logWhereComeFrom(view = cardDetailTrace)
    }
    CardDetailScreen(
        modifier = modifier,
        cardContent = cardDetail.cardContent,
        cardThumbnailUri = cardDetail.cardImgUrl,
        cardTags = cardDetail.tags,
        cardFont = cardDetail.font,
        previousCommentThumbnailUri = cardDetail.previousCardImgUrl ?: "",
        profileUri = cardDetail.profileImgUrl ?: "",
        nickName = cardDetail.nickname,
        memberId = cardDetail.memberId,
        distance = cardDetail.distance ?: "",
        createAt = cardDetail.createdAt.takeIf { it.isNotBlank() } ?: "",
        likeCnt = cardDetail.likeCount,
        commentCnt = cardDetail.commentCardCount,
        searchCnt = cardDetail.visitedCnt,
        isLikeCard = cardDetail.isLike,
        commentsPagingItems = commentsPagingItems,
        isRefreshing = isRefreshing,
        composition = composition,
        progress = progress,
        onBackPressed = onBackPressed,
        onClickLike = {
            viewModel.toggleLike(args.cardId)
        },
        onClickCommentIcon = { event ->
            viewModel.logMoveToCommentCard(
                event = event,
                isEventCard = cardDetail.cardImgName.isEventCard()
            )
            onNavigateToWrite(args.cardId)
        },
        onClickCommentView = { commentCardId ->
            onNavigateToComment(
                CardDetailCommentArgs(
                    cardId = commentCardId,
                    parentId = args.cardId,
                )
            )
        },
        onBlockMember = { toMemberId, nickname ->
            viewModel.blockMember(toMemberId, nickname)
        },
        onNavigateToReport = onNavigateToReport,
        onRefresh = {
            viewModel.loadCardDetail(args.cardId)
        },
        onNavigateToViewTags = { tag ->
            viewModel.logMoveToTagView()
            onNavigateToViewTags(tag)
        },
        lazyListState = lazyListState,
        nestedScrollConnection = nestedScrollConnection,
        cardId = args.cardId,
        snackBarHostState = snackBarHostState,
        remainingTimeMillis = remainingTimeMillis,
        isExpire = (cardDetail.storyExpirationTime != null && (cardDetail.endTime
            ?: 0L) <= 0L) || isDelete,
        isOwnCard = cardDetail.isOwnCard,
        deleteCard = { cardId ->
            viewModel.requestDeleteCard(cardId)
        },
        showBottomSheet = showBottomSheet,
        onShowBottomSheetChange = { showBottomSheet = it },
        showBlockDialog = showBlockDialog,
        onShowBlockDialogChange = { showBlockDialog = it },
        showDeleteDialog = showDeleteDialog,
        onShowDeleteDialogChange = { showDeleteDialog = it },
        refreshingOffset = refreshingOffset,
        refreshState = refreshState,
        density = density,
        onPreviousCardClick = onPreviousCardClick,
        profileClick = { id ->
            if (!cardDetail.isOwnCard) {
                profileClick(id)
            }
        },
        deleteErrorDialog = uiState.deleteErrorDialog,
        onClickDeleteErrorDialog = {
            viewModel.clearError()
            onBackPressed()
        }
    )
}


@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CardDetailScreen(
    modifier: Modifier,
    cardContent: String,
    cardThumbnailUri: String,
    cardTags: List<CardDetailTag>,
    cardFont: String,
    previousCommentThumbnailUri: String,
    profileUri: String,
    nickName: String,
    memberId: Long,
    distance: String,
    createAt: String,
    likeCnt: Int,
    commentCnt: Int,
    searchCnt: Int,
    isLikeCard: Boolean,
    commentsPagingItems: LazyPagingItems<CardComment>,
    isRefreshing: Boolean,
    progress: Float,
    composition: LottieComposition?,
    onBackPressed: () -> Unit,
    onClickLike: () -> Unit,
    onClickCommentIcon: (MoveDetail) -> Unit,
    onClickCommentView: (Long) -> Unit,
    onBlockMember: (Long, String) -> Unit,
    deleteCard: (Long) -> Unit,
    onNavigateToReport: (Long) -> Unit,
    onNavigateToViewTags: (TagViewArgs) -> Unit,
    onRefresh: () -> Unit,
    lazyListState: LazyListState,
    nestedScrollConnection: NestedScrollConnection,
    cardId: Long,
    snackBarHostState: SnackbarHostState,
    remainingTimeMillis: Long,
    isExpire: Boolean,
    isOwnCard: Boolean,
    showBottomSheet: Boolean,
    onShowBottomSheetChange: (Boolean) -> Unit,
    showBlockDialog: Boolean,
    onShowBlockDialogChange: (Boolean) -> Unit,
    showDeleteDialog: Boolean,
    onShowDeleteDialogChange: (Boolean) -> Unit,
    refreshingOffset: Dp,
    refreshState: PullToRefreshState,
    density: androidx.compose.ui.unit.Density,
    onPreviousCardClick: () -> Unit = { },
    profileClick : (Long) -> Unit,
    deleteErrorDialog : Boolean,
    onClickDeleteErrorDialog : () -> Unit
) {

    Scaffold(
        modifier = modifier,
        topBar = {
            CardDetailTopBar(
                remainingTimeMillis = remainingTimeMillis,
                onBackPressed = onBackPressed,
                onMoreClick = { onShowBottomSheetChange(true) }
            )
        },
        snackbarHost = {
            DialogComponent.CustomAnimationSnackBarHost(hostState = snackBarHostState)
        },
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .navigationBarsPadding()
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(27.dp),
                            spotColor = UnKnowColor.color2, // #64686C with 20% opacity
                            ambientColor = UnKnowColor.color2
                        )
                        .background(
                            color = NeutralColor.GRAY_600,
                            shape = RoundedCornerShape(27.dp)
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onClickCommentIcon(MoveDetail.FLOAT) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_plus),
                        contentDescription = "Add",
                        tint = NeutralColor.WHITE
                    )
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            state = refreshState,
            modifier = Modifier
                .fillMaxSize(),
            indicator = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = paddingValues.calculateTopPadding()),
                    contentAlignment = Alignment.Center
                ) {
                    val distanceFraction = refreshState.distanceFraction
                    val lottieProgress = if (isRefreshing) progress else distanceFraction
                    LottieAnimation(
                        composition = composition,
                        progress = { lottieProgress },
                        modifier = Modifier
                            .size(44.dp)
                            .graphicsLayer {
                                alpha = if (isRefreshing || distanceFraction > 0f) 1f else 0f
                            }
                    )
                }
            }
        ) {
            LazyColumn(
                modifier = Modifier
                    .background(NeutralColor.WHITE)
                    .fillMaxSize()
                    .padding(paddingValues)
                    .nestedScroll(nestedScrollConnection)
                    .graphicsLayer {
                        val distanceFraction = refreshState.distanceFraction
                        translationY = if (isRefreshing || distanceFraction > 0f) {
                            distanceFraction * with(density) { refreshingOffset.toPx() }
                        } else {
                            0f
                        }
                    },
                state = lazyListState,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    CardDetail(
                        previousCommentThumbnailUri = previousCommentThumbnailUri,
                        cardContent = cardContent,
                        cardThumbnailUri = cardThumbnailUri,
                        cardTags = cardTags.map { it.name },
                        fontType = FontType.fromServerName(cardFont),
                        isDeleted = isExpire,
                        onTagClick = { tagName ->
                            val tag = cardTags.find { it.name == tagName }
                            if (tag != null) {
                                onNavigateToViewTags(TagViewArgs(tagName = tag.name, tagId = tag.tagId))
                            }
                        },
                        header = {
                            CardDetailHeader(
                                profileUri = profileUri,
                                nickName = nickName,
                                distance = distance,
                                createAt = createAt,
                                memberId = memberId,
                                onClick = profileClick
                            )
                        },
                        bottom = {
                            CardDetailBottom(
                                likeCnt = likeCnt,
                                commentCnt = commentCnt,
                                searchCnt = searchCnt,
                                isLikeCard = isLikeCard,
                                onClickLike = onClickLike,
                                onClickComment = {
                                    onClickCommentIcon(MoveDetail.IMAGE)
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        onPreviousCardClick = onPreviousCardClick
                    )
                }
                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(GRAY_200)
                    )
                }
                item {
                    CommentPreviewSection(
                        commentsPagingItems = commentsPagingItems,
                        onClickCommentView = onClickCommentView
                    )
                }
                if (deleteErrorDialog) {
                    item {
                        DialogComponent.NoDescriptionButtonOne(
                            title = stringResource(com.phew.presentation.detail.R.string.card_detail_dialog_delete_title),
                            buttonText = stringResource(R.string.common_okay),
                            onClick = onClickDeleteErrorDialog,
                            onDismiss = onClickDeleteErrorDialog
                        )
                    }
                }
            }
        }
    }

    // BottomSheet
    if (showBottomSheet) {
        BottomSheetComponent.BottomSheet(
            data = if (isOwnCard) {
                arrayListOf(
                    BottomSheetItem(
                        id = MoreAction.DELETE.ordinal,
                        title = stringResource(id = DetailR.string.card_detail_delete),
                        image = R.drawable.ic_trash_stoke,
                        imageColor = Danger.M_RED,
                        textColor = Danger.M_RED,
                    )
                )
            } else {
                arrayListOf(
                    BottomSheetItem(
                        id = MoreAction.BLOCK.ordinal,
                        title = stringResource(id = DetailR.string.card_detail_block),
                        image = R.drawable.ic_eye,
                        textColor = NeutralColor.GRAY_500,
                        imageColor = NeutralColor.BLACK
                    ),
                    BottomSheetItem(
                        id = MoreAction.DANGER.ordinal,
                        title = stringResource(id = DetailR.string.card_detail_report),
                        image = R.drawable.ic_flag_stoke,
                        imageColor = Danger.M_RED,
                        textColor = Danger.M_RED,
                    )
                )
            },
            onItemClick = { id ->
                when (id) {
                    MoreAction.BLOCK.ordinal -> {
                        onShowBottomSheetChange(false)
                        onShowBlockDialogChange(true)
                    }

                    MoreAction.DANGER.ordinal -> {
                        onShowBottomSheetChange(false)
                        onNavigateToReport(cardId)
                    }

                    MoreAction.DELETE.ordinal -> {
                        onShowBottomSheetChange(false)
                        onShowDeleteDialogChange(true)
                    }
                }
            },
            onDismiss = {
                onShowBottomSheetChange(false)
            }
        )
    }

    if (showDeleteDialog) {
        DialogComponent.DefaultButtonTwo(
            title = stringResource(DetailR.string.card_detail_delete_dialog_title),
            description = stringResource(DetailR.string.card_detail_delete_dialog_content),
            buttonTextStart = stringResource(DetailR.string.card_detail_cancel),
            buttonTextEnd = stringResource(DetailR.string.card_detail_delete),
            onClick = {
                onShowDeleteDialogChange(false)
                deleteCard(cardId)
            },
            onDismiss = {
                onShowDeleteDialogChange(false)
            },
            rightButtonBaseColor = Danger.M_RED,
            rightButtonClickColor = Danger.D_RED,
            startButtonTextColor = NeutralColor.GRAY_600,
        )
    }

    // Block Dialog
    if (showBlockDialog) {
        DialogComponent.DefaultButtonTwo(
            title = stringResource(DetailR.string.card_detail_block_dialog_title),
            description = stringResource(
                DetailR.string.card_detail_block_dialog_subtitle,
                nickName
            ),
            buttonTextStart = stringResource(DetailR.string.card_detail_cancel),
            buttonTextEnd = stringResource(DetailR.string.card_detail_block),
            onClick = {
                onShowBlockDialogChange(false)
                onBlockMember(memberId, nickName)
            },
            onDismiss = { onShowBlockDialogChange(false) },
            startButtonTextColor = NeutralColor.GRAY_600
        )
    }
}

@Composable
private fun CommentPreviewSection(
    commentsPagingItems: LazyPagingItems<CardComment>,
    onClickCommentView: (Long) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val landscapeMinHeightRatio = 0.45f
    val portraitMinHeightRatio = 0.3f
    val landscapeMaxHeight = 280.dp
    val portraitMaxHeight = 340.dp
    val commentCardHeight = 180.dp

    val minHeight = if (isLandscape) {
        (configuration.screenWidthDp * landscapeMinHeightRatio).dp
    } else {
        (configuration.screenHeightDp * portraitMinHeightRatio).dp
    }
    val maxHeight = if (isLandscape) landscapeMaxHeight else portraitMaxHeight

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight, max = maxHeight)
            .background(NeutralColor.GRAY_100)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            commentsPagingItems.loadState.refresh is LoadState.Loading -> {
                CircularProgressIndicator()
            }
            commentsPagingItems.itemCount == 0 -> {
                Text(
                    text = stringResource(DetailR.string.card_no_comment),
                    style = TextComponent.BODY_1_M_14,
                    color = NeutralColor.GRAY_400
                )
            }
            else -> {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(
                        count = commentsPagingItems.itemCount,
                        key = commentsPagingItems.itemKey { it.cardId },
                        contentType = commentsPagingItems.itemContentType { "CardComment" }
                    ) { index ->
                        val comment = commentsPagingItems[index]
                        if (comment != null) {
                            CardViewComment(
                                modifier = Modifier
                                    .height(commentCardHeight)
                                    .aspectRatio(1f),
                                contentText = comment.cardContent,
                                thumbnailUri = comment.cardImgUrl,
                                distance = comment.distance ?: "",
                                createAt = TimeUtils.getRelativeTimeString(comment.createdAt),
                                likeCnt = comment.likeCount.toString(),
                                commentCnt = comment.commentCardCount.toString(),
                                font = comment.font,
                                onClick = { onClickCommentView(comment.cardId) }
                            )
                        }
                    }
                    if (commentsPagingItems.loadState.append is LoadState.Loading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .height(commentCardHeight)
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun CardDetailPreview() {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
    ) {

        CardDetail(
            previousCommentThumbnailUri = null,
            cardContent = "이건 ReplyCard 예시",
            cardThumbnailUri = "",
            cardTags = listOf("내머리와충돌", "중상", "위동"),
            onTagClick = {},
            header = {
                CardDetailHeader(
                    profileUri = "",
                    nickName = "닉네임",
                    distance = "10km",
                    createAt = "2025-10-09T03:54:10.026919",
                    memberId = 12321453,
                    onClick = {}
                )
            },
            bottom = {
                CardDetailBottom(
                    likeCnt = 10,
                    commentCnt = 10,
                    searchCnt = 10,
                    isLikeCard = true,
                    onClickLike = {},
                    onClickComment = {}
                )
            }
        )

        Spacer(modifier = Modifier.size(4.dp))

        CardDetail(
            previousCommentThumbnailUri = "null",
            isDeleted = true,
            cardContent = "",
            cardThumbnailUri = "",
            cardTags = listOf("내머리와충돌", "중상", "위동"),
            onTagClick = {},
            header = {
                CardDetailHeader(
                    profileUri = "",
                    nickName = "닉네임",
                    distance = "10km",
                    createAt = "2025-10-09T03:54:10.026919",
                    memberId = 12321453,
                    onClick = {}
                )
            },
            bottom = {

            }
        )
        Spacer(modifier = Modifier.size(4.dp))

        CardDetail(
            previousCommentThumbnailUri = "null",
            cardContent = "이건 ReplyCard 예시",
            cardThumbnailUri = "",
            cardTags = listOf("내머리와충돌", "중상", "위동"),
            onTagClick = {},
            header = {
                CardDetailHeader(
                    profileUri = "",
                    nickName = "닉네임",
                    distance = "10km",
                    createAt = "2025-10-09T03:54:10.026919",
                    memberId = 12321453,
                    onClick = {}
                )
            },
            bottom = {
                CardDetailBottom(
                    likeCnt = 10,
                    commentCnt = 10,
                    searchCnt = 10,
                    isLikeCard = true,
                    onClickLike = {},
                    onClickComment = {}
                )
            }
        )

        Spacer(modifier = Modifier.size(24.dp))
    }
}

private const val TAG = "CardDetailScreen"
