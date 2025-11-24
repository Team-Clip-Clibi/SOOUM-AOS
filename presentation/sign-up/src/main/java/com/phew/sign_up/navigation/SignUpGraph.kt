package com.phew.sign_up.navigation

import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.phew.core.ui.model.navigation.OnBoardingArgs
import com.phew.core.ui.navigation.createNavType
import com.phew.core.ui.navigation.getNavArg
import com.phew.core.ui.navigation.NavArgKey
import com.phew.core.ui.navigation.asNavArg
import com.phew.core.ui.navigation.asNavParam
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
private val ON_BOARDING_ROUTE_WITH_ARGS = "on_boarding_route".asNavParam()
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

fun NavHostController.navigateToOnBoarding(
    args: OnBoardingArgs = OnBoardingArgs(),
    navOptions: NavOptions? = null
) {
    this.navigate(ON_BOARDING_ROUTE_WITH_ARGS.asNavArg(args), navOptions)
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
        startDestination = ON_BOARDING_ROUTE_WITH_ARGS.asNavArg(OnBoardingArgs()),
        route = SIGN_UP_GRAPH
    ) {
        slideComposable(
            route = ON_BOARDING_ROUTE_WITH_ARGS,
            arguments = listOf(
                navArgument(NavArgKey) {
                    type = createNavType<OnBoardingArgs>()
                    defaultValue = OnBoardingArgs()
                }
            )
        ) { nav ->
            val navBackStackEntry =
                remember(nav) { navController.getBackStackEntry(SIGN_UP_GRAPH) }
            val signUpViewModel: SignUpViewModel = hiltViewModel(navBackStackEntry)
            val args = nav.arguments?.getNavArg<OnBoardingArgs>() ?: OnBoardingArgs()
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
                },
                showWithdrawalDialog = args.showWithdrawalDialog
            )
        }

        slideComposable(SIGN_UP_AUTH_CODE_ROUTE) { nav ->
            val navBackStackEntry =
                remember(nav) { navController.getBackStackEntry(SIGN_UP_GRAPH) }
            val signUpViewModel: SignUpViewModel = hiltViewModel(navBackStackEntry)
            AuthCodeView(
                viewModel = signUpViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onRestoreSuccess = navToHome
            )
        }

        slideComposable(SIGN_UP_AGREEMENT_ROUTE) { nav ->
            val navBackStackEntry =
                remember(nav) { navController.getBackStackEntry(SIGN_UP_GRAPH) }
            val signUpViewModel: SignUpViewModel = hiltViewModel(navBackStackEntry)
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