package com.phew.presentation.tag.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.phew.core_design.AppBar.IconLeftSearchAppBar
import com.phew.core_design.CustomFont
import com.phew.core_design.component.card.CommentBodyContent
import com.phew.domain.dto.TagCardContent
import com.phew.domain.model.TagInfo
import com.phew.presentation.tag.R
import com.phew.presentation.tag.component.SearchListItem

@Composable
internal fun SearchRoute() {

}

@Composable
private fun SearchScreen(
    modifier: Modifier,
    searchValue: String,
    items: List<TagInfo>,
    cardDataItems: LazyPagingItems<TagCardContent>,
    listState: LazyListState,
    onValueChange: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onItemClick: (String) -> Unit,
    onClickCard: (Long) -> Unit,
    onBackPressed: () -> Unit
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        topBar = {
            IconLeftSearchAppBar(
                value = searchValue,
                placeholder = stringResource(R.string.tag_search_tag_placeholder),
                onValueChange = onValueChange,
                onDeleteClick = onDeleteClick,
                onBackClick = onBackPressed
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Spacer(Modifier.padding(top = 8.dp))

            if (cardDataItems.itemCount > 0) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding() + 63.dp)
                ) {
                    items(
                        count = cardDataItems.itemCount,
                        key = cardDataItems.itemKey { data -> data.cardId }
                    ) { index ->
                        val item = cardDataItems[index]
                        if (item != null) {
                            Spacer(Modifier.padding(top = 8.dp))

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
            } else if (items.isNotEmpty()) {
                LazyColumn(
                    state = listState
                ) {
                    itemsIndexed(
                        items = items,
                        key = { _, item -> item.id }
                    ) { _, item ->
                        SearchListItem(
                            title = item.name,
                            content = "${item.usageCnt}",
                            onClick = { onItemClick(item.name) }
                        )
                    }
                }
            }
        }
    }
}