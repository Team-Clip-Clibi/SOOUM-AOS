package com.phew.core_design.component.toast

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log


internal class SooumToastLifecycleCallbacks: Application.ActivityLifecycleCallbacks,
    SooumToastContextProvider {

    private var context: Context? = null
    private var destroyCallback: (()->Unit)? = null

    override fun setDestroyCallback(callback: () -> Unit) {
        destroyCallback = callback
    }

    override fun getCurrentActiveContext(): Context? {
        return context
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (SooumToastConstants.DEBUG_ENABLED) Log.v(TAG, "onActivityCreated() $activity")
    }

    override fun onActivityStarted(activity: Activity) {
        if (SooumToastConstants.DEBUG_ENABLED) Log.v(TAG, "onActivityStarted() $activity")
    }

    override fun onActivityResumed(activity: Activity) {
        if (SooumToastConstants.DEBUG_ENABLED) Log.v(TAG, "onActivityResumed() $activity")

        context = activity
    }

    override fun onActivityPaused(activity: Activity) {
        if (SooumToastConstants.DEBUG_ENABLED) Log.v(TAG, "onActivityPaused() $activity")

        try {
            destroyCallback?.let {
                if (SooumToastConstants.DEBUG_ENABLED) Log.v(TAG, "destroyCallback invoke")
                it.invoke()
            }
        } catch (e: Exception) {
            if (SooumToastConstants.DEBUG_ENABLED) {
                e.printStackTrace()
            }
        }

        context = null
    }

    override fun onActivityStopped(activity: Activity) {
        if (SooumToastConstants.DEBUG_ENABLED) Log.v(TAG, "onActivityStopped() $activity")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        if (SooumToastConstants.DEBUG_ENABLED) Log.v(TAG, "onActivitySaveInstanceState() $activity")
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (SooumToastConstants.DEBUG_ENABLED) Log.v(TAG, "onActivityDestroyed() $activity")
    }

    companion object {
        private const val TAG = "SooumToastLifecycleCallbacks"
    }
}
