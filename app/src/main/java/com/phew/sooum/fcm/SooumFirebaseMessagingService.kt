package com.phew.sooum.fcm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.phew.core_common.log.SooumLog
import com.phew.sooum.MainActivity
import com.phew.sooum.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.core.net.toUri

@AndroidEntryPoint
class SooumFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationChannelManager: NotificationChannelManager

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        SooumLog.d(TAG, "FCM 메시지 수신: ${remoteMessage.from}")
        SooumLog.d(TAG, "알림 객체: ${remoteMessage.notification}")
        
        // 데이터 페이로드 확인
        if (remoteMessage.data.isNotEmpty()) {
            SooumLog.d(TAG, "메시지 데이터: ${remoteMessage.data}")
        }

        // 백그라운드에서도 커스텀 알림을 표시하려면 data만 사용해야 함
        // notification 필드가 있으면 시스템이 자동으로 처리하므로 onMessageReceived가 호출되지 않음
        val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title ?: "SOOUM"
        val body = remoteMessage.data["body"] ?: remoteMessage.notification?.body ?: "새로운 알림이 있습니다."
        
        SooumLog.d(TAG, "알림 제목: $title, 내용: $body")
        // 2. data 페이로드를 기반으로 딥링크 Intent 생성
        val intent = createDeepLinkIntent(remoteMessage.data)

        // 3. 생성된 Intent를 담을 PendingIntent 생성
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(), // requestCode를 매번 고유하게 설정하여 Intent의 extra가 갱신되도록 보장
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE // 일회성, 변경 불가능한 PendingIntent
        )

        // 4. 알림 표시
        sendNotification(title, body, remoteMessage.data, pendingIntent)

        //sendNotification(title, body, remoteMessage.data)
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

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // NotificationCompat.Builder를 사용하여 알림 구성
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO: 앱에 맞는 알림 아이콘으로 변경
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true) // 알림 클릭 시 자동으로 사라지도록 설정
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent) // 알림 클릭 시 실행할 작업
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 중요도 높음 (헤드업 알림으로 표시될 수 있음)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body)) // 긴 텍스트 표시 지원

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 고유한 ID로 알림을 표시 (ID가 같으면 기존 알림이 업데이트됨)
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())

        SooumLog.d(TAG, "알림 표시 완료")
    }

    private fun sendNotification(title: String?, messageBody: String?, data: Map<String, String>) {
        val intent = createDeepLinkIntent(data)
        
        val requestCode = System.currentTimeMillis().toInt()
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

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
        var deepLinkString = "sooum://feed" // 기본값 설정

        val notificationType = data["notificationType"]
        when (notificationType) {
            // [카드] 타입 알림들 - 해당 카드 상세로 이동, 뒤로가기시 Feed Graph
            "FEED_LIKE", "COMMENT_LIKE", "COMMENT_WRITE" -> {
                data["targetCardId"]?.let { cardId ->
                    deepLinkString = "sooum://card/$cardId?backTo=feed"
                }
            }

            // [태그] 타입 알림 - 해당 태그가 포함된 카드 상세로 이동, 뒤로가기시 태그 Graph
            "TAG_USAGE" -> {
                data["targetCardId"]?.let { cardId ->
                    deepLinkString = "sooum://card/$cardId?backTo=tag"
                }
            }

            // [팔로우] 타입 알림 - 팔로우한 사용자 프로필로 이동, 뒤로가기시 마이 Graph
            "FOLLOW" -> {
                data["followingMemberId"]?.let { memberId ->
                    deepLinkString = "sooum://profile/$memberId?backTo=my"
                }
            }

            // [공지사항] 타입 알림들 - 공지사항 상세로 이동, 뒤로가기시 Feed Graph
            "BLOCKED", "DELETED", "TRANSFER_SUCCESS" -> {
                data["notificationId"]?.let { notificationId ->
                    deepLinkString = "sooum://notice/$notificationId?backTo=feed"
                }
            }

            // else: 기본값인 "sooum://feed" 유지
        }

//        val intent = Intent(this, MainActivity::class.java).apply {
//            action = Intent.ACTION_MAIN
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
//                Intent.FLAG_ACTIVITY_CLEAR_TOP or
//                Intent.FLAG_ACTIVITY_SINGLE_TOP
//
//            // 딥링크 정보를 인텐트에 추가 (key-value 형태로)
//            data.forEach { (key, value) ->
//                putExtra(key, value)
//            }
//            putExtra("deep_link", deepLinkString)
//        }
//        return intent
        // 딥링크 문자열을 Uri 객체로 변환
        val deepLinkUri = deepLinkString.toUri()
        SooumLog.d(TAG, "생성된 딥링크 Uri: $deepLinkUri")

        // Intent 생성 시 ACTION_VIEW와 딥링크 Uri를 설정하는 것이 표준 방식입니다.
        return Intent(Intent.ACTION_VIEW, deepLinkUri, this, MainActivity::class.java).apply {
            // 앱이 실행 중이 아닐 때, 새로운 태스크에서 Activity를 시작하도록 설정
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            // data 페이로드의 나머지 정보를 extras로 전달 (필요 시 앱에서 활용 가능)
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }
    }

    private fun sendTokenToServer(token: String) {
        // 로그만 출력
        SooumLog.i(TAG, "FCM 토큰을 서버에 전송해야 함: $token")
    }

    companion object {
        private const val TAG = "SooumFCMService"
    }
}
