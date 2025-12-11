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
import com.phew.domain.usecase.GetProfileInfo
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
    val isSearchLoading: Boolean = false,
    val cardDataItems: Flow<PagingData<TagCardContent>> = flowOf(PagingData.empty()),
    val nickName: String = "",
    val favoriteTags: List<FavoriteTag> = emptyList(),
    val localFavoriteStates: Map<Long, Boolean> = emptyMap(), // 로컬 즐겨찾기 상태
    val currentSearchedTag: TagInfo? = null, // 현재 검색한 태그 정보
    val currentTagFavoriteState: Boolean = false, // 현재 검색한 태그의 즐겨찾기 상태
    val tagRank: UiState<List<TagInfo>> = UiState.Loading,
    val isRefreshing: Boolean = false,
    val requestedTagCards: Set<String> = emptySet() // 요청한 태그 카드 목록 (tagId:tagName 형태)
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
    private val getProfileInfo: GetProfileInfo,
    private val getFavoriteTags: GetFavoriteTags,
    private val addFavoriteTag: AddFavoriteTag,
    private val removeFavoriteTag: RemoveFavoriteTag,
    private val getTagRank: GetTagRank,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TagUiState())
    
    // 화면별 UiEffect 분리
    private val _tagScreenUiEffect = MutableStateFlow<TagUiEffect?>(null)
    private val _searchScreenUiEffect = MutableStateFlow<TagUiEffect?>(null)
    private val _viewTagsScreenUiEffect = MutableStateFlow<TagUiEffect?>(null)
    private val _commonUiEffect = MutableStateFlow<TagUiEffect?>(null)
    
    val tagScreenUiEffect = _tagScreenUiEffect.asSharedFlow()
    val searchScreenUiEffect = _searchScreenUiEffect.asSharedFlow()
    val viewTagsScreenUiEffect = _viewTagsScreenUiEffect.asSharedFlow()
    val commonUiEffect = _commonUiEffect.asSharedFlow()
    
    // 기존 호환성을 위한 통합 UiEffect (deprecated 예정)
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
                val profileNickName = getProfileInfo(BuildConfig.PROFILE_KEY)
                val userInfo = getUserInfo(GetUserInfo.Param(key = BuildConfig.USER_INFO_KEY))
                val resolvedNickName = profileNickName ?: userInfo?.nickName.orEmpty()
                _uiState.update {
                    it.copy(nickName = resolvedNickName)
                }
                SooumLog.d(TAG, "Success to load nickname: $resolvedNickName")
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
                        _uiState.update { currentState ->
                            val favoriteTagIds = limitedTags.map { it.id }.toSet()
                            val allTagIds = currentState.localFavoriteStates.keys + favoriteTagIds
                            val updatedLocalStates = allTagIds.associateWith { it in favoriteTagIds }

                            currentState.copy(
                                favoriteTags = limitedTags,
                                localFavoriteStates = updatedLocalStates
                            )
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
        _uiState.update { it.copy(searchValue = value, searchPerformed = false, isSearchLoading = false) }
    }

    fun onDeleteClick() {
        _uiState.update {
            it.copy(
                searchValue = "",
                recommendedTags = emptyList(),
                searchPerformed = false,
                isSearchLoading = false
            )
        }
    }

    fun performSearch(tag: String) {
        val selectedTag = _uiState.value.recommendedTags.find { it.name == tag }
        val tagId = selectedTag?.id ?: return

        SooumLog.d(TAG, "performSearch tag=$tag, tagId=$tagId")

        // 즉시 로딩 상태 설정
        _uiState.update {
            it.copy(
                isSearchLoading = true,
                recommendedTags = emptyList()
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 태그의 즐겨찾기 상태 확인을 위해 첫 번째 데이터 로드
                val cardsPagingFlow =
                    getTagCardsPaging(GetTagCardsPaging.Param(tagId)).cachedIn(viewModelScope)

                _uiState.update {
                    it.copy(
                        searchPerformed = true,
                        isSearchLoading = false,
                        searchValue = tag,
                        cardDataItems = cardsPagingFlow,
                        currentSearchedTag = selectedTag,
                        currentTagFavoriteState = false // 초기값, 실제 값은 paging data에서 업데이트됨
                    )
                }
            } catch (e: Exception) {
                SooumLog.e(TAG, "Failed to perform search: ${e.message}")
                _uiState.update { it.copy(isSearchLoading = false) }
                emitSearchScreenEffect(TagUiEffect.ShowNetworkErrorSnackbar { performSearch(tag) })
            }
        }
    }

    fun navToSearchScreen() {
        viewModelScope.launch {
            emitTagScreenEffect(TagUiEffect.NavigationSearchScreen)
        }
    }

    // 화면별 UiEffect 클리어 함수들
    fun clearTagScreenUiEffect() {
        viewModelScope.launch {
            _tagScreenUiEffect.emit(null)
        }
    }

    fun clearSearchScreenUiEffect() {
        viewModelScope.launch {
            _searchScreenUiEffect.emit(null)
        }
    }

    fun clearViewTagsScreenUiEffect() {
        viewModelScope.launch {
            _viewTagsScreenUiEffect.emit(null)
        }
    }


    // 화면별 UiEffect 발생 함수들
    private suspend fun emitTagScreenEffect(effect: TagUiEffect) {
        _tagScreenUiEffect.emit(effect)
        _uiEffect.emit(effect) // 기존 호환성
    }

    private suspend fun emitSearchScreenEffect(effect: TagUiEffect) {
        _searchScreenUiEffect.emit(effect)
        _uiEffect.emit(effect) // 기존 호환성
    }

    private suspend fun emitViewTagsScreenEffect(effect: TagUiEffect) {
        _viewTagsScreenUiEffect.emit(effect)
        _uiEffect.emit(effect) // 기존 호환성
    }
    
    private suspend fun emitCommonEffect(effect: TagUiEffect) {
        _commonUiEffect.emit(effect)
        _tagScreenUiEffect.emit(effect)
        _searchScreenUiEffect.emit(effect)
        _viewTagsScreenUiEffect.emit(effect)
        _uiEffect.emit(effect) // 기존 호환성
    }

    fun toggleFavoriteTag(tagId: Long, tagName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentState = _uiState.value
            // localFavoriteStates에 있으면 그 값을 사용, 없으면 favoriteTags 리스트에 있는지 확인
            val currentFavoriteState = currentState.localFavoriteStates[tagId] 
                ?: currentState.favoriteTags.any { it.id == tagId }

            if (currentFavoriteState) {
                removeFavoriteTagAction(tagId, tagName, removeFromList = false) // TagScreen에서는 리스트에서 제거하지 않음
            } else {
                addFavoriteTagAction(tagId, tagName)
            }
        }
    }

    private suspend fun removeFavoriteTagAction(tagId: Long, tagName: String, removeFromList: Boolean = true) {
        try {
            val result = removeFavoriteTag(RemoveFavoriteTag.Param(tagId))
            when (result) {
                is DataResult.Success -> {
                    // 로컬 상태 업데이트 (즐겨찾기 해제)
                    _uiState.update { currentState ->
                        currentState.copy(
                            localFavoriteStates = currentState.localFavoriteStates + (tagId to false),
                            // removeFromList가 true일 때만 favoriteTags에서 제거 (다른 화면에서 사용)
                            favoriteTags = if (removeFromList) {
                                currentState.favoriteTags.filter { it.id != tagId }
                            } else {
                                currentState.favoriteTags // TagScreen에서는 리스트 유지
                            }
                        )
                    }
                    emitCommonEffect(TagUiEffect.ShowRemoveFavoriteTagToast(tagName))
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
                    emitCommonEffect(TagUiEffect.ShowAddFavoriteTagToast(tagName))
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
        // 로컬 상태가 있으면 그 값을 사용, 없으면 favoriteTags 리스트에 있는지 확인
        return localState ?: _uiState.value.favoriteTags.any { it.id == tagId }
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

    // 특정 태그의 즐겨찾기 토글 (tagId와 tagName을 직접 받아서 처리)
    fun toggleTagFavorite(tagId: Long, tagName: String) {
        val currentFavoriteState = _uiState.value.favoriteTags.any { it.id == tagId }

        SooumLog.d(
            TAG,
            "toggleTagFavorite: currentFavoriteState=$currentFavoriteState, tagId=$tagId, tagName=$tagName"
        )

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (currentFavoriteState) {
                    removeFavoriteTagAction(tagId, tagName)
                } else {
                    addFavoriteTagAction(tagId, tagName)
                }
            } catch (e: Exception) {
                SooumLog.e(TAG, "Failed to toggle favorite: ${e.message}")
            }
        }
    }

    fun updateCurrentTagFavoriteState(isFavorite: Boolean) {
        SooumLog.d(TAG, "updateCurrentTagFavoriteState: isFavorite=$isFavorite")
        _uiState.update { it.copy(currentTagFavoriteState = isFavorite) }
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
                    emitTagScreenEffect(TagUiEffect.NavigateToViewTags(tag.name, tag.id))
                }
            }
        }
    }

    fun onTagClick(tagId: Long, tagName: String) {
        viewModelScope.launch {
            emitTagScreenEffect(TagUiEffect.NavigateToViewTags(tagName, tagId))
        }
    }
    
    fun loadTagCards(tagName: String, tagId: Long, initialFavoriteState: Boolean = false) {
        val tagKey = "$tagId:$tagName"
        
        // 이미 요청한 태그인지 확인
        if (_uiState.value.requestedTagCards.contains(tagKey)) {
            SooumLog.d(TAG, "loadTagCards already requested for $tagKey, skipping")
            return
        }
        
        SooumLog.d(TAG, "loadTagCards tagName=$tagName, tagId=$tagId, initialFavoriteState=$initialFavoriteState")
        
        // 요청 상태 업데이트
        _uiState.update { it.copy(requestedTagCards = it.requestedTagCards + tagKey) }
        
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
                        currentTagFavoriteState = initialFavoriteState
                    )
                }
            } catch (e: Exception) {
                SooumLog.e(TAG, "Failed to load tag cards: ${e.message}")
                // 실패시 요청 상태에서 제거
                _uiState.update { it.copy(requestedTagCards = it.requestedTagCards - tagKey) }
                emitViewTagsScreenEffect(TagUiEffect.ShowNetworkErrorSnackbar { loadTagCards(tagName, tagId) })
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
                emitViewTagsScreenEffect(TagUiEffect.ShowNetworkErrorSnackbar { refreshViewTags(tagName, tagId) })
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
