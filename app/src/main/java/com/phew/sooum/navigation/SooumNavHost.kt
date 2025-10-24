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
import com.phew.presentation.detail.navigation.detailGraph
import com.phew.core.ui.model.navigation.WriteArgs
import com.phew.presentation.write.navigation.navigateToWriteGraphWithArgs
import com.phew.presentation.write.navigation.writeGraph
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
                webView = webView,
                onWriteComplete = {
                    // Feed에서 Write 완료 시 Feed 데이터 갱신
                    // TODO: FeedViewModel refresh 호출
                }
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

            detailGraph(
                navController = navController,
                onBackPressed = {
                    SooumOnBackPressed(appState = appState)
                },
                onNavigateToWrite = { cardId ->
                    navController.navigateToWriteGraphWithArgs(
                        WriteArgs(parentCardId = cardId)
                    )
                },
                onWriteComplete = {
                    // Detail에서 Write 완료 시 Detail 댓글 갱신
                    // TODO: CardDetailViewModel refresh 호출
                },
                detailScreen = { _, _, _ ->

                },
                commentScreen = { _, _, _ ->

                }
            )

            // Detail에서 Write로 갈 때 사용하는 별도 writeGraph
            writeGraph(
                appState = appState,
                navController = navController,
                onBackPressed = {
                    SooumOnBackPressed(appState = appState)
                },
                onWriteComplete = {
                    // Detail에서 Write 완료 시 Detail 댓글 갱신 후 돌아가기
                    // TODO: CardDetailViewModel refresh 호출
                    navController.popBackStack()
                },
                onDetailWriteComplete = {
                    navController.popBackStack()
                }
            )
        }
    }

}

private const val TAG = "SooumNavHost"