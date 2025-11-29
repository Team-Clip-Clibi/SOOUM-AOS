package com.phew.presentation.tag.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.phew.core_design.AppBar.LeftAppBar
import com.phew.core_design.LoadingAnimation
import com.phew.core_design.NeutralColor
import com.phew.core_design.Primary
import com.phew.core_design.TextComponent
import com.phew.core_design.TextFiledComponent.SearchField
import com.phew.core_design.component.refresh.RefreshBox
import com.phew.core_design.component.refresh.pullToRefreshOffset
import com.phew.core_design.component.tag.TagRankView
import com.phew.domain.dto.FavoriteTag
import com.phew.domain.model.TagInfo
import com.phew.presentation.tag.R
import com.phew.presentation.tag.component.TagListItem
import com.phew.presentation.tag.viewmodel.TagUiEffect
import com.phew.presentation.tag.viewmodel.TagViewModel
import com.phew.presentation.tag.viewmodel.UiState
import com.phew.core_design.R as DesignR

@Composable
internal fun TagRoute(
    modifier: Modifier = Modifier,
    viewModel: TagViewModel = hiltViewModel(),
    navController: NavHostController,
    navigateToSearchScreen: () -> Unit,
    navigateToViewTags: (String, Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(currentBackStackEntry) {
        val isFavoriteChanged =
            currentBackStackEntry?.savedStateHandle?.get<Boolean>("favorite_changed")
        if (isFavoriteChanged == true) {
            viewModel.loadFavoriteTags()
            currentBackStackEntry?.savedStateHandle?.remove<Boolean>("favorite_changed")
        }
    }

    LaunchedEffect(Unit) {
        viewModel.tagScreenUiEffect
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { effect ->
                effect?.let {
                    when (it) {
                        TagUiEffect.NavigationSearchScreen -> {
                            navigateToSearchScreen()
                            viewModel.clearTagScreenUiEffect()
                        }

                        is TagUiEffect.ShowRemoveFavoriteTagToast -> {
                            Toast.makeText(
                                context,
                                context.getString(R.string.tag_favorite_delete, it.tagName),
                                Toast.LENGTH_SHORT
                            ).show()
                            viewModel.clearTagScreenUiEffect()
                        }

                        is TagUiEffect.ShowAddFavoriteTagToast -> {
                            Toast.makeText(
                                context,
                                context.getString(R.string.tag_favorite_add, it.tagName),
                                Toast.LENGTH_SHORT
                            ).show()
                            viewModel.clearTagScreenUiEffect()
                        }

                        is TagUiEffect.NavigateToViewTags -> {
                            navigateToViewTags(it.tagName, it.tagId)
                            viewModel.clearTagScreenUiEffect()
                        }
                        
                        is TagUiEffect.ShowNetworkErrorSnackbar -> {
                            // TagScreen에서는 네트워크 오류가 발생하지 않으므로 빈 처리
                            viewModel.clearTagScreenUiEffect()
                        }
                    }
                }
            }
    }

    TagScreen(
        modifier = modifier,
        nickName = uiState.nickName,
        favoriteTags = uiState.favoriteTags,
        tagRank = uiState.tagRank,
        isRefreshing = uiState.isRefreshing,
        onSearchView = viewModel::navToSearchScreen,
        onFavoriteClick = { tagId ->
            val tag = uiState.favoriteTags.find { it.id == tagId }
            tag?.let { viewModel.toggleFavoriteTag(it.id, it.name) }
        },
        onRefresh = viewModel::refresh,
        getTagFavoriteState = { tagId ->
            uiState.localFavoriteStates[tagId] ?: uiState.favoriteTags.any { it.id == tagId }
        },
        onTagRankClick =  viewModel::onTagRankClick,
        onTagClick = viewModel::onTagClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagScreen(
    modifier: Modifier,
    nickName: String,
    isRefreshing: Boolean,
    favoriteTags: List<FavoriteTag>,
    tagRank: UiState<List<TagInfo>>,
    onSearchView: () -> Unit,
    onFavoriteClick: (Long) -> Unit,
    onRefresh: () -> Unit,
    getTagFavoriteState: (Long) -> Boolean,
    onTagRankClick: (Long) -> Unit,
    onTagClick: (Long, String) -> Unit
) {
    val refreshState = rememberPullToRefreshState()
    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        topBar = {
            LeftAppBar(
                appBarText = stringResource(R.string.tag_top_title)
            )
        }
    ) { innerPadding ->
        RefreshBox(
            isRefresh = isRefreshing,
            onRefresh = onRefresh,
            state = refreshState,
            paddingValues = innerPadding
        ) {
            TagView(
                paddingValues = innerPadding,
                nickName = nickName,
                favoriteTags = favoriteTags,
                tagRank = tagRank,
                refreshState = refreshState,
                onSearchView = onSearchView,
                onFavoriteClick = onFavoriteClick,
                getTagFavoriteState = getTagFavoriteState,
                onTagRankClick = onTagRankClick,
                onTagClick = onTagClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagView(
    paddingValues: PaddingValues,
    nickName: String,
    favoriteTags: List<FavoriteTag>,
    tagRank: UiState<List<TagInfo>>,
    refreshState: PullToRefreshState,
    onSearchView: () -> Unit,
    onFavoriteClick: (Long) -> Unit,
    getTagFavoriteState: (Long) -> Boolean,
    onTagRankClick: (Long) -> Unit,
    onTagClick: (Long, String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(
            top = paddingValues.calculateTopPadding(),
            start = 16.dp,
            bottom = paddingValues.calculateBottomPadding(),
            end = 16.dp
        ),
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralColor.WHITE)
            .pullToRefreshOffset(state = refreshState, baseOffset = 0.dp)
    ) {
        // 검색 필드
        item(span = { GridItemSpan(currentLineSpan = maxLineSpan) }) {
            SearchField(
                value = "",
                isReadOnly = true,
                placeHolder = stringResource(R.string.tag_search_tag_placeholder),
                onFieldClick = onSearchView,
                focusRequester = null
            )
        }

        // 즐겨찾기 태그 섹션
        item(span = { GridItemSpan(currentLineSpan = maxLineSpan) }) {
            Column(
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text(
                    text = stringResource(R.string.tag_user_favorite, nickName),
                    style = TextComponent.TITLE_1_SB_18,
                    color = NeutralColor.BLACK,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                if (favoriteTags.isNotEmpty()) {
                    FavoriteTagsList(
                        favoriteTags = favoriteTags,
                        modifier = Modifier.fillMaxWidth(),
                        onTagClick = onTagClick,
                        onFavoriteClick = onFavoriteClick,
                        getTagFavoriteState = getTagFavoriteState
                    )
                } else {
                    EmptyFavoriteTag()
                }
            }
        }

        // 태그 랭킹 섹션
        item(span = { GridItemSpan(currentLineSpan = maxLineSpan) }) {
            Text(
                text = stringResource(R.string.tag_rank_title),
                style = TextComponent.TITLE_1_SB_18,
                color = NeutralColor.BLACK,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, bottom = 8.dp)
            )
        }

        when (tagRank) {
            is UiState.Fail -> {
                item(span = { GridItemSpan(currentLineSpan = maxLineSpan) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(DesignR.drawable.ic_deleted_card),
                            modifier = Modifier.size(48.dp),
                            contentDescription = "error fail rank tag"
                        )
                        Text(
                            text = tagRank.errorMessage,
                            style = TextComponent.BODY_1_M_14,
                            color = NeutralColor.GRAY_400,
                            modifier = Modifier.padding(top = 16.dp)
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
                itemsIndexed(tagRank.data) { index, tagInfo ->
                    TagRankView(
                        text = tagInfo.name,
                        userCount = tagInfo.usageCnt,
                        index = (index + 1).toString(),
                        id = tagInfo.id,
                        onClick = onTagRankClick
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteTagsList(
    favoriteTags: List<FavoriteTag>,
    modifier: Modifier = Modifier,
    onFavoriteClick: (Long) -> Unit = {},
    onTagClick: (Long, String) -> Unit,
    getTagFavoriteState: (Long) -> Boolean = { true }
) {
    val chunkedTags = favoriteTags.chunked(3)
    val hasMultiplePages = chunkedTags.size > 1
    val displayChunks = if (hasMultiplePages) {
        buildList {
            add(chunkedTags.last())
            addAll(chunkedTags)
            add(chunkedTags.first())
        }
    } else {
        chunkedTags
    }

    val pagerState = rememberPagerState(
        initialPage = if (hasMultiplePages) 1 else 0,
        pageCount = { displayChunks.size }
    )

    LaunchedEffect(hasMultiplePages, chunkedTags.size) {
        if (displayChunks.isEmpty()) return@LaunchedEffect
        val targetPage = if (hasMultiplePages) 1 else 0
        pagerState.scrollToPage(targetPage)
    }

    LaunchedEffect(pagerState, hasMultiplePages, chunkedTags.size) {
        if (!hasMultiplePages || displayChunks.isEmpty()) return@LaunchedEffect
        snapshotFlow { pagerState.isScrollInProgress }
            .collect { isScrolling ->
                if (!isScrolling) {
                    when (pagerState.currentPage) {
                        0 -> pagerState.scrollToPage(chunkedTags.size)
                        displayChunks.lastIndex -> pagerState.scrollToPage(1)
                    }
                }
            }
    }

    val currentPage by remember {
        derivedStateOf {
            if (chunkedTags.isEmpty()) {
                0
            } else if (hasMultiplePages) {
                val rawIndex = pagerState.currentPage - 1
                ((rawIndex % chunkedTags.size) + chunkedTags.size) % chunkedTags.size
            } else {
                pagerState.currentPage.coerceIn(0, chunkedTags.lastIndex)
            }
        }
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(144.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = chunkedTags.size > 1
            ) { pageIndex ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(144.dp)
                ) {
                    displayChunks[pageIndex].forEach { tag ->
                        TagListItem(
                            tag = tag.name,
                            tagId = tag.id,
                            isFavorite = getTagFavoriteState(tag.id),
                            onTagClick = onTagClick,
                            onFavoriteClick = onFavoriteClick
                        )
                    }
                }
            }
        }

        if (chunkedTags.size > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(chunkedTags.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = if (index == currentPage) Primary.DARK else NeutralColor.GRAY_300,
                                shape = CircleShape
                            )
                    )
                    if (index != chunkedTags.size - 1) {
                        Spacer(modifier = Modifier.size(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyFavoriteTag() {
    Box(
        modifier = Modifier
            .heightIn(min = 203.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier.padding(top = 35.dp),
                painter = painterResource(DesignR.drawable.ic_star_filled),
                contentDescription = "Favorite Empty",
                tint = NeutralColor.GRAY_200
            )

            Spacer(modifier = Modifier.padding(8.dp))

            Text(
                text = stringResource(R.string.tag_empty_favorite_tags),
                style = TextComponent.BODY_1_M_14,
                color = NeutralColor.GRAY_400,
                textAlign = TextAlign.Center
            )
        }

    }
}
