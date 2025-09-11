package com.phew.sooum.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.phew.core_common.NAV_SPLASH
import com.phew.core_design.slideComposable
import com.phew.splash.SplashScreen
import com.phew.splash.SplashViewModel

@Composable
fun Nav(
    update: () -> Unit,
    finish: () -> Unit,
    splashViewModel: SplashViewModel,
) {
    val navController = rememberNavController()
    val duration = 300
    val spec = tween<IntOffset>(durationMillis = duration, easing = FastOutSlowInEasing)
    NavHost(
        navController = navController,
        startDestination = NAV_SPLASH
    ) {
        slideComposable(NAV_SPLASH) {
            SplashScreen(
                viewModel = splashViewModel,
                nextPage = {
                    //TODO 다음 화면 - 로그인
                },
                finish = {
                    finish()
                },
                update = {
                    update()
                }
            )
        }
    }
}