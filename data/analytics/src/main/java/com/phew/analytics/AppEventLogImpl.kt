package com.phew.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AppEventLogImpl @Inject constructor(@ApplicationContext private val context: Context) :
    AppEventLog {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    override fun logEvent(eventName: String, params: Map<String, Any>?) {
        val bundle = Bundle()
        params?.forEach { (key, value) ->
            when (value) {
                is String -> bundle.putString(key, value)
                is Long -> bundle.putLong(key, value)
                is Int -> bundle.putLong(key, value.toLong())
                is Double -> bundle.putDouble(key, value)
                is Boolean -> bundle.putBoolean(key, value)
                else -> bundle.putString(key, value.toString())
            }
        }
        firebaseAnalytics.logEvent(eventName, bundle)
    }

}