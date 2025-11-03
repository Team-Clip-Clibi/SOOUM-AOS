package com.phew.splash.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import com.phew.core_design.slideComposable
import com.phew.splash.Splash
import com.phew.splash.SplashViewModel

const val SPLASH_GRAPH = "splash_graph"

private const val SPLASH_ROUTE = "splash_route"

fun NavHostController.navigateToSplashGraph(
    navOptions: NavOptions? = null
) {
    this.navigate(SPLASH_GRAPH, navOptions)
}

fun NavGraphBuilder.splashNavGraph(
    navToOnBoarding: () -> Unit,
    navToHome:() -> Unit,
    appVersionUpdate: () -> Unit,
    finish: () -> Unit
){
    navigation(
        startDestination = SPLASH_ROUTE,
        route = SPLASH_GRAPH
    ) {
        slideComposable(SPLASH_ROUTE) {
            val splashViewModel: SplashViewModel = hiltViewModel()
            Splash(
                viewModel = splashViewModel,
                signUp = {
                    println("!! $TAG, navToOnBoarding")
                    navToOnBoarding()
                },
                finish = {
                    finish()
                },
                update = {
                    appVersionUpdate()
                },
                home = {
                    navToHome()
                },
            )
        }
    }
}

private const val TAG = "SplashNavGraph"