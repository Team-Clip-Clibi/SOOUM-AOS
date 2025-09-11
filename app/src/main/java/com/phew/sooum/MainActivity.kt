package com.phew.sooum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.phew.splash.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.phew.sooum.ui.Nav

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val splashViewModel: SplashViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Nav(splashViewModel = splashViewModel)
        }
    }
}
