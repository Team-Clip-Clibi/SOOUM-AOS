package com.phew.core_design.component.toast

import android.content.Context

interface SooumToastContextProvider {
    fun getCurrentActiveContext(): Context?
    fun setDestroyCallback(callback: ()->Unit)
}
