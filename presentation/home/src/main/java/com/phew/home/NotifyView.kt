package com.phew.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.res.stringResource
import com.phew.core_design.AppBar
import com.phew.core_design.NeutralColor
import com.phew.home.viewModel.HomeViewModel

@Composable
fun NotifyView(viewModel: HomeViewModel, backClick: () -> Unit) {
    var selectIndex by remember { mutableIntStateOf(NAV_NOTICE_ALL_INDEX) }
    var isTabsVisible by remember { mutableStateOf(true) }
    val lazyListState = rememberLazyListState()
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                isTabsVisible = available.y > 0 || lazyListState.firstVisibleItemIndex == 0
                return Offset.Zero
            }
        }
    }
    BackHandler {
        backClick()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = NeutralColor.WHITE)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        TopBar(
            allClick = {

            },
            followClick = {

            },
            cardClick = {

            },
            noticeClick = {

            },

            backClick = backClick,
            isTabsVisible = isTabsVisible,
            selectIndex = selectIndex
        )
    }
}

@Composable
private fun TopBar(
    backClick: () -> Unit,
    allClick: () -> Unit,
    followClick: () -> Unit,
    cardClick: () -> Unit,
    noticeClick: () -> Unit,
    isTabsVisible: Boolean,
    selectIndex: Int
) {
    AppBar.IconLeftAppBar(
        onClick = backClick,
        appBarText = stringResource(R.string.home_notice_top_bar)
    )
    AnimatedNoticeTabLayout(
        allClick = allClick,
        followClick = followClick,
        cardClick = cardClick,
        noticeClick = noticeClick,
        isTabsVisible = isTabsVisible,
        selectTabData = selectIndex
    )
}