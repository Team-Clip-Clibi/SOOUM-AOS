package com.phew.sooum.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.phew.core_common.log.SooumLog
import com.phew.sooum.MainActivity
import com.phew.sooum.R

class SooumFirebaseMessagingService : FirebaseMessagingService() {
    
    private val notificationChannelManager by lazy {
        NotificationChannelManager(applicationContext)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        SooumLog.d(TAG, "FCM 메시지 수신: ${remoteMessage.from}")

        // 데이터 페이로드 확인
        if (remoteMessage.data.isNotEmpty()) {
            SooumLog.d(TAG, "메시지 데이터: ${remoteMessage.data}")
        }

        // 알림 표시
        remoteMessage.notification?.let {
            sendNotification(it.title, it.body, remoteMessage.data)
        }
    }

    override fun onNewToken(token: String) {
        SooumLog.d(TAG, "새로운 FCM 토큰: $token")

        sendTokenToServer(token)
    }

    private fun sendNotification(title: String?, messageBody: String?, data: Map<String, String>) {
        val intent = createDeepLinkIntent(data)
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // 알림 채널 생성
        notificationChannelManager.createNotificationChannels()
        
        // 알림 타입에 따른 채널 ID 선택
        val notificationType = data["notification_type"] ?: "general"
        val channelId = notificationChannelManager.getChannelIdByType(notificationType)
        
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title ?: "SOOUM")
            .setContentText(messageBody ?: "새로운 알림이 있습니다.")
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = System.currentTimeMillis().toInt() // 고유한 ID 생성
        notificationManager.notify(notificationId, notificationBuilder.build())
        
        SooumLog.d(TAG, "알림 표시 완료 - 타입: $notificationType, 채널: $channelId")
    }

    private fun createDeepLinkIntent(data: Map<String, String>): Intent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            
            // 딥링크 정보를 인텐트에 추가
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
            
            // TODO : 딥링크 타입 확인
            when (data["type"]) {
                "card_detail" -> {
                    data["card_id"]?.let { cardId ->
                        putExtra("deep_link", "sooum://detail/$cardId")
                    }
                }
                "profile" -> {
                    data["user_id"]?.let { userId ->
                        putExtra("deep_link", "sooum://profile/$userId")
                    }
                }
                "notification" -> {
                    putExtra("deep_link", "sooum://notify")
                }
                "feed" -> {
                    putExtra("deep_link", "sooum://feed")
                }
                else -> {
                    // 기본적으로 홈으로 이동
                    putExtra("deep_link", "sooum://feed")
                }
            }
        }
        
        return intent
    }

    private fun sendTokenToServer(token: String) {
        // 로그만 출력
        SooumLog.i(TAG, "FCM 토큰을 서버에 전송해야 함: $token")
    }

    companion object {
        private const val TAG = "SooumFCMService"
    }
}