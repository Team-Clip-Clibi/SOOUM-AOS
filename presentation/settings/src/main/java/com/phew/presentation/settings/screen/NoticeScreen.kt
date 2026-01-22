package com.phew.presentation.settings.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.phew.core.ui.model.navigation.WebViewUrlArgs
import com.phew.core_design.AppBar.IconLeftAppBar
import com.phew.core_design.NeutralColor
import com.phew.core_design.TextComponent
import com.phew.domain.dto.Notice
import com.phew.presentation.settings.R
import com.phew.presentation.settings.viewmodel.NoticeNavigationEvent
import com.phew.presentation.settings.viewmodel.NoticeState
import com.phew.presentation.settings.viewmodel.NoticeViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.phew.core_common.TimeUtils
import com.phew.core_design.R as DesignR

@Composable
internal fun NoticeRoute(
    modifier: Modifier = Modifier,
    viewModel: NoticeViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
    onNoticeItemClick: (WebViewUrlArgs) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val noticePagingItems = viewModel.notice.collectAsLazyPagingItems()

    LaunchedEffect(viewModel.navigationEvent) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is NoticeNavigationEvent.NavigateToNoticeDetail -> {
                    onNoticeItemClick(WebViewUrlArgs(
                        url = event.notice.url
                    ))
                }
            }
        }
    }

    NoticeScreen(
        modifier = modifier,
        uiState = uiState,
        listState = listState,
        noticePagingItems = noticePagingItems,
        onBackPressed = onBackPressed,
        onNoticeItemClick = viewModel::onNoticeItemClick,
        onRefresh = viewModel::refresh
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoticeScreen(
    modifier: Modifier = Modifier,
    uiState: NoticeState,
    listState: LazyListState,
    noticePagingItems: LazyPagingItems<Notice>,
    onBackPressed: () -> Unit,
    onNoticeItemClick: (Notice) -> Unit,
    onRefresh: () -> Unit
) {
    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NeutralColor.WHITE)
            ) {
                IconLeftAppBar(
                    image = DesignR.drawable.ic_left,
                    onClick = onBackPressed,
                    appBarText = stringResource(R.string.setting_notice)
                )
            }
        }
    ) { paddingValues ->
        val refreshState = rememberPullToRefreshState()
        val isRefreshing = noticePagingItems.loadState.refresh is LoadState.Loading

        val composition by rememberLottieComposition(
            LottieCompositionSpec.RawRes(DesignR.raw.ic_refresh)
        )
        val progress by animateLottieCompositionAsState(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            isPlaying = isRefreshing
        )
        
        val density = LocalDensity.current
        val refreshingOffset = 56.dp

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                noticePagingItems.refresh()
                onRefresh()
            },
            state = refreshState,
            modifier = modifier
                .fillMaxSize()
                .background(NeutralColor.WHITE)
                .padding(paddingValues),
            indicator = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
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
            if (uiState.isLoading && noticePagingItems.itemCount == 0) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier.size(44.dp)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val distanceFraction = refreshState.distanceFraction
                            translationY = if (isRefreshing || distanceFraction > 0f) {
                                distanceFraction * with(density) { refreshingOffset.toPx() }
                            } else {
                                0f
                            }
                        }
                ) {
                    items(
                        count = noticePagingItems.itemCount,
                        key = { index -> 
                            noticePagingItems[index]?.let { "notice_${it.id}_$index" } ?: "notice_$index"
                        }
                    ) { index ->
                        val notice = noticePagingItems[index]
                        notice?.let {
                            NoticeItem(
                                notice = it,
                                onClick = { onNoticeItemClick(it) }
                            )
                            if (index < noticePagingItems.itemCount - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    thickness = 1.dp,
                                    color = NeutralColor.GRAY_100
                                )
                            }
                        }
                    }

                    // 페이징 로딩 상태 처리
                    when (noticePagingItems.loadState.append) {
                        is LoadState.Loading -> {
                            item(key = "notice_loading_append") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    LottieAnimation(
                                        composition = composition,
                                        progress = { progress },
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                            }
                        }

                        is LoadState.Error -> {
                            item(key = "notice_error_append") {
                                Text(
                                    text = "로딩 중 오류가 발생했습니다",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    style = TextComponent.BODY_2_R_14,
                                    color = NeutralColor.GRAY_500
                                )
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun NoticeItem(
    notice: Notice,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = notice.content,
                style = TextComponent.SUBTITLE_3_SB_14,
                color = NeutralColor.BLACK
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = TimeUtils.formatToDotDate(notice.createdAt),
                style = TextComponent.CAPTION_1_SB_12,
                color = NeutralColor.GRAY_400
            )
        }
    }
}

@Preview
@Composable
private fun NoticeScreenPreview() {
    // Preview는 실제 PagingItems를 사용할 수 없으므로 간단한 UI만 표시
}