package com.phew.sooum.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.navOptions
import com.phew.core.ui.component.back.SooumOnBackPressed
import com.phew.core.ui.component.home.HomeTabType
import com.phew.core.ui.model.navigation.CardDetailCommentArgs
import com.phew.core.ui.model.navigation.OnBoardingArgs
import com.phew.core.ui.model.navigation.ProfileArgs
import com.phew.core.ui.state.SooumAppState
import com.phew.home.navigation.homeGraph
import com.phew.home.navigation.navigateToHomeGraph
import com.phew.home.navigation.navigateToReport
import com.phew.presentation.detail.navigation.detailGraph
import com.phew.core.ui.model.navigation.WriteArgs
import com.phew.core_common.CardDetailTrace
import com.phew.core_common.log.SooumLog
import com.phew.domain.interceptor.GlobalEvent
import com.phew.presentation.MainViewModel
import com.phew.presentation.detail.navigation.navigateToDetailCommentDirect
import com.phew.presentation.detail.navigation.navigateToDetailGraph
import com.phew.presentation.tag.navigation.navigateToViewTagsWithArgs
import com.phew.presentation.write.navigation.WRITE_GRAPH
import com.phew.presentation.write.navigation.navigateToWriteGraphWithArgs
import com.phew.presentation.write.navigation.writeGraph
import com.phew.profile.navigateToProfileGraphWithArgs
import com.phew.reports.reportGraph
import com.phew.sign_up.navigation.SIGN_UP_GRAPH
import com.phew.sign_up.navigation.navigateToOnBoarding
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
    // 요기 수정 -> webView 삭제
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val navController = appState.navController
    LaunchedEffect(Unit) {
        mainViewModel.globalEvent.collect { event ->
            if (event == GlobalEvent.TeapotEvent) {
                navController.navigateToSignUpGraph(
                    navOptions = navOptions {
                        popUpTo(SPLASH_GRAPH) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                )
            }
        }
    }
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
                appState = appState,
                navController = navController,
                finish = finish,
                onBackPressed = {
                    SooumOnBackPressed(appState = appState)
                },
                // 요기 수정 -> webView 삭제
                onWriteComplete = {
                    navController.navigateToDetailGraph(
                        cardDetailArgs = it,
                        navOptions = navOptions {
                            popUpTo(WRITE_GRAPH) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    )
                },
                onLogOut = {
                    navController.navigateToSignUpGraph(
                        navOptions = navOptions {
                            popUpTo(SPLASH_GRAPH) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    )
                },
                onWithdrawalComplete = {
                    navController.navigateToOnBoarding(
                        args = OnBoardingArgs(showWithdrawalDialog = true),
                        navOptions = navOptions {
                            popUpTo(SPLASH_GRAPH) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    )
                },
                cardClick = { id ->
                    navController.navigateToDetailCommentDirect(
                        cardDetailCommentArgs = CardDetailCommentArgs(
                            cardId = id.cardId,
                            parentId = 0,
                            previousView = CardDetailTrace.PROFILE
                        )
                    )
                }
            )

            reportGraph(
                onBackPressed = {
                    SooumOnBackPressed(appState = appState)
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
                onNavigateToReport = { cardId ->
                    navController.navigateToReport(cardId.toString())
                },
                onNavigateToViewTags = { tagViewArgs ->
                    navController.navigateToViewTagsWithArgs(tagViewArgs)
                },
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
                onTagPressed = {
                    navController.navigateToHomeGraph(
                        navOptions = navOptions {
                            popUpTo(SIGN_UP_GRAPH) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    )
                    // TAG 탭으로 이동하는 로직 추가 - LaunchedEffect를 사용하여 다음 프레임에 실행
                    navController.navigate(HomeTabType.TAG.graph) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(HomeTabType.FEED.route) {
                            saveState = true
                        }
                    }
                },
                onProfileScreen = { profileId ->
                    navController.navigateToProfileGraphWithArgs(ProfileArgs(profileId))
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
                    navController.navigateToDetailGraph(cardDetailArgs = it)
                },
                onDetailWriteComplete = {
                    SooumLog.d(TAG, "onDetailWriteComplete")
                    navController.popBackStack()
                }
            )
        }
    }

}

private const val TAG = "SooumNavHost"
