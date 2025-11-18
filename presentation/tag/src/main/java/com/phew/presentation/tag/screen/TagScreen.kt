package com.phew.presentation.tag.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.phew.core_design.AppBar.LeftAppBar
import com.phew.core_design.NeutralColor
import com.phew.core_design.TextFiledComponent.SearchField
import com.phew.presentation.tag.R
import com.phew.presentation.tag.viewmodel.TagViewModel

@Composable
internal fun TagRoute(
    modifier: Modifier = Modifier,
    viewModel: TagViewModel = hiltViewModel(),
    onBackPressed: () -> Unit
) {
    TagScreen(
        modifier = modifier,
        onBackPressed = onBackPressed
    )
}

@Composable
private fun TagScreen(
    modifier: Modifier,
    onBackPressed: () -> Unit
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
                onFieldClick = {}
            )
        }
    }
}