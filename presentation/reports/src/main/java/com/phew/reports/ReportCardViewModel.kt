package com.phew.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phew.core_common.DomainResult
import com.phew.domain.dto.ReportReason
import com.phew.domain.usecase.ReportsCards
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportCardViewModel @Inject constructor(private val report: ReportsCards) : ViewModel() {
    private var _uiState = MutableStateFlow(ReportState())
    val uiState: StateFlow<ReportState> = _uiState.asStateFlow()

    fun reportCard(cardId: String) {
        _uiState.update { state -> state.copy(reportCard = UiState.Loading) }
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = report(
                ReportsCards.Param(
                    cardId = cardId,
                    reason = _uiState.value.reportReason
                )
            )) {
                is DomainResult.Failure -> {
                    _uiState.update { state -> state.copy(reportCard = UiState.Fail(result.error)) }
                }

                is DomainResult.Success -> {
                    _uiState.update { state -> state.copy(reportCard = UiState.Success(Unit)) }
                }
            }
        }
    }

    fun setReportReason(data: ReportReason) {
        _uiState.update { state ->
            state.copy(reportReason = data)
        }
    }


    data class ReportState(
        val reportCard: UiState<Unit> = UiState.None,
        val reportReason: ReportReason = ReportReason.NONE,
    )

    sealed interface UiState<out T> {
        data object None : UiState<Nothing>
        data object Loading : UiState<Nothing>
        data class Success<T>(val data: T) : UiState<T>
        data class Fail(val errorMessage: String) : UiState<Nothing>
    }
}