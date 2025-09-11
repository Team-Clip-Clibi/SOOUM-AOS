package com.phew.sooum.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.phew.core_common.NAV_SPLASH
import com.phew.splash.SplashScreen
import com.phew.splash.SplashViewModel

@Composable
fun Nav(
    splashViewModel: SplashViewModel,
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = NAV_SPLASH
    ) {
        composable(NAV_SPLASH) {
            SplashScreen(
                viewModel = splashViewModel,
                nextPage = {
                    //TODO 다음 화면 - 로그인
                }
            )
        }
    }
}