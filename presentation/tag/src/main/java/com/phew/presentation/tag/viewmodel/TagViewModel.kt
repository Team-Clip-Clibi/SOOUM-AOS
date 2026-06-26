package com.phew.presentation.tag.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.phew.core.ui.model.navigation.CardDetailArgs
import com.phew.core_common.CardDetailTrace
import com.phew.core_common.errorMessage
import com.phew.core_common.log.SooumLog
import com.phew.domain.BuildConfig
import com.phew.domain.dto.FavoriteTag
import com.phew.domain.dto.TagCardContent
import com.phew.domain.model.TagInfo
import com.phew.domain.model.TagInfoList
import com.phew.domain.usecase.orchestrator.TagUseCaseOrchestrator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
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
    val requestedTagCards: Set<String> = emptySet(), // 요청한 태그 카드 목록 (tagId:tagName 형태)
    val viewTagsDataLoaded: Boolean = false,
    val searchDataLoaded: Boolean = false,
    val isRelatedTagSearch: Boolean = false,
    val checkCardDelete: UiState<Long> = UiState.None,
    val deletedCardIds: Set<Long> = emptySet()
)

sealed interface UiState<out T> {
    data object None: UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Fail(val errorMessage: String) : UiState<Nothing>
}

@HiltViewModel
class TagViewModel @Inject constructor(
    private val tagOrchestrator: TagUseCaseOrchestrator,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TagUiState())

    private val _uiEffect = MutableSharedFlow<TagUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    private val refreshTrigger = MutableStateFlow(0)

    val uiState = combine(
        _uiState,
        refreshTrigger
    ) { state, _ ->
        if (state.deletedCardIds.isNotEmpty()) {
            state.copy(
                cardDataItems = state.cardDataItems.map { pagingData ->
                    pagingData.filter { card ->
                        !state.deletedCardIds.contains(card.cardId)
                    }
                }
            )
        } else {
            state
        }
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
                val profileNickName = tagOrchestrator.profileNickName(BuildConfig.PROFILE_KEY)
                val userInfo = tagOrchestrator.userInfo(BuildConfig.USER_INFO_KEY)
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
                val result = tagOrchestrator.favoriteTags()
                result.fold(
                    onSuccess = { tagList ->
                        // 최대 9개만 표시
                        val limitedTags = tagList.favoriteTags.take(9)
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
                    },
                    onFailure = { throwable ->
                        SooumLog.e(TAG, "Failed to load favorite tags: ${Result.failure<Unit>(throwable).errorMessage()}")
                        _uiState.update { it.copy(favoriteTags = emptyList()) }
                    }
                )
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
                                tagOrchestrator.relatedTags(value)
                            )
                        }
                    } else {
                        flowOf(Result.success(TagInfoList(tagInfos = emptyList())))
                    }
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { tagInfoList ->
                            SooumLog.d(TAG, "success=${tagInfoList.tagInfos}")
                            _uiState.update { it.copy(recommendedTags = tagInfoList.tagInfos) }
                        },
                        onFailure = {
                            // Handle error
                            _uiState.update { it.copy(recommendedTags = emptyList()) }
                        }
                    )
                }
        }
    }

    fun onValueChange(value: String) {
        _uiState.update {
            it.copy(
                searchValue = value,
                searchPerformed = false,
                isSearchLoading = false,
                isRelatedTagSearch = false
            )
        }
    }

    fun onDeleteClick() {
        _uiState.update {
            it.copy(
                searchValue = "",
                recommendedTags = emptyList(),
                searchPerformed = false,
                isSearchLoading = false,
                isRelatedTagSearch = false
            )
        }
    }

    fun performSearch(tag: String, isRelatedTag: Boolean = false) {
        val selectedTag = _uiState.value.recommendedTags.find { it.name == tag }
        val tagId = selectedTag?.id ?: return

        SooumLog.d(TAG, "performSearch tag=$tag, tagId=$tagId")

        // 즉시 로딩 상태 설정
        _uiState.update {
            it.copy(
                isSearchLoading = true,
                recommendedTags = emptyList(),
                isRelatedTagSearch = isRelatedTag
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 태그의 즐겨찾기 상태 확인을 위해 첫 번째 데이터 로드
                val cardsPagingFlow =
                    tagOrchestrator.tagCards(tagId).cachedIn(viewModelScope)

                _uiState.update {
                    it.copy(
                        searchPerformed = true,
                        isSearchLoading = false,
                        searchValue = tag,
                        cardDataItems = cardsPagingFlow,
                        currentSearchedTag = selectedTag,
                        currentTagFavoriteState = false, // 초기값, 실제 값은 paging data에서 업데이트됨
                        searchDataLoaded = true, // 데이터 로드 완료
                        isRelatedTagSearch = isRelatedTag
                    )
                }
            } catch (e: Exception) {
                SooumLog.e(TAG, "Failed to perform search: ${e.message}")
                _uiState.update { it.copy(isSearchLoading = false, searchDataLoaded = true) } // 실패 시에도 로드 완료로 처리
                emitEffect(TagUiEffect.ShowNetworkErrorSnackbar { performSearch(tag) })
            }
        }
    }

    fun navToSearchScreen() {
        viewModelScope.launch {
            tagOrchestrator.logClickSearchView()
            emitEffect(TagUiEffect.NavigationSearchScreen)
        }
    }

    private suspend fun emitEffect(effect: TagUiEffect) {
        _uiEffect.emit(effect)
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
            val result = tagOrchestrator.removeFavoriteTag(tagId)
            result.fold(
                onSuccess = {
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
                    emitEffect(TagUiEffect.ShowRemoveFavoriteTagToast(tagName))
                    SooumLog.d(TAG, "Successfully removed favorite tag: $tagName")
                },
                onFailure = { throwable ->
                    SooumLog.e(TAG, "Failed to remove favorite tag: ${Result.failure<Unit>(throwable).errorMessage()}")
                }
            )
        } catch (e: Exception) {
            SooumLog.e(TAG, "Exception removing favorite tag: ${e.message}")
        }
    }

    private suspend fun addFavoriteTagAction(tagId: Long, tagName: String) {
        try {
            val result = tagOrchestrator.addFavoriteTag(tagId)
            result.fold(
                onSuccess = {
                    // 로컬 상태 업데이트 (즐겨찾기 추가) 및 즐겨찾기 목록 새로고침
                    _uiState.update { currentState ->
                        currentState.copy(
                            localFavoriteStates = currentState.localFavoriteStates + (tagId to true)
                        )
                    }
                    // 즐겨찾기 리스트 새로고침
                    loadFavoriteTags()
                    emitEffect(TagUiEffect.ShowAddFavoriteTagToast(tagName))
                    SooumLog.d(TAG, "Successfully added favorite tag: $tagName")
                },
                onFailure = { throwable ->
                    SooumLog.e(TAG, "Failed to add favorite tag: ${Result.failure<Unit>(throwable).errorMessage()}")
                }
            )
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
            tagOrchestrator.logSelectPopularTag()
            val tagRank = _uiState.value.tagRank
            if (tagRank is UiState.Success) {
                val selectedTag = tagRank.data.find { it.id == tagId }
                selectedTag?.let { tag ->
                    emitEffect(TagUiEffect.NavigateToViewTags(tag.name, tag.id))
                }
            }
        }
    }

    fun onTagClick(tagId: Long, tagName: String) {
        viewModelScope.launch {
            emitEffect(TagUiEffect.NavigateToViewTags(tagName, tagId))
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
                val cardsPagingFlow = tagOrchestrator.tagCards(tagId).cachedIn(viewModelScope)
                
                _uiState.update {
                    it.copy(
                        searchPerformed = true,
                        searchValue = tagName,
                        recommendedTags = emptyList(),
                        cardDataItems = cardsPagingFlow,
                        currentSearchedTag = TagInfo(id = tagId, name = tagName, usageCnt = 0),
                        currentTagFavoriteState = initialFavoriteState,
                        viewTagsDataLoaded = true // 데이터 로드 완료
                    )
                }
            } catch (e: Exception) {
                SooumLog.e(TAG, "Failed to load tag cards: ${e.message}")
                // 실패시 요청 상태에서 제거
                _uiState.update { it.copy(requestedTagCards = it.requestedTagCards - tagKey, viewTagsDataLoaded = true) } // 실패 시에도 로드 완료로 처리
                emitEffect(TagUiEffect.ShowNetworkErrorSnackbar { loadTagCards(tagName, tagId) })
            }
        }
    }
    
    fun refreshViewTags(tagName: String, tagId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isRefreshing = true) }
            try {
                // 새로운 Paging flow 생성
                val cardsPagingFlow = tagOrchestrator.tagCards(tagId).cachedIn(viewModelScope)
                
                _uiState.update {
                    it.copy(cardDataItems = cardsPagingFlow)
                }
                
                // 약간의 지연 후 새로고침 상태 해제 (Paging 데이터 로드 시간 고려)
                kotlinx.coroutines.delay(500)
                _uiState.update { it.copy(isRefreshing = false, viewTagsDataLoaded = true) }
                
                SooumLog.d(TAG, "Successfully refreshed tag cards for $tagName")
            } catch (e: Exception) {
                SooumLog.e(TAG, "Failed to refresh tag cards: ${e.message}")
                _uiState.update { it.copy(isRefreshing = false, viewTagsDataLoaded = false) }
                emitEffect(TagUiEffect.ShowNetworkErrorSnackbar { refreshViewTags(tagName, tagId) })
            }
        }
    }

    private fun tagRank() {
        viewModelScope.launch(Dispatchers.IO) {
            tagOrchestrator.tagRank().fold(
                onSuccess = { tags ->
                    _uiState.update { state ->
                        state.copy(
                            tagRank = UiState.Success(data = tags),
                            isRefreshing = false
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update { state ->
                        state.copy(
                            tagRank = UiState.Fail(
                                errorMessage = Result.failure<List<TagInfo>>(throwable).errorMessage()
                            ),
                            isRefreshing = false
                        )
                    }
                }
            )
        }
    }

    fun navigateToDetail(cardId: Long) {
        if (_uiState.value.checkCardDelete is UiState.Loading) return

        viewModelScope.launch {
            _uiState.update { state -> state.copy(checkCardDelete = UiState.Loading) }
            tagOrchestrator.checkCardDeleted(cardId = cardId).fold(
                onSuccess = { isDeleted ->
                    if (isDeleted) {
                        // 삭제된 경우 ID를 전달
                        _uiState.update { state ->
                            state.copy(checkCardDelete = UiState.Success(cardId))
                        }
                    } else {
                        // 삭제되지 않음
                        _uiState.update { state -> state.copy(checkCardDelete = UiState.None) }
                        emitEffect(
                            TagUiEffect.NavigateToDetail(
                                CardDetailArgs(cardId, previousView = CardDetailTrace.PROFILE)
                            )
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update { state ->
                        state.copy(
                            checkCardDelete = UiState.Fail(
                                Result.failure<Boolean>(throwable).errorMessage()
                            )
                        )
                    }
                }
            )
        }
    }

    fun removeDeletedCard(cardId: Long) {
        _uiState.update { state ->
            state.copy(
                deletedCardIds = state.deletedCardIds + cardId,
                checkCardDelete = UiState.None
            )
        }
    }
}

sealed interface TagUiEffect {
    data object NavigationSearchScreen : TagUiEffect
    data class ShowAddFavoriteTagToast(val tagName: String) : TagUiEffect
    data class ShowRemoveFavoriteTagToast(val tagName: String) : TagUiEffect
    data class NavigateToViewTags(val tagName: String, val tagId: Long) : TagUiEffect
    data class ShowNetworkErrorSnackbar(val retryAction: () -> Unit) : TagUiEffect
    data class NavigateToDetail(val cardDetailArgs: CardDetailArgs) : TagUiEffect
}

private const val TAG = "TagViewModel"
