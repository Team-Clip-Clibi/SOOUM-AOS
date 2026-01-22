package com.phew.sooum

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.phew.core.ui.compose.LifecycleAwareComposables
import com.phew.core.ui.util.extension.LocalLifecycleAwareComposables
import com.phew.core.ui.state.rememberSooumAppState
import com.phew.core_common.log.SooumLog
import com.phew.core_design.theme.SooumTheme
import com.phew.sooum.fcm.NotificationType
import com.phew.sooum.navigation.DeepLinkHandler
import com.phew.sooum.session.TransferSuccessHandler
import com.phew.sooum.ui.SooumApp
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.phew.core_common.clarity.ClarityInterface
import com.phew.core.ui.clarity.LocalSessionRecorder

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var lifecycleAwareComposables: LifecycleAwareComposables

    @Inject
    lateinit var deepLinkHandler: DeepLinkHandler

    @Inject
    lateinit var clarityInterface: ClarityInterface
    private var pendingDeepLink by mutableStateOf<String?>(null)

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.dark(
                Color.TRANSPARENT
            )
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = true
        controller.isAppearanceLightNavigationBars = true
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        // 딥링크 처리
        handleIntent(intent)

        setContent {
            val windowSize = calculateWindowSizeClass(this)
            val isExpandedScreen = windowSize.widthSizeClass != WindowWidthSizeClass.Compact
            val appState = rememberSooumAppState()
            val coroutineScope = rememberCoroutineScope()

            // FCM 토큰 로깅 (디버그용)
            LaunchedEffect(Unit) {
                coroutineScope.launch {
                    try {
                        val token = FirebaseMessaging.getInstance().token.await()
                        SooumLog.d("FCM_TOKEN_DEBUG", "==== FCM 토큰 ====")
                        SooumLog.d("FCM_TOKEN_DEBUG", token)
                        SooumLog.d("FCM_TOKEN_DEBUG", "==================")
                        SooumLog.i("FCM_TOKEN", "현재 FCM 토큰: $token")
                    } catch (e: Exception) {
                        SooumLog.w(TAG, "FCM 토큰 가져오기 실패 error=${e.message}")
                    }
                }
            }

            // 딥링크 처리
            LaunchedEffect(pendingDeepLink) {
                pendingDeepLink?.let { deepLink ->
                    deepLinkHandler.handleDeepLink(
                        navController = appState.navController,
                        deepLinkUrl = deepLink,
                        appState = appState
                    )
                    pendingDeepLink = null
                }
            }

            CompositionLocalProvider(
                LocalLifecycleAwareComposables provides lifecycleAwareComposables,
                LocalSessionRecorder provides clarityInterface
            ) {
                SooumTheme {
                    SooumApp(
                        appState = appState,
                        finish = ::finish,
                        appVersionUpdate = ::playStore,
                        // 요기 수정 -> webView 삭제
                        isExpend = isExpandedScreen
                    )
                }
            }
        }
    }

    private fun playStore() {
        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                BuildConfig.PLAY_STORE_LINK.toUri()
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            throw IllegalArgumentException(e)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return

        SooumLog.d(TAG, "Intent 처리 시작: ${intent.action}")

        when (intent.action) {
            Intent.ACTION_VIEW -> {
                // 딥링크 스키마 처리 (sooum://...)
                val data = intent.data
                if (data != null && data.scheme == "sooum") {
                    val deepLink = data.toString()
                    SooumLog.d(TAG, "딥링크 스키마 감지: $deepLink")
                    pendingDeepLink = deepLink
                }
            }

            Intent.ACTION_MAIN -> {
                // FCM 알림에서 전달된 딥링크 처리
                val deepLink = intent.getStringExtra("deep_link")
                if (!deepLink.isNullOrBlank()) {
                    SooumLog.d(TAG, "FCM 딥링크 감지: $deepLink")
                    pendingDeepLink = deepLink
                } else {
                    // 딥링크가 없더라도 FCM 알림 클릭인지 확인
                    val notificationType = intent.getStringExtra("notificationType")
                    if (!notificationType.isNullOrBlank()) {
                        SooumLog.d(TAG, "FCM 알림 클릭 감지, 알림 타입: $notificationType")
                        // 알림 타입에 따른 기본 딥링크 생성
                        pendingDeepLink = when (notificationType) {
                            NotificationType.FEED_LIKE.value -> {
                                val cardId = intent.getStringExtra("targetCardId")
                                if (cardId != null) "sooum://card/$cardId?backTo=feed&view=detail" else "sooum://feed"
                            }

                            NotificationType.COMMENT_LIKE.value,
                            NotificationType.COMMENT_WRITE.value,
                                -> {
                                val cardId = intent.getStringExtra("targetCardId")
                                if (cardId != null) "sooum://card/$cardId?backTo=feed&view=comment" else "sooum://feed"
                            }

                            NotificationType.TAG_USAGE.value -> {
                                val cardId = intent.getStringExtra("targetCardId")
                                if (cardId != null) "sooum://card/$cardId?backTo=tag" else "sooum://feed"
                            }

                            NotificationType.FOLLOW.value -> "sooum://follow?tab=follower"
                            NotificationType.TRANSFER_SUCCESS.value -> TransferSuccessHandler.TRANSFER_SUCCESS_DEEP_LINK
                            else -> "sooum://feed"
                        }
                        SooumLog.d(TAG, "생성된 기본 딥링크: $pendingDeepLink")
                    }
                }
            }
        }

        // 추가 데이터 로깅
        intent.extras?.let { extras ->
            for (key in extras.keySet()) {
                val value = extras.get(key)
                SooumLog.d(TAG, "Intent extra - $key: $value")
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
