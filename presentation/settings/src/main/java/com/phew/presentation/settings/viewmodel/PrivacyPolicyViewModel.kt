package com.phew.presentation.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phew.core.ui.model.navigation.WebViewUrlArgs
import com.phew.presentation.settings.R
import com.phew.presentation.settings.model.privacy.PrivacyPolicyItem
import com.phew.presentation.settings.model.privacy.PrivacyPolicyItemId
import com.phew.presentation.settings.model.setting.SettingItemType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrivacyPolicyViewModel @Inject constructor() : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<PrivacyPolicyNavigationEvent>()
    val navigationEvent: SharedFlow<PrivacyPolicyNavigationEvent> = _navigationEvent.asSharedFlow()

    fun getPrivacyPolicyItems(): List<PrivacyPolicyItem> {
        return PrivacyPolicyType.entries.sortedBy { it.id }.map { type ->
            PrivacyPolicyItem(
                id = PrivacyPolicyItemId.valueOf("PRIVACY_POLICY_${type.name}"),
                titleResId = getTitleResIdForType(type),
                type = SettingItemType.NAVIGATION
            )
        }
    }
    
    fun onPrivacyPolicyItemClick(type: PrivacyPolicyType) {
        viewModelScope.launch {
            _navigationEvent.emit(
                PrivacyPolicyNavigationEvent.NavigateToWebView(
                    WebViewUrlArgs(url = type.url)
                )
            )
        }
    }
    
    private fun getTitleResIdForType(type: PrivacyPolicyType): Int {
        return when (type) {
            PrivacyPolicyType.PERSONAL_INFO -> R.string.privacy_policy_person_info
            PrivacyPolicyType.TERMS_OF_SERVICE -> R.string.privacy_policy_service
            PrivacyPolicyType.LOCATION_INFO -> R.string.privacy_policy_location
        }
    }
}

enum class PrivacyPolicyType(val id: String, val url: String) {
    // 개인 정보 처리 방침
    PERSONAL_INFO("1", "https://www.notion.so/26b2142ccaa38059a1dbf3e6b6b6b4e6"),
    // 서비스 이용 약관
    TERMS_OF_SERVICE("2", "https://www.notion.so/26b2142ccaa38076b491df099cd7b559"),
    // 위치정보 이용 약관
    LOCATION_INFO("3", "https://www.notion.so/26b2142ccaa380f1bfafe99f5f8a10f1")
}

sealed class PrivacyPolicyNavigationEvent {
    data class NavigateToWebView(val args: WebViewUrlArgs) : PrivacyPolicyNavigationEvent()
}