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
import com.phew.domain.usecase.GetTagRank
import com.phew.domain.usecase.GetUserInfo
import com.phew.domain.usecase.RemoveFavoriteTag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
    val localFavoriteStates: Map<Long, Boolean> = emptyMap(), // 로컬 즐겨찾기 상태
    val currentSearchedTag: TagInfo? = null, // 현재 검색한 태그 정보
    val currentTagFavoriteState: Boolean = false, // 현재 검색한 태그의 즐겨찾기 상태
    val tagRank: UiState<List<TagInfo>> = UiState.Loading,
    val isRefreshing: Boolean = false,
)

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Fail(val errorMessage: String) : UiState<Nothing>
}

@HiltViewModel
class TagViewModel @Inject constructor(
    private val getTagCardsPaging: GetTagCardsPaging,
    private val getRelatedTags: GetRelatedTags,
    private val getUserInfo: GetUserInfo,
    private val getFavoriteTags: GetFavoriteTags,
    private val addFavoriteTag: AddFavoriteTag,
    private val removeFavoriteTag: RemoveFavoriteTag,
    private val getTagRank: GetTagRank,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TagUiState())
    
    private val _uiEffect = MutableStateFlow<TagUiEffect?>(null)
    val uiEffect = _uiEffect.asSharedFlow()

    private val refreshTrigger = MutableStateFlow(0)

    val uiState = combine(
        _uiState,
        refreshTrigger
    ) { state, _ ->
        state
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TagUiState()
    )

    init {
        observeSearchValue()
        loadUserInfo()
        observeRefreshTrigger()
    }

    private fun loadUserInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userInfo = getUserInfo(GetUserInfo.Param(key = BuildConfig.USER_INFO_KEY))
                _uiState.update {
                    it.copy(nickName = userInfo?.nickName ?: "")
                }
                SooumLog.d(TAG, "Success to load user info: ${userInfo?.nickName}")
            } catch (e: Exception) {
                SooumLog.e(TAG, "Failed to load user info: ${e.message}")
            }
        }
    }

    fun loadFavoriteTags() {
        viewModelScope.launch(Dispatchers.IO) {
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
                        flow {
                            emit(
                                getRelatedTags(
                                    GetRelatedTags.Param(
                                        resultCnt = 20L,
                                        tag = value
                                    )
                                )
                            )
                        }
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
        _uiState.update {
            it.copy(
                searchValue = "",
                recommendedTags = emptyList(),
                searchPerformed = false
            )
        }
    }

    fun performSearch(tag: String) {
        val selectedTag = _uiState.value.recommendedTags.find { it.name == tag }
        val tagId = selectedTag?.id ?: return

        SooumLog.d(TAG, "performSearch tag=$tag, tagId=$tagId")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 태그의 즐겨찾기 상태 확인을 위해 첫 번째 데이터 로드
                val cardsPagingFlow =
                    getTagCardsPaging(GetTagCardsPaging.Param(tagId)).cachedIn(viewModelScope)

                _uiState.update {
                    it.copy(
                        searchPerformed = true,
                        searchValue = tag,
                        recommendedTags = emptyList(),
                        cardDataItems = cardsPagingFlow,
                        currentSearchedTag = selectedTag,
                        currentTagFavoriteState = false // 초기값, 실제 값은 paging data에서 업데이트됨
                    )
                }
            } catch (e: Exception) {
                SooumLog.e(TAG, "Failed to perform search: ${e.message}")
                _uiEffect.emit(TagUiEffect.ShowNetworkErrorSnackbar { performSearch(tag) })
            }
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
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _uiState.value
            // FavoriteTagsList에서 사용하므로 기본값을 true로 설정 (이미 즐겨찾기된 태그들)
            val currentFavoriteState = currentState.localFavoriteStates[tagId] ?: true

            if (currentFavoriteState) {
                removeFavoriteTagAction(tagId, tagName)
            } else {
                addFavoriteTagAction(tagId, tagName)
            }
        }
    }

    private suspend fun removeFavoriteTagAction(tagId: Long, tagName: String) {
        try {
            val result = removeFavoriteTag(RemoveFavoriteTag.Param(tagId))
            when (result) {
                is DataResult.Success -> {
                    // 로컬 상태 업데이트 (즐겨찾기 해제) 및 favoriteTags 리스트에서 제거
                    _uiState.update { currentState ->
                        currentState.copy(
                            localFavoriteStates = currentState.localFavoriteStates + (tagId to false),
                            favoriteTags = currentState.favoriteTags.filter { it.id != tagId }
                        )
                    }
                    _uiEffect.emit(TagUiEffect.ShowRemoveFavoriteTagToast(tagName))
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
                    // 로컬 상태 업데이트 (즐겨찾기 추가) 및 즐겨찾기 목록 새로고침
                    _uiState.update { currentState ->
                        currentState.copy(
                            localFavoriteStates = currentState.localFavoriteStates + (tagId to true)
                        )
                    }
                    // 즐겨찾기 리스트 새로고침
                    loadFavoriteTags()
                    _uiEffect.emit(TagUiEffect.ShowAddFavoriteTagToast(tagName))
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

    // 현재 검색된 태그의 즐겨찾기 토글
    fun toggleCurrentSearchedTagFavorite() {
        val currentTag = _uiState.value.currentSearchedTag ?: return
        val currentFavoriteState = _uiState.value.currentTagFavoriteState

        SooumLog.d(
            TAG,
            "toggleCurrentSearchedTagFavorite: currentFavoriteState=$currentFavoriteState, tag=${currentTag.name}"
        )

        _uiState.update { it.copy(currentTagFavoriteState = !currentFavoriteState) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (currentFavoriteState) {
                    removeFavoriteTagAction(currentTag.id, currentTag.name)
                } else {
                    addFavoriteTagAction(currentTag.id, currentTag.name)
                }
            } catch (e: Exception) {
                SooumLog.e(TAG, "Failed to toggle favorite: ${e.message}")
                _uiState.update { it.copy(currentTagFavoriteState = currentFavoriteState) }
            }
        }
    }

    fun updateCurrentTagFavoriteState(isFavorite: Boolean) {
        SooumLog.d(TAG, "updateCurrentTagFavoriteState: isFavorite=$isFavorite")
        _uiState.update { it.copy(currentTagFavoriteState = isFavorite) }
    }
    
    fun resetSearchPerformed() {
        SooumLog.d(TAG, "resetSearchPerformed: No search results found")
        _uiState.update { it.copy(searchPerformed = false) }
    }

    fun refreshTagScreenData() {
        SooumLog.d(TAG, "refreshTagScreenData")
        loadFavoriteTags()
    }

    fun refresh() {
        _uiState.update { state ->
            state.copy(isRefreshing = true)
        }
        refreshTrigger.value++
    }
    
    private fun observeRefreshTrigger() {
        viewModelScope.launch {
            refreshTrigger.collect {
                loadFavoriteTags()
                tagRank()
            }
        }
    }
    
    fun onTagRankClick(tagId: Long) {
        viewModelScope.launch {
            val tagRank = _uiState.value.tagRank
            if (tagRank is UiState.Success) {
                val selectedTag = tagRank.data.find { it.id == tagId }
                selectedTag?.let { tag ->
                    _uiEffect.emit(TagUiEffect.NavigateToViewTags(tag.name, tag.id))
                }
            }
        }
    }

    fun onTagClick(tagId: Long, tagName: String) {
        viewModelScope.launch {
            _uiEffect.emit(TagUiEffect.NavigateToViewTags(tagName, tagId))
        }
    }
    
    fun loadTagCards(tagName: String, tagId: Long) {
        SooumLog.d(TAG, "loadTagCards tagName=$tagName, tagId=$tagId")
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cardsPagingFlow = getTagCardsPaging(GetTagCardsPaging.Param(tagId)).cachedIn(viewModelScope)
                
                _uiState.update {
                    it.copy(
                        searchPerformed = true,
                        searchValue = tagName,
                        recommendedTags = emptyList(),
                        cardDataItems = cardsPagingFlow,
                        currentSearchedTag = TagInfo(id = tagId, name = tagName, usageCnt = 0),
                        currentTagFavoriteState = false
                    )
                }
            } catch (e: Exception) {
                SooumLog.e(TAG, "Failed to load tag cards: ${e.message}")
                _uiEffect.emit(TagUiEffect.ShowNetworkErrorSnackbar { loadTagCards(tagName, tagId) })
            }
        }
    }
    
    fun refreshViewTags(tagName: String, tagId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isRefreshing = true) }
            try {
                // 새로운 Paging flow 생성
                val cardsPagingFlow = getTagCardsPaging(GetTagCardsPaging.Param(tagId)).cachedIn(viewModelScope)
                
                _uiState.update {
                    it.copy(cardDataItems = cardsPagingFlow)
                }
                
                // 약간의 지연 후 새로고침 상태 해제 (Paging 데이터 로드 시간 고려)
                kotlinx.coroutines.delay(500)
                _uiState.update { it.copy(isRefreshing = false) }
                
                SooumLog.d(TAG, "Successfully refreshed tag cards for $tagName")
            } catch (e: Exception) {
                SooumLog.e(TAG, "Failed to refresh tag cards: ${e.message}")
                _uiState.update { it.copy(isRefreshing = false) }
                _uiEffect.emit(TagUiEffect.ShowNetworkErrorSnackbar { refreshViewTags(tagName, tagId) })
            }
        }
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

sealed interface TagUiEffect {
    data object NavigationSearchScreen : TagUiEffect
    data class ShowAddFavoriteTagToast(val tagName: String) : TagUiEffect
    data class ShowRemoveFavoriteTagToast(val tagName: String) : TagUiEffect
    data class NavigateToViewTags(val tagName: String, val tagId: Long) : TagUiEffect
    data class ShowNetworkErrorSnackbar(val retryAction: () -> Unit) : TagUiEffect
}

private const val TAG = "TagViewModel"