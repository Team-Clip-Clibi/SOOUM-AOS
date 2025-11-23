package com.phew.presentation.settings.webview

import android.webkit.WebChromeClient
import android.webkit.WebViewClient

interface WebViewConfiguration {
    val url: String
    val client: WebViewClient
    val chromeClient: WebChromeClient

    fun isValid() = url.isNotEmpty()

    companion object {
        val None = object : WebViewConfiguration {
            override val url: String = ""
            override val client: WebViewClient = WebViewClient()
            override val chromeClient: WebChromeClient = WebChromeClient()
        }

        fun fromUrl(
            url: String,
            client: WebViewClient = DefaultWebViewClient(),
            chromeClient: WebChromeClient = DefaultWebChromeClient(),
            onProgressChanged: ((Int) -> Unit)? = null
        ): WebViewConfiguration = DefaultWebViewConfiguration(
            url = url,
            client = client,
            chromeClient = chromeClient
        )
    }
}

private data class DefaultWebViewConfiguration(
    override val url: String,
    override val client: WebViewClient,
    override val chromeClient: WebChromeClient
) : WebViewConfiguration