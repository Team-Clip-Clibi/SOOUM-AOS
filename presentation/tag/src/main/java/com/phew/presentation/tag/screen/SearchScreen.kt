package com.phew.presentation.tag.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
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
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.phew.core.ui.model.navigation.CardDetailArgs
import com.phew.core_common.log.SooumLog
import com.phew.core_design.AppBar.SearchAppBar
import com.phew.core_design.CustomFont
import com.phew.core_design.DialogComponent
import com.phew.core_design.DialogComponent.DeletedCardDialog
import com.phew.core_design.LoadingAnimation
import com.phew.core_design.MediumButton.IconPrimary
import com.phew.core_design.NeutralColor
import com.phew.core_design.TextComponent
import com.phew.core_design.Warning
import com.phew.core_design.component.card.CommentBodyContent
import com.phew.core_design.component.toast.SooumToast
import com.phew.domain.dto.TagCardContent
import com.phew.domain.model.TagInfo
import com.phew.presentation.tag.R
import com.phew.presentation.tag.component.SearchListItem
import com.phew.presentation.tag.viewmodel.TagUiEffect
import com.phew.presentation.tag.viewmodel.TagViewModel
import com.phew.presentation.tag.viewmodel.UiState
import com.phew.core_design.R as DesignR

@Composable
internal fun SearchRoute(
    modifier: Modifier = Modifier,
    viewModel: TagViewModel = hiltViewModel(),
    navigateToDetail: (cardDetailArgs: CardDetailArgs) -> Unit,
    onBackPressed: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cardDataItems = uiState.cardDataItems.collectAsLazyPagingItems()
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val yOffset = 8.dp.value.toInt()

    // Toast 처리 및 Snackbar 처리
    LaunchedEffect(Unit) {
        viewModel.searchScreenUiEffect
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { effect ->
                effect?.let {
                    when (it) {
                        is TagUiEffect.ShowAddFavoriteTagToast -> {
                            val message = context.getString(R.string.tag_favorite_add, it.tagName)
                            SooumToast.makeToast(
                                context,
                                message,
                                SooumToast.LENGTH_SHORT,
                                yOffset = yOffset
                            ).show()
                            viewModel.clearSearchScreenUiEffect()
                        }

                        is TagUiEffect.ShowRemoveFavoriteTagToast -> {
                            val message =
                                context.getString(R.string.tag_favorite_delete, it.tagName)
                            SooumToast.makeToast(
                                context,
                                message,
                                SooumToast.LENGTH_SHORT,
                                yOffset = yOffset
                            ).show()
                            viewModel.clearSearchScreenUiEffect()
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
                            viewModel.clearSearchScreenUiEffect()
                        }

                        is TagUiEffect.NavigateToDetail -> {
                              navigateToDetail(it.cardDetailArgs)
                              viewModel.clearSearchScreenUiEffect()
                        }

                        else -> {
                            viewModel.clearSearchScreenUiEffect()
                        }
                    }
                }
            }
    }

    // 삭제된 카드 상태 감지
    var deletedCardId by remember { mutableStateOf<Long?>(null) }
    
    LaunchedEffect(uiState.checkCardDelete) {
        if (uiState.checkCardDelete is UiState.Success) {
            deletedCardId = (uiState.checkCardDelete as UiState.Success<Long>).data
            showDeleteDialog = true
        }
    }

    if (showDeleteDialog && deletedCardId != null) {
        val onDialogHandled = {
            deletedCardId?.let { viewModel.removeDeletedCard(it) }
            showDeleteDialog = false
            deletedCardId = null
        }
        DeletedCardDialog(
            onDismiss = onDialogHandled,
            onConfirm = onDialogHandled
        )
    }

    // cardDataItems에서 첫 번째 아이템의 isFavorite 상태를 ViewModel에 업데이트
    LaunchedEffect(cardDataItems.itemCount, uiState.searchPerformed, uiState.isSearchLoading) {
        if (uiState.searchPerformed && !uiState.isSearchLoading) {
            if (cardDataItems.itemCount > 0) {
                try {
                    val firstItem = cardDataItems[0]
                    if (firstItem != null) {
                        SooumLog.d(
                            "SearchRoute",
                            "Updating favorite state: ${firstItem.isFavorite}"
                        )
                        viewModel.updateCurrentTagFavoriteState(firstItem.isFavorite)
                    }
                } catch (e: Exception) {
                    SooumLog.e("SearchRoute", "Error accessing first item: ${e.message}")
                }
            }
            // 로딩이 완료되고 여전히 결과가 없으면 빈 결과 상태로 유지
            // searchPerformed를 false로 변경하지 않음
        }
    }

    SearchScreen(
        modifier = modifier,
        searchValue = uiState.searchValue,
        recommendedTags = uiState.recommendedTags,
        searchPerformed = uiState.searchPerformed,
        isSearchLoading = uiState.isSearchLoading,
        searchDataLoaded = uiState.searchDataLoaded,
        cardDataItems = cardDataItems,
        listState = listState,
        gridState = gridState,
        onValueChange = viewModel::onValueChange,
        onInputFieldRemoveClick = viewModel::onDeleteClick,
        onItemClick = viewModel::performSearch,
        onSearch = { viewModel.performSearch(uiState.searchValue) },
        onClickCard = viewModel::navigateToDetail,
        onBackPressed = onBackPressed,
        isFavorite = uiState.currentTagFavoriteState,
        onFavoriteToggle = viewModel::toggleCurrentSearchedTagFavorite,
        snackbarHostState = snackbarHostState,
        autoFocus = true
    )
}

