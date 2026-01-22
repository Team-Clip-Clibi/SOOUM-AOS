package com.phew.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phew.domain.interceptor.GlobalEvent
import com.phew.domain.interceptor.GlobalEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val globalEventBus: GlobalEventBus) : ViewModel() {
    private val _globalEvent = Channel<GlobalEvent>()
    val globalEvent = _globalEvent.receiveAsFlow()

    init {
        observerGlobalEvents()
    }

    private fun observerGlobalEvents() {
        viewModelScope.launch {
            globalEventBus.events.collect { event ->
                _globalEvent.send(event)
            }
        }
    }
}