package com.phew.sooum.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.phew.core.ui.component.back.SooumBackHandler
import com.phew.core.ui.component.home.SooumBottomBar
import com.phew.core.ui.state.SooumAppState
import com.phew.core.ui.state.rememberSooumAppState
import com.phew.core_design.DialogComponent
import com.phew.core_design.R
import com.phew.sooum.navigation.SooumNavHost

//  TODO AppState NetworkMonitor 추가
@Composable
fun SooumApp(
    appVersionUpdate: () -> Unit,
    finish: () -> Unit,
    appState: SooumAppState = rememberSooumAppState(),
) {
    var globalDialogMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        SooumNavHost(
            modifier = Modifier.fillMaxSize(),
            appState = appState,
            appVersionUpdate = appVersionUpdate,
            finish = finish,
            onServerMessage = { globalDialogMessage = it },
        )

        SooumBottomBar(
            navController = appState.navController,
            appState = appState
        )

        SooumBackHandler(appState = appState)
    }

    globalDialogMessage?.let { message ->
        DialogComponent.NoDescriptionButtonOne(
            title = message,
            buttonText = stringResource(R.string.common_okay),
            onClick = { globalDialogMessage = null },
            onDismiss = { globalDialogMessage = null },
        )
    }
}
