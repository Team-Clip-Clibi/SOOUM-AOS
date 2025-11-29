package com.phew.sooum.fcm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.phew.core_common.log.SooumLog
import com.phew.sooum.MainActivity
import com.phew.sooum.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SooumFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationChannelManager: NotificationChannelManager

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        SooumLog.d(TAG, "FCM 메시지 수신: ${remoteMessage.from}")

        // 데이터 페이로드 확인
        if (remoteMessage.data.isNotEmpty()) {
            SooumLog.d(TAG, "메시지 데이터: ${remoteMessage.data}")
        }

        // 알림 표시
        val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title
        val body = remoteMessage.data["body"] ?: remoteMessage.notification?.body
        sendNotification(title, body, remoteMessage.data)
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

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = System.currentTimeMillis().toInt() // 고유한 ID 생성
        notificationManager.notify(notificationId, notificationBuilder.build())

        SooumLog.d(TAG, "알림 표시 완료 - 타입: $notificationType, 채널: $channelId")
    }

    private fun createDeepLinkIntent(data: Map<String, String>): Intent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            // 딥링크 정보를 인텐트에 추가
            data.forEach { (key, value) ->
                putExtra(key, value)
            }

            // 알림 타입별 딥링크 처리
            val notificationType = NotificationType.fromString(data["notificationType"])
            when (notificationType) {
                // [카드] 타입 알림들 - 해당 카드 상세로 이동, 뒤로가기시 Feed Graph
                NotificationType.FEED_LIKE, 
                NotificationType.COMMENT_LIKE, 
                NotificationType.COMMENT_WRITE -> {
                    data["targetCardId"]?.let { cardId ->
                        putExtra("deep_link", "sooum://card/$cardId?backTo=feed")
                    }
                }

                // [태그] 타입 알림 - 해당 태그가 포함된 카드 상세로 이동, 뒤로가기시 태그 Graph
                NotificationType.TAG_USAGE -> {
                    data["targetCardId"]?.let { cardId ->
                        putExtra("deep_link", "sooum://card/$cardId?backTo=tag")
                    }
                }

                // [팔로우] 타입 알림 - 팔로우 리스트 페이지로 이동, 뒤로가기시 마이 Graph
                NotificationType.FOLLOW -> {
                    putExtra("deep_link", "sooum://follow?backTo=my")
                }

                // [공지사항] 타입 알림들 - 공지사항 상세로 이동, 뒤로가기시 Feed Graph
                NotificationType.BLOCKED, 
                NotificationType.DELETED, 
                NotificationType.TRANSFER_SUCCESS -> {
                    data["notificationId"]?.let { notificationId ->
                        putExtra("deep_link", "sooum://notice/$notificationId?backTo=feed")
                    }
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