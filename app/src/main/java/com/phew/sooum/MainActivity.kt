package com.phew.sooum

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
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
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.phew.core.ui.compose.LifecycleAwareComposables
import com.phew.core.ui.util.extension.LocalLifecycleAwareComposables
import com.phew.core.ui.state.rememberSooumAppState
import com.phew.core_common.log.SooumLog
import com.phew.core_design.theme.SooumTheme
import com.phew.sooum.debug.FCMTokenLogger
import com.phew.sooum.navigation.DeepLinkHandler
import com.phew.sooum.ui.SooumApp
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var lifecycleAwareComposables: LifecycleAwareComposables
    
    private var pendingDeepLink by mutableStateOf<String?>(null)

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
                        FirebaseMessaging.getInstance().token
                            .addOnCompleteListener { task ->
                                if (!task.isSuccessful) {
                                    SooumLog.w(TAG,"FCM_TOKEN_DEBUG 토큰 가져오기 실패 ${task.exception}")
                                    return@addOnCompleteListener
                                }

                                // FCM 토큰 가져오기
                                val token = task.result
                                SooumLog.d("FCM_TOKEN_DEBUG", "==== FCM 토큰 ====")
                                SooumLog.d("FCM_TOKEN_DEBUG", token)
                                SooumLog.d("FCM_TOKEN_DEBUG", "==================")
                                SooumLog.i("FCM_TOKEN", "현재 FCM 토큰: $token")
                                
                                SooumLog.d("TOKEN",token)
                            }
                    } catch (e: Exception) {
                        SooumLog.e("FCM_TOKEN", "토큰 가져오기 실패: ${e.message}")
                    }
                }
            }
            
            // 딥링크 처리
            LaunchedEffect(pendingDeepLink) {
                pendingDeepLink?.let { deepLink ->
                    DeepLinkHandler.handleDeepLink(appState.navController, deepLink)
                    pendingDeepLink = null
                }
            }
            
            CompositionLocalProvider(
                LocalLifecycleAwareComposables provides lifecycleAwareComposables
            ) {
                SooumTheme {
                    SooumApp(
                        appState = appState,
                        finish = ::finish,
                        appVersionUpdate = ::playStore,
                        webView = ::openWebPage,
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
                "market://details?id=${this.packageName}".toUri()
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            throw IllegalArgumentException(e)
        }
    }

    private fun openWebPage(url: String) {
        if (url.trim().isEmpty()) return
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
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
