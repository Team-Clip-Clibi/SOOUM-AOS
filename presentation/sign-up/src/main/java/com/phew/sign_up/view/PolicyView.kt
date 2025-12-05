package com.phew.sign_up.view

import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phew.core_design.LoadingAnimation
import com.phew.sign_up.SignUpViewModel

@Composable
fun PolicyView(uri: String, viewModel: SignUpViewModel = hiltViewModel(), onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var webView: WebView? by remember { mutableStateOf(null) }
    BackHandler(enabled = true) {
        if (webView == null) {
            onBack()
            return@BackHandler
        }
        webView?.goBack()
    }
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            viewModel.loadPolicyView(isStart = true)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            viewModel.loadPolicyView(isStart = false)
                        }
                    }
                    loadUrl(uri)
                    webView = this
                }
            },
            update = { webView ->
                if (webView.url != uri) webView.loadUrl(uri)
            }
        )
        if (uiState.loadPolicyView) {
            Column(modifier = Modifier.align(Alignment.Center)) {
                LoadingAnimation.LoadingView()
            }
        }
    }
}