package com.phew.presentation.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phew.core_common.DomainResult
import com.phew.core_common.DataResult
import com.phew.core_common.TimeUtils
import com.phew.domain.model.AppVersionStatusType
import com.phew.domain.usecase.CheckAppVersionNew
import com.phew.domain.usecase.GetActivityRestrictionDate
import com.phew.domain.usecase.GetRefreshToken
import com.phew.domain.usecase.GetRejoinableDate
import com.phew.domain.usecase.ToggleNotification
import com.phew.presentation.settings.model.setting.SettingNavigationEvent
import com.phew.presentation.settings.model.setting.SettingItem
import com.phew.presentation.settings.model.setting.SettingItemId
import com.phew.presentation.settings.model.setting.SettingItemType
import com.phew.presentation.settings.model.setting.SettingUiState
import com.phew.presentation.settings.model.setting.ToastEvent
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
    private val getActivityRestrictionDate: GetActivityRestrictionDate,
    private val checkAppVersionNew: CheckAppVersionNew,
    private val getRejoinableDate: GetRejoinableDate,
    private val getRefreshToken: GetRefreshToken,
    private val toggleNotification: ToggleNotification
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingUiState(
            settingItems = createSettingItems()
        )
    )
    val uiState: StateFlow<SettingUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<SettingNavigationEvent>()
    val navigationEvent: SharedFlow<SettingNavigationEvent> = _navigationEvent.asSharedFlow()

    private val _toastEvent = MutableSharedFlow<ToastEvent>()
    val toastEvent: SharedFlow<ToastEvent> = _toastEvent.asSharedFlow()
    
    init {
        loadActivityRestrictionDate()
        checkAppVersion()
        loadRejoinableDate()
    }

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

    fun onNotificationToggle(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = toggleNotification()) {
                is DataResult.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            notificationEnabled = result.data.isAllowNotify
                        ) 
                    }
                }
                is DataResult.Fail -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _toastEvent.emit(ToastEvent.ShowNotificationToggleErrorToast)
                }
            }
        }
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val param = CheckAppVersionNew.Param(
                type = "ANDROID"
            )
            
            when (val result = checkAppVersionNew(param)) {
                is DomainResult.Success -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            appVersionStatus = result.data,
                            appVersionStatusType = result.data?.status,
                            latestVersion = result.data?.latestVersion
                        )
                    }
                }
                is DomainResult.Failure -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
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
            val refreshToken = getRefreshToken()
            _navigationEvent.emit(
                SettingNavigationEvent.SendInquiryMail(refreshToken = refreshToken)
            )
        }
    }

    fun onPrivacyPolicyClick() {
        viewModelScope.launch {
            _navigationEvent.emit(SettingNavigationEvent.NavigateToPrivacyPolicy)
        }
    }

    fun onAccountDeletionClick() {
        _uiState.update { it.copy(showWithdrawalDialog = true) }
    }
    
    fun onDismissWithdrawalDialog() {
        _uiState.update { it.copy(showWithdrawalDialog = false) }
    }
    
    fun onConfirmWithdrawal() {
        _uiState.update { it.copy(showWithdrawalDialog = false) }
        viewModelScope.launch {
            _navigationEvent.emit(SettingNavigationEvent.NavigateToAccountDeletion)
        }
    }

    fun onAppUpdateClick() {
        viewModelScope.launch {
            val currentState = _uiState.value
            when (currentState.appVersionStatusType) {
                AppVersionStatusType.UPDATE -> {
                    _navigationEvent.emit(SettingNavigationEvent.NavigateToAppStore)
                }
                AppVersionStatusType.OK, AppVersionStatusType.PENDING -> {
                    _toastEvent.emit(ToastEvent.ShowCurrentVersionToast)
                }
                null -> {
                    // 상태가 없는 경우 아무 동작 하지 않음
                }
            }
        }
    }
    
    private fun loadActivityRestrictionDate() {
        viewModelScope.launch {
            val result = getActivityRestrictionDate()
            if (result is DomainResult.Success) {
                _uiState.update { 
                    it.copy(activityRestrictionDate = result.data?.let { dateString -> 
                        TimeUtils.formatToKoreanDateTime(dateString) 
                    }) 
                }
            }
        }
    }
    
    private fun checkAppVersion() {
        viewModelScope.launch {
            val param = CheckAppVersionNew.Param(
                type = "ANDROID"
            )
            
            when (val result = checkAppVersionNew(param)) {
                is DomainResult.Success -> {
                    _uiState.update { 
                        it.copy(
                            appVersionStatus = result.data,
                            appVersionStatusType = result.data?.status,
                            latestVersion = result.data?.latestVersion
                        )
                    }
                }
                is DomainResult.Failure -> {
                    // 실패시에는 null로 유지 (기본값)
                }
            }
        }
    }
    
    private fun loadRejoinableDate() {
        viewModelScope.launch {
            val result = getRejoinableDate()
            result.onSuccess { rejoinableDate ->
                _uiState.update { it.copy(rejoinableDate = rejoinableDate) }
            }.onFailure {
                // 실패시 처리 (필요시)
            }
        }
    }
}
