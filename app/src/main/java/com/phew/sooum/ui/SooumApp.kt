package com.phew.sooum.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.phew.core.ui.component.back.SooumBackHandler
import com.phew.core.ui.component.home.SooumBottomBar
import com.phew.core.ui.state.SooumAppState
import com.phew.core.ui.state.rememberSooumAppState
import com.phew.sooum.navigation.SooumNavHost

//  TODO AppState NetworkMonitor 추가
@Composable
fun SooumApp(
    appVersionUpdate: () -> Unit,
    finish: () -> Unit,
    appState: SooumAppState = rememberSooumAppState(),
    webView: (String) -> Unit,
    isExpend: Boolean,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        SooumNavHost(
            modifier = Modifier.fillMaxSize(),
            appState = appState,
            appVersionUpdate = appVersionUpdate,
            finish = finish,
            webView = webView
        )

        SooumBottomBar(
            navController = appState.navController,
            appState = appState
        )

        SooumBackHandler(appState = appState)
    }

}