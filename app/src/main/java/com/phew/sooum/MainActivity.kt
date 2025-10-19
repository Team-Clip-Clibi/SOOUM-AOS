package com.phew.sooum

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.CompositionLocalProvider
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.phew.core.ui.compose.LifecycleAwareComposables
import com.phew.core.ui.util.extension.LocalLifecycleAwareComposables
import com.phew.core_design.theme.SooumTheme
import com.phew.sooum.ui.SooumApp
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var lifecycleAwareComposables: LifecycleAwareComposables

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = true
        controller.isAppearanceLightNavigationBars = true
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        setContent {
            CompositionLocalProvider(
                LocalLifecycleAwareComposables provides lifecycleAwareComposables
            ) {
                SooumTheme {
                    SooumApp(
                        finish = {
                            finish()
                        },
                        appVersionUpdate = {
                            playStore()
                        },
                        webView = { url ->
                            openWebPage(url)
                        }
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
}
