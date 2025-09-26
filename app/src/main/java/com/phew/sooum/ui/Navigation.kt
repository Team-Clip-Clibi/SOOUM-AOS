package com.phew.sooum.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.phew.core_common.NAV_HOME
import com.phew.core_common.NAV_HOME_FEED
import com.phew.core_common.NAV_HOME_NOTIFY
import com.phew.core_common.NAV_ON_BOARDING
import com.phew.core_common.NAV_SIGN_UP
import com.phew.core_common.NAV_SIGN_UP_AGREEMENT
import com.phew.core_common.NAV_SIGN_UP_AUTH_CODE
import com.phew.core_common.NAV_SIGN_UP_FINISH
import com.phew.core_common.NAV_SIGN_UP_NICKNAME
import com.phew.core_common.NAV_SIGN_UP_PROFILE
import com.phew.core_common.NAV_SPLASH
import com.phew.core_design.BottomBarComponent
import com.phew.core_design.DialogComponent
import com.phew.core_design.slideComposable
import com.phew.home.FeedView
import com.phew.home.NotifyView
import com.phew.home.viewModel.HomeViewModel
import com.phew.sign_up.AuthCodeView
import com.phew.sign_up.NickNameView
import com.phew.sign_up.OnBoarding
import com.phew.sign_up.ProfileImageView
import com.phew.sign_up.SignUpAgreementView
import com.phew.sign_up.SignUpFinish
import com.phew.sign_up.SignUpViewModel
import com.phew.splash.Splash
import com.phew.splash.SplashViewModel

@Composable
fun Nav(
    update: () -> Unit,
    finish: () -> Unit,
    locationPermission: () -> Unit,
    feedLocationDialogNotShow : Boolean,
    closeDialog: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NAV_SPLASH
    ) {
        splashNavGraph(
            navController = navController,
            update = update,
            finish = finish
        )
        signUpNabGraph(
            navController = navController,
            finish = finish
        )
        homeGraph(
            navController = navController,
            finish = finish,
            dialogDismiss = feedLocationDialogNotShow,
            locationPermission = locationPermission,
            closeDialog = closeDialog
        )
    }
}

fun NavGraphBuilder.splashNavGraph(
    navController: NavController,
    update: () -> Unit,
    finish: () -> Unit
){
    slideComposable(NAV_SPLASH) {
        val splashViewModel: SplashViewModel = hiltViewModel()
        Splash(
            viewModel = splashViewModel,
            nextPage = {
                navController.navigate(NAV_ON_BOARDING)
            },
            finish = {
                finish()
            },
            update = {
                update()
            },
            home = {
                navController.navigate(NAV_HOME_FEED)
            },
        )
    }
}

