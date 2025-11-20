package com.phew.presentation.tag.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import com.phew.core_design.AppBar.LeftAppBar
import com.phew.core_design.NeutralColor
import com.phew.core_design.Primary
import com.phew.core_design.TextComponent
import com.phew.core_design.TextFiledComponent.SearchField
import com.phew.domain.dto.FavoriteTag
import com.phew.presentation.tag.component.TagListItem
import com.phew.presentation.tag.R
import com.phew.core_design.R as DesignR
import com.phew.presentation.tag.viewmodel.TagUiEffect
import com.phew.presentation.tag.viewmodel.TagViewModel

@Composable
internal fun TagRoute(
    modifier: Modifier = Modifier,
    viewModel: TagViewModel = hiltViewModel(),
    navigateToSearchScreen: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEffect
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { effect ->
                effect?.let {
                    when (it) {
                        TagUiEffect.NavigationSearchScreen -> {
                            navigateToSearchScreen()
                            viewModel.clearUiEffect()
                        }

                        is TagUiEffect.ShowToast -> {
                            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                            viewModel.clearUiEffect()
                        }
                    }
                }
            }
    }

    TagScreen(
        modifier = modifier,
        nickName = uiState.nickName,
        favoriteTags = uiState.favoriteTags,
        onSearchView = viewModel::navToSearchScreen,
        onFavoriteClick = { tagId ->
            val tag = uiState.favoriteTags.find { it.id == tagId }
            tag?.let { viewModel.toggleFavoriteTag(it.id, it.name) }
        },
        getTagFavoriteState = viewModel::getTagFavoriteState
    )
}

@Composable
private fun TagScreen(
    modifier: Modifier,
    nickName: String,
    favoriteTags: List<FavoriteTag>,
    onSearchView: () -> Unit,
    onFavoriteClick: (Long) -> Unit,
    getTagFavoriteState: (Long) -> Boolean
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        topBar = {
            LeftAppBar(
                appBarText = stringResource(R.string.tag_top_title)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NeutralColor.WHITE)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)

        ) {
            SearchField(
                value = "",
                isReadOnly = true,
                placeHolder = stringResource(R.string.tag_search_tag_placeholder),
                onFieldClick = {
                    onSearchView()
                }
            )

            Text(
                text = stringResource(R.string.tag_user_favorite, nickName),
                style = TextComponent.TITLE_1_SB_18,
                color = NeutralColor.BLACK,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 8.dp)
            )
            
            if (favoriteTags.isNotEmpty()) {
                FavoriteTagsList(
                    favoriteTags = favoriteTags,
                    modifier = Modifier.fillMaxWidth(),
                    onFavoriteClick = onFavoriteClick,
                    getTagFavoriteState = getTagFavoriteState
                )
            } else {
                EmptyFavoriteTag()
            }

        }
    }
}

@Composable
private fun FavoriteTagsList(
    favoriteTags: List<FavoriteTag>,
    modifier: Modifier = Modifier,
    onFavoriteClick: (Long) -> Unit = {},
    getTagFavoriteState: (Long) -> Boolean = { true }
) {
    val chunkedTags = favoriteTags.chunked(3)
    val pagerState = rememberPagerState(pageCount = { chunkedTags.size })
    
    Column(modifier = modifier) {
        // 페이지 리스트
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { pageIndex ->
            Column {
                chunkedTags[pageIndex].forEach { tag ->
                    TagListItem(
                        tag = tag.name,
                        tagId = tag.id,
                        isFavorite = getTagFavoriteState(tag.id),
                        onClick = onFavoriteClick
                    )
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
                                color = if (index == pagerState.currentPage) Primary.DARK else NeutralColor.GRAY_300,
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
            .heightIn(min= 203.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally
        ){
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
                color = NeutralColor.GRAY_400
            )
        }

    }
}