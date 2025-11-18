package com.phew.presentation.tag.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.log.SooumLog
import com.phew.domain.dto.TagCardContent
import com.phew.domain.model.TagInfo
import com.phew.domain.model.TagInfoList
import com.phew.domain.usecase.GetRelatedTags
import com.phew.domain.usecase.GetTagCardsPaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TagUiState(
    val searchValue: String = "",
    val recommendedTags: List<TagInfo> = emptyList(),
    val searchPerformed: Boolean = false,
    val cardDataItems: Flow<PagingData<TagCardContent>> = flowOf(PagingData.empty())
)

@HiltViewModel
class TagViewModel @Inject constructor(
    private val getTagCardsPaging: GetTagCardsPaging,
    private val getRelatedTags: GetRelatedTags
) : ViewModel() {

    private val _uiState = MutableStateFlow(TagUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableStateFlow<TagUiEffect?>(null)
    val uiEffect  = _uiEffect.asSharedFlow()

    init {
        observeSearchValue()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeSearchValue() {
        viewModelScope.launch {
            _uiState
                .map { it.searchValue }
                .debounce(300)
                .distinctUntilChanged()
                .flatMapLatest { value ->
                    if (value.trim().isNotEmpty()) {
                        flow { emit(getRelatedTags(GetRelatedTags.Param(resultCnt = 20L, tag = value))) }
                    } else {
                        flowOf(DataResult.Success(TagInfoList(tagInfos = emptyList())) as DataResult<TagInfoList>)
                    }
                }
                .collect { result ->
                    when (result) {
                        is DataResult.Success -> {
                            SooumLog.d(TAG, "success=${result.data.tagInfos}")
                            _uiState.update { it.copy(recommendedTags = result.data.tagInfos) }
                        }
                        is DataResult.Fail -> {
                            // Handle error
                            _uiState.update { it.copy(recommendedTags = emptyList()) }
                        }
                    }
                }
        }
    }

    fun onValueChange(value: String) {
        _uiState.update { it.copy(searchValue = value, searchPerformed = false) }
    }

    fun onDeleteClick() {
        _uiState.update { it.copy(searchValue = "", recommendedTags = emptyList(), searchPerformed = false) }
    }

    fun performSearch(tag: String) {
        val selectedTag = _uiState.value.recommendedTags.find { it.name == tag }
        val tagId = selectedTag?.id ?: return

        SooumLog.d(TAG, "performSearch tag=$tag, tagId=$tagId")
        _uiState.update {
            it.copy(
                searchPerformed = true,
                searchValue = tag,
                recommendedTags = emptyList(),
                cardDataItems = getTagCardsPaging(GetTagCardsPaging.Param(tagId)).cachedIn(viewModelScope)
            )
        }
    }

    fun navToSearchScreen() {
        viewModelScope.launch {
            _uiEffect.emit(TagUiEffect.NavigationSearchScreen)
        }
    }
    
    fun clearUiEffect() {
        viewModelScope.launch {
            _uiEffect.emit(null)
        }
    }
}

sealed interface TagUiEffect {
    data object NavigationSearchScreen : TagUiEffect
}

private const val TAG = "TagViewModel"