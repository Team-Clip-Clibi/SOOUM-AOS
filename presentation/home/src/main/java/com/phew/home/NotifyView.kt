package com.phew.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.phew.core_design.NeutralColor
import com.phew.home.viewModel.HomeViewModel

@Composable
fun NotifyView(viewModel: HomeViewModel, backClick: () -> Unit) {
    BackHandler {
        backClick()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = NeutralColor.WHITE)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) { }
}