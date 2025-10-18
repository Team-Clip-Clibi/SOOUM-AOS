package com.phew.sooum.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.navOptions
import com.phew.core.ui.component.back.SooumOnBackPressed
import com.phew.core.ui.state.SooumAppState
import com.phew.core.ui.state.rememberSooumAppState
import com.phew.home.navigation.homeGraph
import com.phew.home.navigation.navigateToHomeGraph
import com.phew.sign_up.navigation.SIGN_UP_GRAPH
import com.phew.sign_up.navigation.navigateToSignUpGraph
import com.phew.sign_up.navigation.signUpGraph
import com.phew.splash.navigation.SPLASH_GRAPH
import com.phew.splash.navigation.splashNavGraph

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SooumNavHost(
    appState: SooumAppState,
    modifier: Modifier = Modifier,
    appVersionUpdate: () -> Unit,
    finish: () -> Unit,
    webView: (String) -> Unit,
) {
    val navController = appState.navController
    val homeAppState = rememberSooumAppState()

    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = SPLASH_GRAPH,
            modifier = modifier
        ) {
            signUpGraph(
                navController = navController,
                navToHome = {
                    navController.navigateToHomeGraph(
                        navOptions = navOptions {
                            popUpTo(SIGN_UP_GRAPH) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    )
                },
                finish = finish
            )

            homeGraph(
                appState = homeAppState,
                navController = navController,
                finish = finish,
                onBackPressed = {
                    SooumOnBackPressed(appState = appState)
                },
                webView = webView
            )

            splashNavGraph(
                navToOnBoarding = {
                    navController.navigateToSignUpGraph(
                        navOptions = navOptions {
                            popUpTo(SPLASH_GRAPH) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    )
                },
                navToHome = {
                    println("!! $TAG, NavToHome")
                    navController.navigateToHomeGraph(
                        navOptions = navOptions {
                            popUpTo(SIGN_UP_GRAPH) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    )
                },
                appVersionUpdate = appVersionUpdate,
                finish = finish
            )
        }
    }

}

private const val TAG = "SooumNavHost"