package com.phew.presentation.settings.screen

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.phew.core.ui.model.navigation.WebViewUrlArgs
import com.phew.core_design.NeutralColor
import com.phew.presentation.settings.viewmodel.SooumWebViewEffect
import com.phew.presentation.settings.viewmodel.SooumWebViewViewModel
import com.phew.presentation.settings.R
import com.phew.presentation.settings.webview.WebViewConfiguration

@Composable
fun SooumWebViewRoute(
    args: WebViewUrlArgs,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SooumWebViewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    LaunchedEffect(args) {
        viewModel.initializeWithArgs(args)
    }

    // Effect 처리
    LaunchedEffect(Unit) {
        viewModel.uiEffect
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { effect ->
                when (effect) {
                    is SooumWebViewEffect.ShowError -> {
                        snackbarHostState.showSnackbar(
                            message = context.getString(
                                R.string.webview_load_failed,
                                effect.message
                            )
                        )
                    }
                }
            }
    }

    SooumWebViewScreen(
        modifier = modifier,
        isLoading = uiState.isLoading,
        configuration = uiState.configuration,
        snackbarHostState = snackbarHostState,
        onBackPressed = onBackPressed
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SooumWebViewScreen(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    configuration: WebViewConfiguration?,
    snackbarHostState: SnackbarHostState,
    onBackPressed: () -> Unit
) {

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "")
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = ""
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NeutralColor.WHITE
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(NeutralColor.WHITE)
        ) {
            // 로딩 인디케이터
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // WebView
            if (configuration != null) {
                WebViewContent(
                    configuration = configuration,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = stringResource(R.string.no_url_message),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun WebViewContent(
    configuration: WebViewConfiguration,
    modifier: Modifier = Modifier
) {
    if (!configuration.isValid()) {
        return
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    setGeolocationEnabled(true)
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    builtInZoomControls = true
                    displayZoomControls = false
                }
                webViewClient = configuration.client
                webChromeClient = configuration.chromeClient
                loadUrl(configuration.url)
            }
        },
        update = { webView ->
            // URL이 다를 때만 새로 로드
            if (webView.url != configuration.url) {
                webView.loadUrl(configuration.url)
            }
        }
    )
}