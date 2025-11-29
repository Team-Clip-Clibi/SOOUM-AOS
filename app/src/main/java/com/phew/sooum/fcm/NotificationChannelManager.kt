package com.phew.sooum.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.phew.core_common.log.SooumLog
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationChannelManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createGeneralNotificationChannel()
            createCommentNotificationChannel()
            createLikeNotificationChannel()
            createFollowNotificationChannel()
            SooumLog.d(TAG, "알림 채널 생성 완료")
        }
    }
    
    private fun createChannel(
        channelId: String,
        channelName: String,
        importance: Int,
        description: String,
        vibrationPattern: LongArray,
        lightColor: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                importance
            ).apply {
                this.description = description
                enableVibration(true)
                this.vibrationPattern = vibrationPattern
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
                this.lightColor = lightColor
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createGeneralNotificationChannel() {
        createChannel(
            channelId = GENERAL_CHANNEL_ID,
            channelName = GENERAL_CHANNEL_NAME,
            importance = NotificationManager.IMPORTANCE_HIGH,
            description = "일반 알림",
            vibrationPattern = longArrayOf(0, 1000, 500, 1000),
            lightColor = ContextCompat.getColor(context, android.R.color.holo_blue_bright)
        )
    }
    
    private fun createCommentNotificationChannel() {
        createChannel(
            channelId = COMMENT_CHANNEL_ID,
            channelName = COMMENT_CHANNEL_NAME,
            importance = NotificationManager.IMPORTANCE_HIGH,
            description = "댓글 알림",
            vibrationPattern = longArrayOf(0, 500, 200, 500),
            lightColor = ContextCompat.getColor(context, android.R.color.holo_green_light)
        )
    }
    
    private fun createLikeNotificationChannel() {
        createChannel(
            channelId = LIKE_CHANNEL_ID,
            channelName = LIKE_CHANNEL_NAME,
            importance = NotificationManager.IMPORTANCE_DEFAULT,
            description = "좋아요 알림",
            vibrationPattern = longArrayOf(0, 300),
            lightColor = ContextCompat.getColor(context, android.R.color.holo_red_light)
        )
    }
    
    private fun createFollowNotificationChannel() {
        createChannel(
            channelId = FOLLOW_CHANNEL_ID,
            channelName = FOLLOW_CHANNEL_NAME,
            importance = NotificationManager.IMPORTANCE_DEFAULT,
            description = "팔로우 알림",
            vibrationPattern = longArrayOf(0, 200, 100, 200),
            lightColor = ContextCompat.getColor(context, android.R.color.holo_orange_light)
        )
    }
    
    fun getChannelIdByType(notificationType: String): String {
        return when (notificationType) {
            "comment", "reply" -> COMMENT_CHANNEL_ID
            "like", "heart" -> LIKE_CHANNEL_ID
            "follow", "following" -> FOLLOW_CHANNEL_ID
            else -> GENERAL_CHANNEL_ID
        }
    }
    
    fun getChannelNameByType(notificationType: String): String {
        return when (notificationType) {
            "comment", "reply" -> COMMENT_CHANNEL_NAME
            "like", "heart" -> LIKE_CHANNEL_NAME
            "follow", "following" -> FOLLOW_CHANNEL_NAME
            else -> GENERAL_CHANNEL_NAME
        }
    }
    
    companion object {
        private const val TAG = "NotificationChannelManager"
        
        const val GENERAL_CHANNEL_ID = "sooum_general_notification"
        const val COMMENT_CHANNEL_ID = "sooum_comment_notification"
        const val LIKE_CHANNEL_ID = "sooum_like_notification"
        const val FOLLOW_CHANNEL_ID = "sooum_follow_notification"
        
        private const val GENERAL_CHANNEL_NAME = "일반 알림"
        private const val COMMENT_CHANNEL_NAME = "댓글 알림"
        private const val LIKE_CHANNEL_NAME = "좋아요 알림"
        private const val FOLLOW_CHANNEL_NAME = "팔로우 알림"
    }
}