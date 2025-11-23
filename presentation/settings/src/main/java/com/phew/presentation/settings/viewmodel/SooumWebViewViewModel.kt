package com.phew.presentation.settings.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phew.core.ui.model.navigation.WebViewUrlArgs
import com.phew.presentation.settings.webview.DefaultWebViewClient
import com.phew.presentation.settings.webview.DefaultWebChromeClient
import com.phew.presentation.settings.webview.WebViewConfiguration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SooumWebViewViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SooumWebViewUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<SooumWebViewEffect>(extraBufferCapacity = 1)
    val uiEffect = _uiEffect.asSharedFlow()

    fun initializeWithArgs(args: WebViewUrlArgs) {
        _uiState.update {
            it.copy(
                url = args.url,
                isLoading = false,
                configuration = WebViewConfiguration.fromUrl(
                    url = args.url,
                    client = DefaultWebViewClient(
                        onPageStarted = { setLoading(true) },
                        onPageFinished = { setLoading(false) },
                        onError = { handleError(it) }
                    ),
                    chromeClient = DefaultWebChromeClient { progress ->
                        if (progress >= 100) {
                            setLoading(false)
                        } else if (progress > 0) {
                            setLoading(true)
                        }
                    }
                )
            )
        }
    }

    private fun setLoading(isLoading: Boolean) {
        Log.d("SooumWebViewViewModel", "setLoading: $isLoading")
        _uiState.update { state ->
            if (state.isLoading == isLoading) state else state.copy(isLoading = isLoading)
        }
    }

    private fun handleError(message: String) {
        setLoading(false)
        sendEffect(SooumWebViewEffect.ShowError(message))
    }

    private fun sendEffect(effect: SooumWebViewEffect) {
        viewModelScope.launch {
            _uiEffect.emit(effect)
        }
    }
}

data class SooumWebViewUiState(
    val url: String = "",
    val isLoading: Boolean = false,
    val configuration: WebViewConfiguration = WebViewConfiguration.None
)

sealed interface SooumWebViewEffect {
    data class ShowError(val message: String) : SooumWebViewEffect
}
