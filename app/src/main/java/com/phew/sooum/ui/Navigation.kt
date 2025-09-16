package com.phew.sooum.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.phew.core_common.NAV_ON_BOARDING
import com.phew.core_common.NAV_SIGN_UP_AGREEMENT
import com.phew.core_common.NAV_SIGN_UP_AUTH_CODE
import com.phew.core_common.NAV_SIGN_UP_FINISH
import com.phew.core_common.NAV_SIGN_UP_NICKNAME
import com.phew.core_common.NAV_SIGN_UP_PROFILE
import com.phew.core_common.NAV_SPLASH
import com.phew.core_design.slideComposable
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
    splashViewModel: SplashViewModel,
    signUpViewModel: SignUpViewModel,
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = NAV_SPLASH
    ) {
        slideComposable(NAV_SPLASH) {
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

                }
            )
        }

        slideComposable(NAV_ON_BOARDING) {
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

        slideComposable(NAV_SIGN_UP_AUTH_CODE) {
            AuthCodeView(
                viewModel = signUpViewModel,
                home = {
                    //TODO 홈화면 개발
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        slideComposable(NAV_SIGN_UP_AGREEMENT) {
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

        slideComposable(NAV_SIGN_UP_NICKNAME) {
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

        slideComposable(NAV_SIGN_UP_PROFILE) {
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
                    //TODO 홈화면 개발
                }
            )
        }
    }
}