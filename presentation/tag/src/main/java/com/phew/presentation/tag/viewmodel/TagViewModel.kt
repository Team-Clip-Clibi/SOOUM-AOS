package com.phew.presentation.tag.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.phew.core_common.DataResult
import com.phew.core_common.DomainResult
import com.phew.core_common.log.SooumLog
import com.phew.domain.BuildConfig
import com.phew.domain.dto.FavoriteTag
import com.phew.domain.dto.TagCardContent
import com.phew.domain.model.TagInfo
import com.phew.domain.model.TagInfoList
import com.phew.domain.usecase.AddFavoriteTag
import com.phew.domain.usecase.GetFavoriteTags
import com.phew.domain.usecase.GetRelatedTags
import com.phew.domain.usecase.GetTagCardsPaging
import com.phew.domain.usecase.GetUserInfo
import com.phew.domain.usecase.RemoveFavoriteTag
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
    val cardDataItems: Flow<PagingData<TagCardContent>> = flowOf(PagingData.empty()),
    val nickName: String = "",
    val favoriteTags: List<FavoriteTag> = emptyList(),
    val localFavoriteStates: Map<Long, Boolean> = emptyMap() // 로컬 즐겨찾기 상태
)

@HiltViewModel
class TagViewModel @Inject constructor(
    private val getTagCardsPaging: GetTagCardsPaging,
    private val getRelatedTags: GetRelatedTags,
    private val getUserInfo: GetUserInfo,
    private val getFavoriteTags: GetFavoriteTags,
    private val addFavoriteTag: AddFavoriteTag,
    private val removeFavoriteTag: RemoveFavoriteTag
) : ViewModel() {

    private val _uiState = MutableStateFlow(TagUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableStateFlow<TagUiEffect?>(null)
    val uiEffect  = _uiEffect.asSharedFlow()

    init {
        observeSearchValue()
        loadUserInfo()
        loadFavoriteTags()
    }
    
    private fun loadUserInfo() {
        viewModelScope.launch {
            try {
                val userInfo = getUserInfo(GetUserInfo.Param(key =  BuildConfig.USER_INFO_KEY))
                _uiState.update { 
                    it.copy(nickName = userInfo?.nickName ?: "")
                }
                SooumLog.d(TAG, "Success to load user info: ${userInfo?.nickName}")
            } catch (e: Exception) {
                SooumLog.e(TAG, "Failed to load user info: ${e.message}")
            }
        }
    }
    
    private fun loadFavoriteTags() {
        viewModelScope.launch {
            try {
                val result = getFavoriteTags()
                when (result) {
                    is DataResult.Success -> {
                        // 최대 9개만 표시
                        val limitedTags = result.data.favoriteTags.take(9)
                        _uiState.update { 
                            it.copy(favoriteTags = limitedTags)
                        }
                        SooumLog.d(TAG, "Favorite tags loaded: ${limitedTags.size}")
                    }
                    is DataResult.Fail -> {
                        SooumLog.e(TAG, "Failed to load favorite tags: ${result.message}")
                        _uiState.update { it.copy(favoriteTags = emptyList()) }
                    }
                }
            } catch (e: Exception) {
                SooumLog.e(TAG, "Exception loading favorite tags: ${e.message}")
                _uiState.update { it.copy(favoriteTags = emptyList()) }
            }
        }
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
    
    fun toggleFavoriteTag(tagId: Long, tagName: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val currentFavoriteState = currentState.localFavoriteStates[tagId] ?: true // 기본적으로 true
            
            if (currentFavoriteState) {
                // 현재 즐겨찾기인 경우 -> 제거
                removeFavoriteTagAction(tagId, tagName)
            } else {
                // 현재 즐겨찾기가 아닌 경우 -> 추가
                addFavoriteTagAction(tagId, tagName)
            }
        }
    }
    
    private suspend fun removeFavoriteTagAction(tagId: Long, tagName: String) {
        try {
            val result = removeFavoriteTag(RemoveFavoriteTag.Param(tagId))
            when (result) {
                is DataResult.Success -> {
                    // 로컬 상태 업데이트 (즐겨찾기 해제)
                    _uiState.update { currentState ->
                        currentState.copy(
                            localFavoriteStates = currentState.localFavoriteStates + (tagId to false)
                        )
                    }
                    _uiEffect.emit(TagUiEffect.ShowToast("'$tagName'을 관심 태그에서 삭제했어요."))
                    SooumLog.d(TAG, "Successfully removed favorite tag: $tagName")
                }
                is DataResult.Fail -> {
                    SooumLog.e(TAG, "Failed to remove favorite tag: ${result.message}")
                }
            }
        } catch (e: Exception) {
            SooumLog.e(TAG, "Exception removing favorite tag: ${e.message}")
        }
    }
    
    private suspend fun addFavoriteTagAction(tagId: Long, tagName: String) {
        try {
            val result = addFavoriteTag(AddFavoriteTag.Param(tagId))
            when (result) {
                is DataResult.Success -> {
                    // 로컬 상태 업데이트 (즐겨찾기 추가)
                    _uiState.update { currentState ->
                        currentState.copy(
                            localFavoriteStates = currentState.localFavoriteStates + (tagId to true)
                        )
                    }
                    _uiEffect.emit(TagUiEffect.ShowToast("'$tagName'을 관심 태그에 추가했어요."))
                    SooumLog.d(TAG, "Successfully added favorite tag: $tagName")
                }
                is DataResult.Fail -> {
                    SooumLog.e(TAG, "Failed to add favorite tag: ${result.message}")
                }
            }
        } catch (e: Exception) {
            SooumLog.e(TAG, "Exception adding favorite tag: ${e.message}")
        }
    }
    
    // 태그의 즐겨찾기 상태를 가져오는 함수
    fun getTagFavoriteState(tagId: Long): Boolean {
        val localState = _uiState.value.localFavoriteStates[tagId]
        return localState ?: true // 로컬 상태가 없으면 기본적으로 즐겨찾기로 간주 (FavoriteTagsList에서 사용)
    }
}

sealed interface TagUiEffect {
    data object NavigationSearchScreen : TagUiEffect
    data class ShowToast(val message: String) : TagUiEffect
}

private const val TAG = "TagViewModel"