package com.phew.sooum.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.phew.core.ui.component.home.SooumBottomBar
import com.phew.core.ui.state.SooumAppState
import com.phew.core.ui.state.rememberSooumAppState
import com.phew.core_design.component.SooumNavigationBar
import com.phew.sooum.navigation.SooumNavHost

//  TODO AppState NetworkMonitor 추가
//  TODO BackHandler 분리
@Composable
fun SooumApp(
    appVersionUpdate: () -> Unit,
    finish: () -> Unit,
    appState: SooumAppState = rememberSooumAppState()
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        SooumNavHost(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Vertical)),
            appState = appState,
            appVersionUpdate = appVersionUpdate,
            finish = finish
        )

        SooumBottomBar(
            navController = appState.navController
        )
    }

}