@Composable
private fun SearchScreen(
    modifier: Modifier,
    searchValue: String,
    recommendedTags: List<TagInfo>,
    searchPerformed: Boolean,
    isSearchLoading: Boolean,
    searchDataLoaded: Boolean,
    cardDataItems: LazyPagingItems<TagCardContent>,
    listState: LazyListState,
    gridState: LazyGridState,
    onValueChange: (String) -> Unit,
    onInputFieldRemoveClick: () -> Unit,
    onItemClick: (String) -> Unit,
    onSearch: () -> Unit,
    onClickCard: (Long) -> Unit,
    onBackPressed: () -> Unit,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    snackbarHostState: SnackbarHostState,
    autoFocus: Boolean = false
) {
    SooumLog.d("SearchScreen", "recommendedTags=$recommendedTags")
    val focusManager = LocalFocusManager.current
    var isSearchFieldFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // Auto focus on search field when autoFocus is true
    LaunchedEffect(autoFocus) {
        if (autoFocus) {
            focusRequester.requestFocus()
            isSearchFieldFocused = true
        }
    }

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

    // 스크롤 시 키보드 숨기기 및 포커스 상태 해제
    LaunchedEffect(isListScrolling, isGridScrolling) {
        if (isListScrolling || isGridScrolling) {
            focusManager.clearFocus()
            isSearchFieldFocused = false
        }
    }

    // PagingData 로딩 상태 체크
    val isPagingLoading = cardDataItems.loadState.refresh is LoadState.Loading

    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        snackbarHost = { DialogComponent.CustomAnimationSnackBarHost(hostState = snackbarHostState) },
        topBar = {
            SearchAppBar(
                value = searchValue,
                placeholder = stringResource(R.string.tag_search_tag_placeholder),
                onValueChange = { value ->
                    onValueChange(value)
                    isSearchFieldFocused = true
                },
                onInputFieldRemoveClick = onInputFieldRemoveClick,
                onBackClick = onBackPressed,
                onSearch = {
                    focusManager.clearFocus()
                    isSearchFieldFocused = false
                    onSearch()
                },
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
                isIcon = searchPerformed && cardDataItems.itemCount > 0,
                focusRequester = focusRequester,
                showDeleteIcon = isSearchFieldFocused
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
                        isSearchFieldFocused = false
                    })
                }
        ) {
            Spacer(Modifier.padding(top = 8.dp))

            when {
                // 1. 로딩 중
                isSearchLoading || (searchPerformed && isPagingLoading) -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LoadingAnimation.LoadingView()
                    }
                }
                // 2. 검색 수행 후 카드가 있음
                searchPerformed && cardDataItems.itemCount > 0 -> {
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
                                        isSearchFieldFocused = false
                                        onClickCard(cardId)
                                    }
                                )
                            }
                        }
                    }
                }
                // 3. 검색 수행 후 카드가 없음 (더 정확한 LoadState 확인)
                searchPerformed && searchDataLoaded && !isSearchLoading && !isPagingLoading && cardDataItems.itemCount == 0 -> {
                    // 추천 태그 클릭으로 검색한 경우 vs 직접 입력한 검색어로 검색한 경우 구분
                    // 현재 검색값이 원래 추천 태그 목록에 있었는지 확인하기 위해 searchValue 사용
                    val isFromRecommendedTag = recommendedTags.any { it.name == searchValue } || 
                                             (recommendedTags.isEmpty() && searchValue.isNotBlank())
                    
                    if (isFromRecommendedTag) {
                        EmptyCardList() // 추천 태그를 클릭했지만 해당 태그에 카드가 없는 경우
                    } else {
                        EmptySearchCard() // 검색어를 입력하고 완료를 눌렀지만 관련 검색 결과가 없는 경우
                    }
                }
                // 4. 추천 태그가 있음 (아직 검색 수행 안함)
                recommendedTags.isNotEmpty() -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxHeight()
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
                                    isSearchFieldFocused = false
                                    onItemClick(item.name)
                                }
                            )
                        }
                    }
                }
                // 5. 검색어는 있지만 추천 태그가 없고 검색도 안함
                searchValue.isNotBlank() && recommendedTags.isEmpty() && !searchPerformed && !isSearchFieldFocused -> {
                    EmptyCardList()
                }
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
            painter = painterResource(DesignR.drawable.ic_noti_no_data),
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
            painter = painterResource(DesignR.drawable.ic_deleted_card),
            contentDescription = "no notify",
            contentScale = ContentScale.Fit,
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
