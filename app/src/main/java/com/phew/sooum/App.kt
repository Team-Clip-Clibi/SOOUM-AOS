package com.phew.sooum

import android.app.Application
import com.phew.sooum.fcm.NotificationChannelManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 알림 채널 생성 - Application 컨텍스트에서 직접 생성
        val notificationChannelManager = NotificationChannelManager(this)
        notificationChannelManager.createNotificationChannels()
    }
}