package com.phew.sooum.fcm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.phew.core_common.log.SooumLog
import com.phew.sooum.MainActivity
import com.phew.sooum.R
import com.phew.sooum.session.TransferSuccessHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

// Coil 3.x
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.transformations
import coil3.transform.RoundedCornersTransformation
import coil3.toBitmap

@AndroidEntryPoint
class SooumFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationChannelManager: NotificationChannelManager

    @Inject
    lateinit var transferSuccessHandler: TransferSuccessHandler

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        SooumLog.d(TAG, "FCM 메시지 수신: ${remoteMessage.from}")
        val data = remoteMessage.data
        val notificationTypeStr = data["notificationType"] ?: "general"
        val notificationType = NotificationType.fromString(notificationTypeStr)
        val notificationId = System.currentTimeMillis().toInt()
        if (notificationType == NotificationType.TRANSFER_SUCCESS) {
            transferSuccessHandler.handleFromService(this)
        }
        val title = remoteMessage.data["title"] ?: "SOOUM"
        val body = remoteMessage.data["body"] ?: "새로운 알림이 있습니다."
        val imageUrl = data["imageUrl"] ?: ""
        var largeIconBitmap: Bitmap? = null
        val isImageTargetType = notificationType == NotificationType.ARTICLE_CARD_UPLOAD ||
                notificationType == NotificationType.FOLLOW_CARD_UPLOAD
        if (isImageTargetType && imageUrl.isNotEmpty() && imageUrl != "null") {
            largeIconBitmap = runBlocking {
                withTimeoutOrNull(5000L) {
                    try {
                        val request = ImageRequest.Builder(this@SooumFirebaseMessagingService)
                            .data(imageUrl)
                            .transformations(RoundedCornersTransformation(24f))
                            .build()
                        val result = applicationContext.imageLoader.execute(request)
                        result.image?.toBitmap()
                    } catch (e: Exception) {
                        SooumLog.e(TAG, "이미지 로드 실패: ${e.message}")
                        null
                    }
                }
            }
        }
        val intent = createDeepLinkIntent(data)
        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        sendNotification(title, body, data, pendingIntent, largeIconBitmap, notificationId)
    }

    private fun sendNotification(
        title: String,
        body: String,
        data: Map<String, String>,
        pendingIntent: PendingIntent,
        largeIconBitmap: Bitmap?,
        notificationId: Int
    ) {
        val notificationTypeStr = data["notificationType"] ?: "general"
        val channelId = notificationChannelManager.getChannelIdByType(notificationTypeStr)
        val groupKey = "com.phew.sooum.GROUP_$notificationTypeStr"
        val summaryId = notificationTypeStr.hashCode()
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_sooum_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setGroup(groupKey)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))

        if (largeIconBitmap != null) {
            notificationBuilder.setLargeIcon(largeIconBitmap)
        }
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())
        val summaryNotification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_sooum_logo)
            .setGroup(groupKey)
            .setGroupSummary(true)
            .setAutoCancel(true)
            .setStyle(
                NotificationCompat.InboxStyle()
                    .setSummaryText(getGroupSummaryText(notificationTypeStr))
            )
            .build()
        notificationManager.notify(summaryId, summaryNotification)
    }

    private fun getGroupSummaryText(type: String): String {
        return when (type) {
            NotificationType.COMMENT_WRITE.value -> "새로운 댓글 알림"
            NotificationType.FEED_LIKE.value -> "좋아요 알림"
            NotificationType.ARTICLE_CARD_UPLOAD.value,
            NotificationType.FOLLOW_CARD_UPLOAD.value,
                -> "새로운 카드 소식"

            else -> "새로운 알림 소식"
        }
    }

    private fun createDeepLinkIntent(data: Map<String, String>): Intent {
        var deepLinkString = "sooum://feed"
        val typeStr = data["notificationType"]

        when (typeStr) {
            NotificationType.COMMENT_LIKE.value,
            NotificationType.COMMENT_WRITE.value,
            NotificationType.VIEW_FEED_COMMENT_WRITE.value,
            NotificationType.FOLLOW_CARD_UPLOAD.value,
            NotificationType.ARTICLE_CARD_UPLOAD.value -> {
                data["targetCardId"]?.let { cardId ->
                    deepLinkString = "sooum://card/$cardId?backTo=feed&view=comment"
                }
            }

            NotificationType.FEED_LIKE.value -> {
                data["targetCardId"]?.let { cardId ->
                    deepLinkString = "sooum://card/$cardId?backTo=feed&view=detail"
                }
            }

            NotificationType.TAG_USAGE.value -> {
                data["targetCardId"]?.let { cardId ->
                    deepLinkString = "sooum://card/$cardId?backTo=tag"
                }
            }

            NotificationType.FOLLOW.value -> deepLinkString = "sooum://follow?tab=follower"
            NotificationType.TRANSFER_SUCCESS.value -> deepLinkString =
                TransferSuccessHandler.TRANSFER_SUCCESS_DEEP_LINK
        }

        return Intent(
            Intent.ACTION_VIEW,
            deepLinkString.toUri(),
            this,
            MainActivity::class.java
        ).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            data.forEach { (key, value) -> putExtra(key, value) }
            putExtra("deep_link", deepLinkString)
        }
    }

    override fun onNewToken(token: String) {
        SooumLog.d(TAG, "새로운 토큰: $token")
    }

    companion object {
        private const val TAG = "SooumFCMService"
    }
}
