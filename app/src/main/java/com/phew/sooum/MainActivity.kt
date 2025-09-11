package com.phew.sooum

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.phew.splash.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.phew.sooum.ui.Nav
import androidx.core.net.toUri

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val splashViewModel: SplashViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Nav(
                splashViewModel = splashViewModel,
                finish = {
                    finish()
                },
                update = {
                    playStore()
                }
            )
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
}
