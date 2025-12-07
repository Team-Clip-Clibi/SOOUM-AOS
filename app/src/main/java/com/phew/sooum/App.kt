package com.phew.sooum

import android.app.Application
import com.phew.sooum.fcm.NotificationChannelManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var notificationChannelManager: NotificationChannelManager
    
    override fun onCreate() {
        super.onCreate()
        
        // Hilt로 주입받은 채널 매니저 사용
        notificationChannelManager.createNotificationChannels()
    }
}
