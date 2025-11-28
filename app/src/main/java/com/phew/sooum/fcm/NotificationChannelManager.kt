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
    
    private fun createGeneralNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                GENERAL_CHANNEL_ID,
                GENERAL_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "일반 알림"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
                lightColor = ContextCompat.getColor(context, android.R.color.holo_blue_bright)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createCommentNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                COMMENT_CHANNEL_ID,
                COMMENT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "댓글 알림"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
                lightColor = ContextCompat.getColor(context, android.R.color.holo_green_light)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createLikeNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                LIKE_CHANNEL_ID,
                LIKE_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "좋아요 알림"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
                lightColor = ContextCompat.getColor(context, android.R.color.holo_red_light)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createFollowNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                FOLLOW_CHANNEL_ID,
                FOLLOW_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "팔로우 알림"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 200, 100, 200)
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), null)
                lightColor = ContextCompat.getColor(context, android.R.color.holo_orange_light)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
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