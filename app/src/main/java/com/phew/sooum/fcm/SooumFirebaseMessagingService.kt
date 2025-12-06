package com.phew.sooum.fcm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.phew.core_common.log.SooumLog
import com.phew.sooum.MainActivity
import com.phew.sooum.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.core.net.toUri
import com.phew.sooum.session.TransferSuccessHandler

@AndroidEntryPoint
class SooumFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationChannelManager: NotificationChannelManager
    @Inject
    lateinit var transferSuccessHandler: TransferSuccessHandler

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        SooumLog.d(TAG, "FCM 메시지 수신: ${remoteMessage.from}")
        SooumLog.d(TAG, "알림 객체: ${remoteMessage.notification}")
        
        // 데이터 페이로드 확인
        if (remoteMessage.data.isNotEmpty()) {
            SooumLog.d(TAG, "메시지 데이터: ${remoteMessage.data}")
        }

        val notificationType = NotificationType.fromString(remoteMessage.data["notificationType"])

        val isTransferSuccess = notificationType == NotificationType.TRANSFER_SUCCESS
        if (isTransferSuccess) {
            SooumLog.d(TAG, "계정 이전 성공 알림 수신 - 즉시 로그아웃 처리")
            transferSuccessHandler.handleFromService(this)
        }

        // notification 필드가 있는 경우, 시스템이 이미 알림을 표시했을 수 있음
        // 이 경우 추가 알림을 표시하지 않음 (중복 방지)
        if (remoteMessage.notification != null) {
            SooumLog.d(TAG, "시스템 알림이 이미 표시되어 커스텀 알림을 건너뜁니다.")
            return
        }

        // data only 메시지인 경우에만 커스텀 알림 표시
        val title = remoteMessage.data["title"] ?: "SOOUM"
        val body = remoteMessage.data["body"] ?: "새로운 알림이 있습니다."
        
        SooumLog.d(TAG, "커스텀 알림 표시 - 제목: $title, 내용: $body")

        val intent = createDeepLinkIntent(remoteMessage.data)

        val requestCode = REQUEST_CODE
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 커스텀 알림 표시
        sendNotification(title, body, remoteMessage.data, pendingIntent)
    }

    override fun onNewToken(token: String) {
        SooumLog.d(TAG, "새로운 FCM 토큰: $token")

        sendTokenToServer(token)
    }

    private fun sendNotification(title: String, body: String, data: Map<String, String>, pendingIntent: PendingIntent) {
        // 알림 타입에 따라 적절한 채널 ID 가져오기
        val notificationType = data["notificationType"] ?: "general"
        val channelId = notificationChannelManager.getChannelIdByType(notificationType)
        SooumLog.d(TAG, "알림 타입: $notificationType, 채널 ID: $channelId")

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO: 앱에 맞는 알림 아이콘으로 변경
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(REQUEST_CODE, notificationBuilder.build())

    }


    private fun createDeepLinkIntent(data: Map<String, String>): Intent {
        var deepLinkString = "sooum://feed" // 기본값 설정

        val notificationType = data["notificationType"]
        when (notificationType) {
            // [카드] 타입 알림들 - 해당 카드 상세로 이동, 뒤로가기시 Feed Graph
            NotificationType.COMMENT_LIKE.value,
            NotificationType.COMMENT_WRITE.value -> {
                data["targetCardId"]?.let { cardId ->
                    deepLinkString = "sooum://card/$cardId?backTo=feed&view=comment"
                }
            }

            NotificationType.FEED_LIKE.value -> {
                data["targetCardId"]?.let { cardId ->
                    deepLinkString = "sooum://card/$cardId?backTo=feed&view=detail"
                }
            }

                // [태그] 타입 알림 - 해당 태그가 포함된 카드 상세로 이동, 뒤로가기시 태그 Graph
            NotificationType.TAG_USAGE.value -> {
                data["targetCardId"]?.let { cardId ->
                    deepLinkString = "sooum://card/$cardId?backTo=tag"
                }
            }

            // [팔로우] 타입 알림 - 팔로우한 사용자 프로필로 이동, 뒤로가기시 마이 Graph
            NotificationType.FOLLOW.value -> {
                deepLinkString = "sooum://follow?tab=follower"
            }

            NotificationType.TRANSFER_SUCCESS.value -> {
                deepLinkString = TransferSuccessHandler.TRANSFER_SUCCESS_DEEP_LINK
            }
        }

        // 딥링크 문자열을 Uri 객체로 변환
        val deepLinkUri = deepLinkString.toUri()
        SooumLog.d(TAG, "생성된 딥링크 Uri: $deepLinkUri")

        return Intent(Intent.ACTION_VIEW, deepLinkUri, this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP

            // data 페이로드의 나머지 정보를 extras로 전달 (필요 시 앱에서 활용 가능)
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
            putExtra("deep_link", deepLinkString)
        }
    }

    private fun sendTokenToServer(token: String) {
        // 로그만 출력
        SooumLog.i(TAG, "FCM 토큰을 서버에 전송해야 함: $token")
    }

    companion object {
        private const val TAG = "SooumFCMService"
        private const val REQUEST_CODE = 9880
    }
}
