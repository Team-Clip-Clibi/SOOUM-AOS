package com.phew.presentation.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.phew.core_common.DomainResult
import com.phew.domain.model.BlockMember
import com.phew.domain.usecase.GetBlockUserPaging
import com.phew.domain.usecase.GetRefreshToken
import com.phew.domain.usecase.UnblockMember
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlockUserManagementViewModel @Inject constructor(
    getBlockUserPaging: GetBlockUserPaging,
    private val unblockMember: UnblockMember,
    private val getRefreshToken: GetRefreshToken
) : ViewModel() {

    val blockUsers: Flow<PagingData<BlockMember>> =
        getBlockUserPaging().cachedIn(viewModelScope)

    private val _uiState = MutableStateFlow(BlockUserManagementUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<BlockUserManagementUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()


    fun showUnblockDialog(blockMember: BlockMember) {
        _uiState.update {
            it.copy(
                showUnblockDialog = true,
                selectedBlockMember = blockMember
            )
        }
    }

    fun hideUnblockDialog() {
        _uiState.update {
            it.copy(
                showUnblockDialog = false,
                selectedBlockMember = null
            )
        }
    }

    fun unblockUser() {
        val blockMember = _uiState.value.selectedBlockMember ?: return

        viewModelScope.launch {
            when (
                val result = unblockMember(
                    UnblockMember.Param(toMemberId = blockMember.blockMemberId)
                )
            ) {
                is DomainResult.Success -> {
                    _uiState.update {
                        it.copy(
                            showUnblockDialog = false,
                            selectedBlockMember = null
                        )
                    }
                    _uiEffect.emit(BlockUserManagementUiEffect.ShowUnblockSuccess)
                    _uiEffect.emit(BlockUserManagementUiEffect.RefreshBlockList)
                }

                is DomainResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            showUnblockDialog = false,
                            selectedBlockMember = null
                        )
                    }
                    val refreshToken = getRefreshToken()
                    _uiEffect.emit(
                        BlockUserManagementUiEffect.ShowError(
                            message = result.error ?: "차단 해제에 실패했습니다.",
                            refreshToken = refreshToken
                        )
                    )
                }
            }
        }
    }
}

data class BlockUserManagementUiState(
    val showUnblockDialog: Boolean = false,
    val selectedBlockMember: BlockMember? = null
)

sealed class BlockUserManagementUiEffect {
    object ShowUnblockSuccess : BlockUserManagementUiEffect()
    data class ShowError(val message: String, val refreshToken: String) : BlockUserManagementUiEffect()
    object RefreshBlockList : BlockUserManagementUiEffect()
}
