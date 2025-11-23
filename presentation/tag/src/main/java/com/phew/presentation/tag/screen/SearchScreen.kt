package com.phew.presentation.tag.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.phew.core_common.log.SooumLog
import com.phew.core_design.AppBar.SearchAppBar
import com.phew.core_design.CustomFont
import com.phew.core_design.MediumButton.IconPrimary
import com.phew.core_design.NeutralColor
import com.phew.core_design.R as DesignR
import com.phew.core_design.TextComponent
import com.phew.core_design.Warning
import com.phew.core_design.component.card.CommentBodyContent
import com.phew.domain.dto.TagCardContent
import com.phew.domain.model.TagInfo
import com.phew.presentation.tag.R
import com.phew.presentation.tag.component.SearchListItem
import com.phew.presentation.tag.viewmodel.TagUiEffect
import com.phew.presentation.tag.viewmodel.TagViewModel

@Composable
internal fun SearchRoute(
    modifier: Modifier = Modifier,
    viewModel: TagViewModel = hiltViewModel(),
    onClickCard: (Long) -> Unit,
    onBackPressed: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cardDataItems = uiState.cardDataItems.collectAsLazyPagingItems()
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Toast 처리 및 Snackbar 처리
    LaunchedEffect(Unit) {
        viewModel.uiEffect
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { effect ->
                effect?.let {
                    when (it) {
                        is TagUiEffect.ShowAddFavoriteTagToast -> {
                            val message = context.getString(R.string.tag_favorite_add, it.tagName)
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            viewModel.clearUiEffect()
                        }
                        is TagUiEffect.ShowRemoveFavoriteTagToast -> {
                            val message = context.getString(R.string.tag_favorite_delete, it.tagName)
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            viewModel.clearUiEffect()
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
                            viewModel.clearUiEffect()
                        }
                        else -> {
                            viewModel.clearUiEffect()
                        }
                    }
                }
            }
    }

    // cardDataItems에서 첫 번째 아이템의 isFavorite 상태를 ViewModel에 업데이트
    LaunchedEffect(cardDataItems.itemCount, uiState.searchPerformed) {
        if (uiState.searchPerformed) {
            if (cardDataItems.itemCount > 0) {
                try {
                    val firstItem = cardDataItems[0]
                    if (firstItem != null) {
                        SooumLog.d("SearchRoute", "Updating favorite state: ${firstItem.isFavorite}")
                        viewModel.updateCurrentTagFavoriteState(firstItem.isFavorite)
                    }
                } catch (e: Exception) {
                    SooumLog.e("SearchRoute", "Error accessing first item: ${e.message}")
                }
            } else {
                // 검색했지만 결과가 없으면 searchPerformed를 false로 설정
                viewModel.resetSearchPerformed()
            }
        }
    }

    SearchScreen(
        modifier = modifier,
        searchValue = uiState.searchValue,
        recommendedTags = uiState.recommendedTags,
        searchPerformed = uiState.searchPerformed,
        cardDataItems = cardDataItems,
        listState = listState,
        gridState = gridState,
        onValueChange = viewModel::onValueChange,
        onDeleteClick = viewModel::onDeleteClick,
        onItemClick = viewModel::performSearch,
        onSearch = { viewModel.performSearch(uiState.searchValue) },
        onClickCard = onClickCard,
        onBackPressed = onBackPressed,
        isFavorite = uiState.currentTagFavoriteState,
        onFavoriteToggle = viewModel::toggleCurrentSearchedTagFavorite,
        snackbarHostState = snackbarHostState
    )
}

@Composable
private fun SearchScreen(
    modifier: Modifier,
    searchValue: String,
    recommendedTags: List<TagInfo>,
    searchPerformed: Boolean,
    cardDataItems: LazyPagingItems<TagCardContent>,
    listState: LazyListState,
    gridState: LazyGridState,
    onValueChange: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onItemClick: (String) -> Unit,
    onSearch: () -> Unit,
    onClickCard: (Long) -> Unit,
    onBackPressed: () -> Unit,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    SooumLog.d("SearchScreen", "recommendedTags=$recommendedTags")
    val focusManager = LocalFocusManager.current
    
    // 스크롤 감지를 위한 상태 (list와 grid 모두)
    val isListScrolling by remember {
        derivedStateOf {
            listState.isScrollInProgress
        }
    }
    
    val isGridScrolling by remember {
        derivedStateOf {
            gridState.isScrollInProgress
        }
    }
    
    // 스크롤 시 키보드 숨기기
    LaunchedEffect(isListScrolling, isGridScrolling) {
        if (isListScrolling || isGridScrolling) {
            focusManager.clearFocus()
        }
    }
    
    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            SearchAppBar(
                value = searchValue,
                placeholder = stringResource(R.string.tag_search_tag_placeholder),
                onValueChange = onValueChange,
                onDeleteClick = onDeleteClick,
                onBackClick = onBackPressed,
                onSearch = onSearch,
                icon = {
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
                },
                isIcon = searchPerformed
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NeutralColor.WHITE)
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }
        ) {
            Spacer(Modifier.padding(top = 8.dp))

            if (searchPerformed && cardDataItems.itemCount == 0) {
                EmptySearchCard()
            } else if (searchPerformed && cardDataItems.itemCount > 0) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    state = gridState,
                    modifier = modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding() + 63.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
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
                                onClick = { cardId ->
                                    focusManager.clearFocus()
                                    onClickCard(cardId)
                                }
                            )
                        }
                    }
                }
            } else if (recommendedTags.isNotEmpty()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.height(144.dp)
                ) {
                    itemsIndexed(
                        items = recommendedTags,
                        key = { _, item -> item.id }
                    ) { _, item ->
                        SearchListItem(
                            title = item.name,
                            content = "${item.usageCnt}",
                            onClick = { 
                                focusManager.clearFocus()
                                onItemClick(item.name)
                            }
                        )
                    }
                }
            } else if (searchValue.isNotBlank() && recommendedTags.isEmpty()) {
                // 검색어가 있지만 추천 태그 결과가 없을 때
                EmptyCardList()
            }
        }
    }
}

@Composable
private fun EmptySearchCard() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(com.phew.core_design.R.drawable.ic_noti_no_data),
            contentDescription = "no notify"
        )
        Text(
            text = stringResource(R.string.tag_search_no_card),
            style = TextComponent.BODY_1_M_14,
            color = NeutralColor.GRAY_400,
            modifier = Modifier.padding(top = 20.dp)
        )
    }
}


@Composable
private fun EmptyCardList() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(com.phew.core_design.R.drawable.ic_deleted_card),
            contentDescription = "no notify",
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
