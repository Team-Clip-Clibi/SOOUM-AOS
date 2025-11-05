package com.phew.presentation.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phew.presentation.settings.model.setting.SettingNavigationEvent
import com.phew.presentation.settings.model.setting.SettingItem
import com.phew.presentation.settings.model.setting.SettingItemId
import com.phew.presentation.settings.model.setting.SettingItemType
import com.phew.presentation.settings.model.setting.SettingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    // TODO: Inject repositories when needed
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingUiState(
            settingItems = createSettingItems()
        )
    )
    val uiState: StateFlow<SettingUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<SettingNavigationEvent>()
    val navigationEvent: SharedFlow<SettingNavigationEvent> = _navigationEvent.asSharedFlow()

    private fun createSettingItems(): List<SettingItem> {
        return listOf(
            SettingItem(
                id = SettingItemId.LOGIN_OTHER_DEVICE,
                title = "", // Will be set from strings in UI
                type = SettingItemType.NAVIGATION
            ),
            SettingItem(
                id = SettingItemId.LOAD_PREVIOUS_ACCOUNT,
                title = "", // Will be set from strings in UI
                type = SettingItemType.NAVIGATION
            ),
            SettingItem(
                id = SettingItemId.BLOCKED_USERS,
                title = "", // Will be set from strings in UI
                type = SettingItemType.NAVIGATION
            ),
            SettingItem(
                id = SettingItemId.NOTICE,
                title = "", // Will be set from strings in UI
                type = SettingItemType.NAVIGATION
            ),
            SettingItem(
                id = SettingItemId.INQUIRY,
                title = "", // Will be set from strings in UI
                type = SettingItemType.NAVIGATION
            ),
            SettingItem(
                id = SettingItemId.PRIVACY_POLICY,
                title = "", // Will be set from strings in UI
                type = SettingItemType.NAVIGATION
            ),
            SettingItem(
                id = SettingItemId.APP_UPDATE,
                title = "", // Will be set from strings in UI
                type = SettingItemType.INFO
            ),
            SettingItem(
                id = SettingItemId.ACCOUNT_DELETION,
                title = "", // Will be set from strings in UI
                type = SettingItemType.DANGER
            )
        )
    }

    fun toggleNotification(enabled: Boolean) {
        _uiState.update { it.copy(notificationEnabled = enabled) }
        // TODO: Save to preferences or call repository
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // TODO: Implement update check logic
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isUpdateAvailable = false
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onLoginOtherDeviceClick() {
        viewModelScope.launch {
            _navigationEvent.emit(SettingNavigationEvent.NavigateToLoginOtherDevice)
        }
    }

    fun onLoadPreviousAccountClick() {
        viewModelScope.launch {
            _navigationEvent.emit(SettingNavigationEvent.NavigateToLoadPreviousAccount)
        }
    }

    fun onBlockedUsersClick() {
        viewModelScope.launch {
            _navigationEvent.emit(SettingNavigationEvent.NavigateToBlockedUsers)
        }
    }

    fun onNoticeClick() {
        viewModelScope.launch {
            _navigationEvent.emit(SettingNavigationEvent.NavigateToNotice)
        }
    }

    fun onInquiryClick() {
        viewModelScope.launch {
            _navigationEvent.emit(SettingNavigationEvent.NavigateToInquiry)
        }
    }

    fun onPrivacyPolicyClick() {
        viewModelScope.launch {
            _navigationEvent.emit(SettingNavigationEvent.NavigateToPrivacyPolicy)
        }
    }

    fun onAccountDeletionClick() {
        viewModelScope.launch {
            _navigationEvent.emit(SettingNavigationEvent.NavigateToAccountDeletion)
        }
    }
}