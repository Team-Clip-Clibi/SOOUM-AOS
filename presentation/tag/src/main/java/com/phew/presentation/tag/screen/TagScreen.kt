package com.phew.presentation.tag.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phew.core_design.AppBar.LeftAppBar
import com.phew.core_design.LoadingAnimation
import com.phew.core_design.NeutralColor
import com.phew.core_design.component.refresh.RefreshBox
import com.phew.core_design.component.refresh.TOP_CONTENT_OFFSET
import com.phew.core_design.component.refresh.pullToRefreshOffset
import com.phew.core_design.component.tag.TagRankView
import com.phew.domain.model.TagInfo
import com.phew.presentation.tag.R
import com.phew.presentation.tag.viewmodel.TagState
import com.phew.presentation.tag.viewmodel.TagViewModel
import com.phew.presentation.tag.viewmodel.UiState

@Composable
internal fun TagRoute(
    modifier: Modifier = Modifier,
    viewModel: TagViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TagScreen(
        modifier = modifier,
        onBackPressed = onBackPressed,
        onRefresh = viewModel::refresh,
        uiState = uiState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagScreen(
    modifier: Modifier,
    onBackPressed: () -> Unit,
    onRefresh: () -> Unit,
    uiState: TagState,
) {
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
            onRefresh = onRefresh,
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
            .pullToRefreshOffset(
                state = refreshState,
                baseOffset = TOP_CONTENT_OFFSET
            )
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