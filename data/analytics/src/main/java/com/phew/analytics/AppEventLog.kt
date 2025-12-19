package com.phew.analytics

interface AppEventLog {
    fun logEvent(eventName: String, params: Map<String, Any>? = null)
}