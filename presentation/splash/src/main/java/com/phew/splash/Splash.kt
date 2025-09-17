package com.phew.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.phew.core_design.DialogComponent
import com.phew.core_design.NeutralColor
import com.phew.core_design.Primary


@Composable
fun Splash(
    viewModel: SplashViewModel,
    nextPage: () -> Unit,
    update: () -> Unit,
    finish: () -> Unit,
    home: () -> Unit,
) {
    val uiState by viewModel.usState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(uiState) {
        when (uiState) {
            UiState.Success -> {
                nextPage()
            }

            UiState.Error -> {
                snackBarHostState.showSnackbar(
                    message = context.getString(com.phew.core_design.R.string.error_network),
                    duration = SnackbarDuration.Short
                )
            }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState) { data ->
                DialogComponent.SnackBar(data)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Primary.MAIN)
                .navigationBarsPadding()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                ),
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                painter = painterResource(id = com.phew.core_design.R.drawable.ic_sooum_black),
                contentDescription = "app logo",
                tint = NeutralColor.WHITE,
                modifier = Modifier
                    .width(200.dp)
                    .height(33.dp)
                    .padding(1.dp)
            )
            if (uiState is UiState.Fail) {
                DialogComponent.DefaultButtonOne(
                    title = stringResource(R.string.splash_dialog_update_title),
                    description = stringResource(R.string.splash_dialog_update_description),
                    buttonText = stringResource(R.string.splash_dialog_update_btn),
                    onClick = {
                        nextPage()
                    },
                    onDismiss = {
                        nextPage()
                    }
                )
            }
        }
    }
}