fun NavGraphBuilder.signUpNabGraph(
    navController: NavController,
    finish: () -> Unit
){
    navigation(
        startDestination = NAV_ON_BOARDING,
        route = NAV_SIGN_UP
    ) {
        slideComposable(NAV_ON_BOARDING) { nav ->
            val navBackStackEntry =
                remember(nav) { navController.getBackStackEntry(NAV_SIGN_UP) }
            val signUpViewModel: SignUpViewModel = hiltViewModel(navBackStackEntry)
            OnBoarding(
                signUp = {
                    navController.navigate(NAV_SIGN_UP_AGREEMENT)
                },
                alreadySignUp = {
                    navController.navigate(NAV_SIGN_UP_AUTH_CODE)
                },
                back = {
                    finish()
                },
                viewModel = signUpViewModel
            )
        }

        slideComposable(NAV_SIGN_UP_AUTH_CODE) { nav ->
            val navBackStackEntry =
                remember(nav) { navController.getBackStackEntry(NAV_SIGN_UP) }
            val signUpViewModel: SignUpViewModel = hiltViewModel(navBackStackEntry)
            AuthCodeView(
                viewModel = signUpViewModel,
                home = {
                    navController.navigate(NAV_HOME_FEED)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        slideComposable(NAV_SIGN_UP_AGREEMENT) { nav ->
            val navBackStackEntry =
                remember(nav) { navController.getBackStackEntry(NAV_SIGN_UP) }
            val signUpViewModel: SignUpViewModel = hiltViewModel(navBackStackEntry)
            SignUpAgreementView(
                viewModel = signUpViewModel,
                back = {
                    navController.popBackStack()
                },
                nextPage = {
                    navController.navigate(NAV_SIGN_UP_NICKNAME)
                }
            )
        }

        slideComposable(NAV_SIGN_UP_NICKNAME) { nav ->
            val navBackStackEntry =
                remember(nav) { navController.getBackStackEntry(NAV_SIGN_UP) }
            val signUpViewModel: SignUpViewModel = hiltViewModel(navBackStackEntry)
            NickNameView(
                viewModel = signUpViewModel,
                onBack = {
                    navController.popBackStack()
                },
                nextPage = {
                    navController.navigate(NAV_SIGN_UP_PROFILE)
                }
            )
        }

        slideComposable(NAV_SIGN_UP_PROFILE) { nav ->
            val navBackStackEntry =
                remember(nav) { navController.getBackStackEntry(NAV_SIGN_UP) }
            val signUpViewModel: SignUpViewModel = hiltViewModel(navBackStackEntry)
            ProfileImageView(
                viewModel = signUpViewModel,
                onBack = {
                    navController.popBackStack()
                },
                nexPage = {
                    navController.navigate(NAV_SIGN_UP_FINISH)
                }
            )
        }

        slideComposable(NAV_SIGN_UP_FINISH) {
            SignUpFinish(
                home = {
                    navController.navigate(NAV_HOME_FEED)
                }
            )
        }
    }
}

fun NavGraphBuilder.homeGraph(
    navController: NavController,
    finish: () -> Unit,
    dialogDismiss: Boolean,
    locationPermission: () -> Unit,
    closeDialog: () -> Unit
) {
    slideComposable(NAV_HOME) { nav ->
        val navBackStackEntry =
            remember(nav) { navController.getBackStackEntry(NAV_HOME) }
        val homeViewModel: HomeViewModel = hiltViewModel(navBackStackEntry)
        val homeNavController = rememberNavController()
        val homeNavBackStackEntry by homeNavController.currentBackStackEntryAsState()
        val currentRoute = homeNavBackStackEntry?.destination?.route
        val snackBarHostState = remember { SnackbarHostState() }

        Scaffold(
            bottomBar = {
                if (currentRoute == NAV_HOME_FEED) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .navigationBarsPadding()
                    ) {
                        BottomBarComponent.HomeBottomBar(
                            homeClick = {
                                homeNavController.navigate(NAV_HOME_FEED) {
                                    popUpTo(homeNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            addCardClick = {
                                //TODO 카드추가 화면 포팅
                            },
                            tagClick = {
                                //TODO 태그 화면 포팅
                            },
                            myProfileClick = {
                                //TODO 마이프로필 화면 포팅
                            },
                        )
                    }
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = snackBarHostState) { data ->
                    DialogComponent.SnackBar(data)
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = homeNavController,
                startDestination = NAV_HOME_FEED,
                modifier = Modifier.padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                )
            ) {
                composable(NAV_HOME_FEED) {
                    FeedView(
                        viewModel = homeViewModel,
                        finish = finish,
                        locationPermission = locationPermission,
                        dialogDismiss = dialogDismiss,
                        closeDialog = closeDialog,
                        noticeClick = { navController.navigate(NAV_HOME_NOTIFY) }
                    )
                }
                slideComposable(NAV_HOME_NOTIFY) {
                    NotifyView(
                        viewModel = homeViewModel,
                        snackBarHostState = snackBarHostState,
                        backClick = { navController.popBackStack() },
                        logout = {}
                    )
                }
            }
        }
    }
}


