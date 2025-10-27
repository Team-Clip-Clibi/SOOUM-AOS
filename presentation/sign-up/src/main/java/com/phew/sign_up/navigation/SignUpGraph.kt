package com.phew.sign_up.navigation

import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import com.phew.core_design.slideComposable
import com.phew.sign_up.AuthCodeView
import com.phew.sign_up.NickNameView
import com.phew.sign_up.OnBoarding
import com.phew.sign_up.ProfileImageView
import com.phew.sign_up.SignUpAgreementView
import com.phew.sign_up.SignUpFinish
import com.phew.sign_up.SignUpViewModel

const val SIGN_UP_GRAPH = "sign_up_graph"
private const val ON_BOARDING_ROUTE = "on_boarding_route"
private const val SIGN_UP_AUTH_CODE_ROUTE = "sign_up_auth_code_route"
private const val SIGN_UP_AGREEMENT_ROUTE = "sign_up_agreement_route"
private const val SIGN_UP_NICKNAME_ROUTE = "sign_up_nickname_route"
private const val SIGN_UP_PROFILE_ROUTE = "sign_up_profile_route"
private const val SIGN_UP_FINISH_ROUTE = "sign_up_finish_route"

fun NavHostController.navigateToSignUpGraph(
    navOptions: NavOptions? = null
) {
    this.navigate(SIGN_UP_GRAPH, navOptions)
}

private fun NavHostController.navigateToOnBoarding(
    navOptions: NavOptions? = null
) {
    this.navigate(ON_BOARDING_ROUTE, navOptions)
}

private fun NavHostController.navigateToSignUpAuthCode(
    navOptions: NavOptions? = null
) {
    this.navigate(SIGN_UP_AUTH_CODE_ROUTE, navOptions)
}

private fun NavHostController.navigateToSignUpAgreement(
    navOptions: NavOptions? = null
) {
    this.navigate(SIGN_UP_AGREEMENT_ROUTE, navOptions)
}

private fun NavHostController.navigateToSignUpNickName(
    navOptions: NavOptions? = null
) {
    this.navigate(SIGN_UP_NICKNAME_ROUTE, navOptions)
}

private fun NavHostController.navigateToSignUpProfile(
    navOptions: NavOptions? = null
) {
    this.navigate(SIGN_UP_PROFILE_ROUTE, navOptions)
}

private fun NavHostController.navigateToSignUpFinish(
    navOptions: NavOptions? = null
) {
    this.navigate(SIGN_UP_FINISH_ROUTE, navOptions)
}

fun NavGraphBuilder.signUpGraph(
    navController: NavHostController,
    navToHome: () -> Unit,
    finish: () -> Unit
) {
    navigation(
        startDestination = ON_BOARDING_ROUTE,
        route = SIGN_UP_GRAPH
    ) {
        slideComposable(ON_BOARDING_ROUTE) { nav ->
            val navBackStackEntry =
                remember(nav) { navController.getBackStackEntry(SIGN_UP_GRAPH) }
            val signUpViewModel: SignUpViewModel = hiltViewModel(navBackStackEntry)
            println("!! $TAG, $ON_BOARDING_ROUTE")
            OnBoarding(
                signUp = navController::navigateToSignUpAgreement,
                alreadySignUp =  navController::navigateToSignUpAuthCode,
                back = {
                    finish()
                },
                viewModel = signUpViewModel,
                home = {
                    navToHome()
                }
            )
        }

        slideComposable(SIGN_UP_AUTH_CODE_ROUTE) { nav ->
            val navBackStackEntry =
                remember(nav) { navController.getBackStackEntry(SIGN_UP_GRAPH) }
            val signUpViewModel: SignUpViewModel = hiltViewModel(navBackStackEntry)
            println("!! $TAG, $SIGN_UP_AUTH_CODE_ROUTE")
            AuthCodeView(
                viewModel = signUpViewModel,
                home = {
                    navToHome()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        slideComposable(SIGN_UP_AGREEMENT_ROUTE) { nav ->
            val navBackStackEntry =
                remember(nav) { navController.getBackStackEntry(SIGN_UP_GRAPH) }
            val signUpViewModel: SignUpViewModel = hiltViewModel(navBackStackEntry)
            println("!! $TAG, $SIGN_UP_AGREEMENT_ROUTE")
            SignUpAgreementView(
                viewModel = signUpViewModel,
                back = {
                    navController.popBackStack()
                },
                nextPage = navController::navigateToSignUpNickName

            )
        }

        slideComposable(SIGN_UP_NICKNAME_ROUTE) { nav ->
            val navBackStackEntry =
                remember(nav) { navController.getBackStackEntry(SIGN_UP_GRAPH) }
            val signUpViewModel: SignUpViewModel = hiltViewModel(navBackStackEntry)
            println("!! $TAG, $SIGN_UP_NICKNAME_ROUTE")
            NickNameView(
                viewModel = signUpViewModel,
                onBack = {
                    navController.popBackStack()
                },
                nextPage = navController::navigateToSignUpProfile

            )
        }

        slideComposable(SIGN_UP_PROFILE_ROUTE) { nav ->
            val navBackStackEntry =
                remember(nav) { navController.getBackStackEntry(SIGN_UP_GRAPH) }
            val signUpViewModel: SignUpViewModel = hiltViewModel(navBackStackEntry)
            println("!! $TAG, $SIGN_UP_PROFILE_ROUTE")
            ProfileImageView(
                viewModel = signUpViewModel,
                onBack = {
                    navController.popBackStack()
                },
                nexPage = navController::navigateToSignUpFinish

            )
        }

        slideComposable(SIGN_UP_FINISH_ROUTE) {
            SignUpFinish(
                home = {
                    navToHome()
                }
            )
        }
    }
}

private const val TAG = "SignUpGraph"