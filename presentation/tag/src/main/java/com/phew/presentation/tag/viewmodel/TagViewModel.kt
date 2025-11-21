package com.phew.presentation.tag.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phew.core_common.DomainResult
import com.phew.domain.model.TagInfo
import com.phew.domain.usecase.GetTagRank
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagViewModel @Inject constructor(
    private val getTagRank: GetTagRank,
) : ViewModel() {
    private var _uiState = MutableStateFlow(TagState())
    val uiState: StateFlow<TagState> = _uiState.asStateFlow()

    init {
        tagRank()
    }

    fun refresh() {
        _uiState.update { state ->
            state.copy(isRefreshing = true)
        }
        tagRank()
    }

    private fun tagRank() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = getTagRank()) {
                is DomainResult.Failure -> {
                    _uiState.update { state ->
                        state.copy(
                            tagRank = UiState.Fail(errorMessage = result.error),
                            isRefreshing = false
                        )
                    }
                }

                is DomainResult.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            tagRank = UiState.Success(data = result.data),
                            isRefreshing = false
                        )
                    }
                }
            }
        }
    }

}

data class TagState(
    val tagRank: UiState<List<TagInfo>> = UiState.Loading,
    val isRefreshing: Boolean = false,
)

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Fail(val errorMessage: String) : UiState<Nothing>
}