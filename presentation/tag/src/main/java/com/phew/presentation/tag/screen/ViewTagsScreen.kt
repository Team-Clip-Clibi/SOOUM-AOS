package com.phew.presentation.tag.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.phew.core_design.AppBar.IconLeftAndRightAppBar
import com.phew.core_design.CustomFont
import com.phew.core_design.DialogComponent
import com.phew.core_design.MediumButton.IconPrimary
import com.phew.core_design.NeutralColor
import com.phew.core_design.TextComponent
import com.phew.core_design.Warning
import com.phew.core_design.component.card.CommentBodyContent
import com.phew.core_design.component.refresh.RefreshBox
import com.phew.domain.dto.TagCardContent
import com.phew.presentation.tag.R
import com.phew.presentation.tag.viewmodel.TagUiEffect
import com.phew.presentation.tag.viewmodel.TagViewModel
import com.phew.core_design.R as DesignR


@Composable
internal fun ViewTagsRoute(
    modifier: Modifier = Modifier,
    tagName: String,
    tagId: Long,
    viewModel: TagViewModel = hiltViewModel(),
    onClickCard: (Long) -> Unit,
    onBackPressed: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cardDataItems = uiState.cardDataItems.collectAsLazyPagingItems()
    val gridState = rememberLazyGridState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // favoriteTags 로드를 먼저 확인
    LaunchedEffect(tagName, tagId) {
        if (uiState.favoriteTags.isEmpty()) {
            viewModel.loadFavoriteTags()
        }
    }

    // favoriteTags가 로드된 후 tagCards 로드 (즐겨찾기 상태 포함)
    LaunchedEffect(tagName, tagId, uiState.favoriteTags) {
        if (uiState.favoriteTags.isNotEmpty() || uiState.nickName.isNotEmpty()) {
            val currentFavoriteState = viewModel.getTagFavoriteState(tagId)
            viewModel.loadTagCards(tagName, tagId, currentFavoriteState)
        }
    }

    // Toast 처리 및 Snackbar 처리
    LaunchedEffect(Unit) {
        viewModel.viewTagsScreenUiEffect
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { effect ->
                effect?.let {
                    when (it) {
                        is TagUiEffect.ShowAddFavoriteTagToast -> {
                            val message = context.getString(R.string.tag_favorite_add, it.tagName)
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            viewModel.clearViewTagsScreenUiEffect()
                        }
                        is TagUiEffect.ShowRemoveFavoriteTagToast -> {
                            val message = context.getString(R.string.tag_favorite_delete, it.tagName)
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            viewModel.clearViewTagsScreenUiEffect()
                        }
                        is TagUiEffect.ShowNetworkErrorSnackbar -> {
                            val result = snackbarHostState.showSnackbar(
                                message = context.getString(R.string.tag_network_error_message),
                                actionLabel = context.getString(R.string.tag_network_error_retry),
                                duration = SnackbarDuration.Indefinite
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                it.retryAction()
                            }
                            viewModel.clearViewTagsScreenUiEffect()
                        }
                        else -> {
                            viewModel.clearViewTagsScreenUiEffect()
                        }
                    }
                }
            }
    }

    // cardDataItems에서 첫 번째 아이템의 isFavorite 상태를 ViewModel에 업데이트
    LaunchedEffect(cardDataItems.itemCount, uiState.searchPerformed) {
        if (uiState.searchPerformed && cardDataItems.itemCount > 0) {
            try {
                val firstItem = cardDataItems[0]
                if (firstItem != null) {
                    viewModel.updateCurrentTagFavoriteState(firstItem.isFavorite)
                }
            } catch (e: Exception) {
                // 에러 처리
            }
        }
    }

    ViewTagsScreen(
        modifier = modifier,
        tagName = tagName,
        cardDataItems = cardDataItems,
        gridState = gridState,
        isRefreshing = uiState.isRefreshing,
        onClickCard = onClickCard,
        onBackPressed = onBackPressed,
        isFavorite = uiState.currentTagFavoriteState,
        onFavoriteToggle = viewModel::toggleCurrentSearchedTagFavorite,
        onRefresh = { viewModel.refreshViewTags(tagName, tagId) },
        snackbarHostState = snackbarHostState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewTagsScreen(
    modifier: Modifier,
    tagName: String,
    cardDataItems: LazyPagingItems<TagCardContent>,
    isFavorite: Boolean,
    isRefreshing: Boolean,
    gridState: LazyGridState,
    onRefresh: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onBackPressed: () -> Unit,
    onClickCard: (Long) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        snackbarHost = { DialogComponent.CustomAnimationSnackBarHost(hostState = snackbarHostState) },
        topBar = {
            IconLeftAndRightAppBar(
                title = tagName,
                onBackClick = onBackPressed,
                rightIcon = {
                    IconPrimary(
                        icon = {
                            Icon(
                                painter = painterResource(DesignR.drawable.ic_star_filled),
                                contentDescription = "favorite",
                                tint = if (isFavorite) Warning.M_YELLOW else NeutralColor.GRAY_200
                            )
                        },
                        onClick = onFavoriteToggle
                    )
                }
            )
        }
    ) { innerPadding ->
        val refreshState = rememberPullToRefreshState()

        RefreshBox(
            isRefresh = isRefreshing,
            onRefresh = onRefresh,
            state = refreshState,
            paddingValues = innerPadding
        ) {
            if (cardDataItems.itemCount == 0) {
                EmptyViewTags()
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    state = gridState,
                    contentPadding = PaddingValues(
                        top = innerPadding.calculateTopPadding(),
                        start = 1.dp,
                        end = 1.dp,
                        bottom = innerPadding.calculateBottomPadding()
                    ),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                    horizontalArrangement = Arrangement.spacedBy(1.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .background(NeutralColor.WHITE)
                ) {
                    items(
                        count = cardDataItems.itemCount,
                        key = cardDataItems.itemKey { data -> data.cardId }
                    ) { index ->
                        val item = cardDataItems[index]
                        if (item != null) {
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

@Composable
private fun EmptyViewTags() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(DesignR.drawable.ic_deleted_card_sv),
            contentDescription = "no data",
            modifier = Modifier
                .height(130.dp)
                .width(220.dp)
        )
        Text(
            text = stringResource(R.string.tag_not_search_card),
            style = TextComponent.BODY_1_M_14,
            color = NeutralColor.GRAY_400,
            modifier = Modifier.padding(top = 20.dp)
        )
    }
}