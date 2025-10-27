package com.phew.presentation.detail.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phew.core.ui.model.navigation.CardDetailArgs
import com.phew.core.ui.model.navigation.CardDetailCommentArgs
import com.phew.core.ui.util.extension.nestedScrollWithStickyHeader
import com.phew.core_common.TimeUtils
import com.phew.core_common.log.SooumLog
import com.phew.core_design.AppBar.IconBothAppBar
import com.phew.core_design.BottomSheetComponent
import com.phew.core_design.BottomSheetItem
import com.phew.core_design.Danger
import com.phew.core_design.DialogComponent
import com.phew.core_design.NeutralColor
import com.phew.core_design.Primary
import com.phew.core_design.R
import com.phew.core_design.TextComponent
import com.phew.core_design.UnKnowColor
import com.phew.presentation.detail.R as DetailR
import com.phew.core_design.component.card.CardDetail
import com.phew.core_design.component.card.CardViewComment
import com.phew.domain.dto.CardComment
import com.phew.presentation.detail.component.CardDetailBottom
import com.phew.presentation.detail.component.CardDetailHeader
import com.phew.presentation.detail.model.MoreAction
import com.phew.presentation.detail.viewmodel.CardDetailError
import com.phew.presentation.detail.viewmodel.CardDetailViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun CardDetailRoute(
    modifier: Modifier = Modifier,
    args: CardDetailArgs,
    viewModel: CardDetailViewModel = hiltViewModel(),
    onNavigateToComment: (CardDetailCommentArgs) -> Unit,
    onNavigateToWrite: (Long) -> Unit,
    onNavigateToReport: (Long) -> Unit,
    onBackPressed: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(args.cardId) {
        SooumLog.d(TAG, "cardId=${args.cardId}")
        viewModel.loadCardDetail(args.cardId)
    }

    // 에러 처리
    uiState.error?.let { errorType ->
        val errorMessage = when (errorType) {
            CardDetailError.COMMENTS_LOAD_FAILED -> stringResource(DetailR.string.card_detail_error_comments)
            CardDetailError.CARD_LOAD_FAILED -> stringResource(DetailR.string.card_detail_error_load_card)
            CardDetailError.NETWORK_ERROR -> stringResource(DetailR.string.card_detail_error_load_card)
        }
        
        LaunchedEffect(errorType) {
            // TODO: 에러 메시지 표시 (Toast, SnackBar 등)
            // errorMessage를 사용하여 에러 표시
            viewModel.clearError()
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

    // 로딩 중일 때는 로딩 화면 표시
    if (uiState.isLoading) {
        // TODO: 로딩 화면 표시
        return
    }

    // cardDetail이 없으면 빈 화면 또는 에러 표시
    val cardDetail = uiState.cardDetail
    if (cardDetail == null) {
        // TODO: 데이터 없음 화면 표시
        return
    }

    CardDetailScreen(
        modifier = modifier,
        cardContent = cardDetail.cardContent,
        cardThumbnailUri = cardDetail.cardImgUrl,
        cardTags = cardDetail.tags.map { it.name },
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
        comments = uiState.comments,
        onBackPressed = onBackPressed,
        onClickLike = {
            viewModel.toggleLike(args.cardId)
        },
        onClickCommentIcon = {
            onNavigateToWrite(args.cardId)
        },
        onClickCommentView = {

        },
        onBlockMember = { toMemberId, nickname ->
            viewModel.blockMember(toMemberId, nickname)
        },
        onNavigateToReport = onNavigateToReport,
        cardId = args.cardId,
        snackBarHostState = snackBarHostState,
        storyExpirationTime = if(cardDetail.storyExpirationTime == null) "0" else cardDetail.endTime.toString(),
        isExpire = cardDetail.storyExpirationTime != null && TimeUtils.parseTimerToMillis(
            cardDetail.storyExpirationTime ?: ""
        ) <= 0L
    )
}


@Composable
private fun CardDetailScreen(
    modifier: Modifier,
    cardContent: String,
    cardThumbnailUri: String,
    cardTags: List<String>,
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
    comments: List<CardComment>,
    onBackPressed: () -> Unit,
    onClickLike: () -> Unit,
    onClickCommentIcon: () -> Unit,
    onClickCommentView: () -> Unit,
    onBlockMember: (Long, String) -> Unit,
    onNavigateToReport: (Long) -> Unit,
    cardId: Long,
    snackBarHostState: SnackbarHostState,
    storyExpirationTime : String,
    isExpire : Boolean
) {
    var remainingTimeMillis by remember {
        mutableLongStateOf(storyExpirationTime.toLong())
    }
    var isExpired by remember { mutableStateOf(false) }
    LaunchedEffect(memberId) {
        while (remainingTimeMillis > 0) {
            delay(1000L)
            remainingTimeMillis -= 1000L
        }
        isExpired = true
    }
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f,
        pageCount = { comments.size }
    )
    
    var showBottomSheet by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = modifier,
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    IconBothAppBar(
                        startImage = R.drawable.ic_left,
                        endImage = R.drawable.ic_more_stroke_circle,
                        appBarText = stringResource(DetailR.string.card_title_comment),
                        startClick = onBackPressed,
                        endClick = { showBottomSheet = true }
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
            },
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState) { data ->
                DialogComponent.SnackBar(data)
            }
        }
    ) { paddingValues ->
        val scrollState = rememberScrollState()

        Box(
            modifier = Modifier.fillMaxSize().navigationBarsPadding()
        ) {
            Column(
                modifier = modifier
                    .background(NeutralColor.WHITE)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(paddingValues)
            ) {
                CardDetail(
                    previousCommentThumbnailUri = previousCommentThumbnailUri,
                    cardContent = cardContent,
                    cardThumbnailUri = cardThumbnailUri,
                    cardTags = cardTags,
                    isDeleted = isExpire,
                    header = {
                        CardDetailHeader(
                            profileUri = profileUri,
                            nickName = nickName,
                            distance = distance,
                            createAt = createAt
                        )
                    },
                    bottom = {
                        CardDetailBottom(
                            likeCnt = likeCnt,
                            commentCnt = commentCnt,
                            searchCnt = searchCnt,
                            isLikeCard = isLikeCard,
                            onClickLike = onClickLike,
                            onClickComment = onClickCommentIcon
                        )
                    }
                )
                
                if (comments.isNotEmpty()) {
                    HorizontalPager(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(NeutralColor.GRAY_100)
                            .nestedScrollWithStickyHeader(scrollState),
                        state = pagerState,
                        flingBehavior = PagerDefaults.flingBehavior(state = pagerState),
                        pageSpacing = 10.dp,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        pageContent = { page ->
                            CardViewComment(
                                contentText = comments[page].cardContent,
                                thumbnailUri = comments[page].cardImgUrl,
                                distance = comments[page].distance ?: "",
                                createAt = TimeUtils.getRelativeTimeString(comments[page].createdAt),
                                likeCnt = comments[page].likeCount.toString(),
                                commentCnt = comments[page].commentCardCount.toString(),
                                font = comments[page].font,
                                onClick = {
                                    onClickCommentView()
                                }
                            )
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(DetailR.string.card_no_comment),
                            style = TextComponent.BODY_1_M_14
                        )
                    }

                }
            }

            // Floating Action Button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 32.dp, end = 16.dp)
                    .width(54.dp)
                    .height(54.dp)
                    .shadow(
                        elevation = 12.dp,
                        spotColor = UnKnowColor.color2,
                        ambientColor = UnKnowColor.color2
                    )
                    .background(
                        color = NeutralColor.GRAY_600,
                        shape = RoundedCornerShape(27.dp)
                    )
                    .clickable { onClickCommentIcon() },
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
        
        // BottomSheet
        if (showBottomSheet) {
            BottomSheetComponent.BottomSheet(
                data = arrayListOf(
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
                ),
                onItemClick = { id ->
                    when (id) {
                        MoreAction.BLOCK.ordinal -> {
                            showBottomSheet = false
                            showBlockDialog = true
                        }
                        MoreAction.DANGER.ordinal -> {
                            showBottomSheet = false
                            onNavigateToReport(cardId)
                        }
                    }
                },
                onDismiss = {
                    showBottomSheet = false
                }
            )
        }


        
        // Block Dialog
        if (showBlockDialog) {
            DialogComponent.DefaultButtonTwo(
                title = stringResource(DetailR.string.card_detail_block_dialog_title),
                description = stringResource(DetailR.string.card_detail_block_dialog_subtitle, nickName),
                buttonTextStart = stringResource(DetailR.string.card_detail_cancel),
                buttonTextEnd = stringResource(DetailR.string.card_detail_block),
                onClick = {
                    showBlockDialog = false
                    onBlockMember(memberId, nickName)
                },
                onDismiss = { showBlockDialog = false }
            )
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
            header = {
                CardDetailHeader(
                    profileUri = "",
                    nickName = "닉네임",
                    distance = "10km",
                    createAt = "2025-10-09T03:54:10.026919"
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
            header = {
                CardDetailHeader(
                    profileUri = "",
                    nickName = "닉네임",
                    distance = "10km",
                    createAt = "2025-10-09T03:54:10.026919"
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
            header = {
                CardDetailHeader(
                    profileUri = "",
                    nickName = "닉네임",
                    distance = "10km",
                    createAt = "2025-10-09T03:54:10.026919"
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