package com.phew.feed.notification

import android.graphics.Bitmap
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phew.core_design.LoadingAnimation
import com.phew.feed.viewModel.FeedViewModel
import android.content.Intent
import androidx.core.net.toUri

@Composable
fun WebView(url: String, viewModel: FeedViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BackHandler(enabled = true) {
        onBack()
    }
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        userAgentString =
                            "Mozilla/5.0 (Linux; Android 12; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36"
                        allowFileAccess = false
                        allowContentAccess = false
                    }
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?,
                        ): Boolean {
                            val requestUrl = request?.url?.toString() ?: return false
                            if (requestUrl.contains("instagram.com")) {
                                return try {
                                    val intent = Intent(Intent.ACTION_VIEW, requestUrl.toUri())
                                    view?.context?.startActivity(intent)
                                    true
                                } catch (e: Exception) {
                                    Log.e("WebView", "External link failed", e)
                                    false
                                }
                            }
                            return false
                        }

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            viewModel.setLoadNoticeView(data = true)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            viewModel.setLoadNoticeView(data = false)
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?,
                        ) {
                            super.onReceivedError(view, request, error)
                            Log.e("PolicyView", "WebView Error: ${error?.description}")
                        }
                    }
                    loadUrl(url)
                }
            },
        )
        if (uiState.loadNoticeView) {
            Column(modifier = Modifier.align(Alignment.Center)) {
                LoadingAnimation.LoadingView()
            }
        }
    }
}