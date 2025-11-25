package com.phew.domain.interceptor

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlobalEventBus @Inject constructor() {
    private val _events = MutableSharedFlow<GlobalEvent>()
    val events = _events.asSharedFlow()

    suspend fun emitEvent(event: GlobalEvent) {
        _events.emit(event)
    }
}

sealed interface GlobalEvent {
    data object TeapotEvent : GlobalEvent
}