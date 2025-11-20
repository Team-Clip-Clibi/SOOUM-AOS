package com.phew.presentation.tag.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.phew.core_design.AppBar.LeftAppBar
import com.phew.core_design.LoadingAnimation
import com.phew.core_design.NeutralColor
import com.phew.core_design.component.tag.TagRankView
import com.phew.domain.model.TagInfo
import com.phew.presentation.tag.R
import com.phew.presentation.tag.viewmodel.TagViewModel
import com.phew.presentation.tag.viewmodel.UiState

@Composable
internal fun TagRoute(
    modifier: Modifier = Modifier,
    viewModel: TagViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
) {
    TagScreen(
        modifier = modifier,
        onBackPressed = onBackPressed,
        viewModel = viewModel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagScreen(
    modifier: Modifier,
    onBackPressed: () -> Unit,
    viewModel: TagViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val refreshState = rememberPullToRefreshState()
    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        topBar = {
            LeftAppBar(
                appBarText = stringResource(R.string.tag_top_title),
                onClick = {

                }
            )
        }
    ) { innerPadding ->
        RefreshBox(
            isRefresh = uiState.isRefreshing,
            onRefresh = remember(viewModel::refresh) { { viewModel.refresh() } },
            state = refreshState,
            paddingValues = innerPadding
        ) {
            TagView(
                paddingValues = innerPadding,
                rankTag = uiState.tagRank,
                refreshState = refreshState
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RefreshBox(
    isRefresh: Boolean,
    onRefresh: () -> Unit,
    state: PullToRefreshState,
    paddingValues: PaddingValues,
    content: @Composable (() -> Unit),
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(com.phew.core_design.R.raw.ic_refresh)
    )
    val refreshProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        restartOnPlay = isRefresh
    )
    PullToRefreshBox(
        isRefreshing = isRefresh,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxWidth(),
        state = state,
        indicator = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(top = paddingValues.calculateTopPadding()),
                contentAlignment = Alignment.Center
            ) {
                val progress = if (isRefresh) refreshProgress else state.distanceFraction
                if (isRefresh || state.distanceFraction > 0f) {
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier.size(80.dp)
                    )
                }
            }
        }
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagView(
    paddingValues: PaddingValues,
    rankTag: UiState<List<TagInfo>>,
    refreshState: PullToRefreshState,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(
            start = 16.dp, bottom = paddingValues.calculateBottomPadding(), end = 16.dp
        ),
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralColor.WHITE)
            .graphicsLayer {
                val pullDistance = refreshState.distanceFraction * 100.dp.toPx()
                translationY = pullDistance + 72.dp.toPx()
            }
    ) {
        item(span = { GridItemSpan(currentLineSpan = maxLineSpan) }) {
            //TODO 검색어
        }
        item(span = { GridItemSpan(currentLineSpan = maxLineSpan) }) {
            //TODO 닉네임 + 관심 테그
        }
        when (rankTag) {
            is UiState.Fail -> {
                item(span = { GridItemSpan(currentLineSpan = maxLineSpan) }) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(com.phew.core_design.R.drawable.ic_deleted_card),
                            modifier = Modifier.size(24.dp),
                            contentDescription = "error fail rank Tag"
                        )
                    }
                }
            }

            UiState.Loading -> {
                item(span = { GridItemSpan(currentLineSpan = maxLineSpan) }) {
                    LoadingAnimation.LoadingView()
                }
            }

            is UiState.Success -> {
                itemsIndexed(rankTag.data) { index, tagInfo ->
                    TagRankView(
                        text = tagInfo.name,
                        userCount = tagInfo.usageCnt,
                        index = (index + 1).toString(),
                        id = tagInfo.id,
                        onClick = { tagId ->
                            //TODO 카드 이동
                        }
                    )
                }
            }
        }
    }
}