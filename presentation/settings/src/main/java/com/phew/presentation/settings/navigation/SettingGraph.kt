package com.phew.presentation.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.phew.core.ui.model.navigation.CardDetailCommentArgs
import com.phew.core.ui.navigation.createNavType
import com.phew.core.ui.navigation.getNavArg
import com.phew.core_design.slideComposable
import com.phew.core.ui.model.navigation.WebViewUrlArgs
import com.phew.core.ui.navigation.NavArgKey
import com.phew.core.ui.navigation.asNavArg
import com.phew.core.ui.navigation.asNavParam
import com.phew.presentation.settings.screen.LoadPreviousAccountRoute
import com.phew.presentation.settings.screen.LoginOtherDeviceRoute
import com.phew.presentation.settings.screen.NoticeRoute
import com.phew.presentation.settings.screen.PrivacyPolicyRoute
import com.phew.presentation.settings.screen.SettingRoute
import com.phew.presentation.settings.screen.SooumWebViewRoute

const val SETTING_GRAPH = "setting_graph"

private const val SETTING_ROUTE = "setting_route"
private const val LOGIN_OTHER_DEVICE_ROUTE = "login_other_device_route"
private const val LOAD_PREVIOUS_ACCOUNT_ROUTE = "load_previous_account_route"
private const val BLOCKED_USERS_ROUTE = "blocked_users_route"
private const val NOTICE_ROUTE = "notice_route"
private const val INQUIRY_ROUTE = "inquiry_route"
private const val PRIVACY_POLICY_ROUTE = "privacy_policy_route"
private const val APP_UPDATE_ROUTE = "app_update_route"
private const val ACCOUNT_DELETION_ROUTE = "account_deletion_route"
private val WEBVIEW_ROUTE = "webview_route".asNavParam()

fun NavHostController.navigateToSettingGraph(
    navOptions: NavOptions? = null
) {
    this.navigate(SETTING_GRAPH)
}

private fun NavHostController.navigateToLoginOtherDeviceRoute(
    navOptions: NavOptions? = null
) {
    this.navigate(LOGIN_OTHER_DEVICE_ROUTE)
}

private fun NavHostController.navigateToLoadPreviousAccountRoute(
    navOptions: NavOptions? = null
) {
    this.navigate(LOAD_PREVIOUS_ACCOUNT_ROUTE)
}

private fun NavHostController.navigateToBlockedUsersRoute(
    navOptions: NavOptions? = null
) {
    this.navigate(BLOCKED_USERS_ROUTE)
}

private fun NavHostController.navigateToNoticeRoute(
    navOptions: NavOptions? = null
) {
    this.navigate(NOTICE_ROUTE)
}

private fun NavHostController.navigateToInquiryRoute(
    navOptions: NavOptions? = null
) {
    this.navigate(INQUIRY_ROUTE)
}

private fun NavHostController.navigateToPrivacyPolicyRoute(
    navOptions: NavOptions? = null
) {
    this.navigate(PRIVACY_POLICY_ROUTE)
}

private fun NavHostController.navigateToAppUpdateRoute(
    navOptions: NavOptions? = null
) {
    this.navigate(APP_UPDATE_ROUTE)
}

private fun NavHostController.navigateToAccountDeletionRoute(
    navOptions: NavOptions? = null
) {
    this.navigate(ACCOUNT_DELETION_ROUTE)
}

private fun NavHostController.navigateToWebViewRoute(
    args: WebViewUrlArgs,
    navOptions: NavOptions? = null
) {
    this.navigate(WEBVIEW_ROUTE.asNavArg(args), navOptions)
}

fun NavGraphBuilder.settingGraph(
    navController: NavHostController,
    onBackPressed: () -> Unit
) {
    navigation(
        route = SETTING_GRAPH,
        startDestination = SETTING_ROUTE
    ) {
        slideComposable(
            route = SETTING_ROUTE
        ) {
            SettingRoute(
                onBackPressed = onBackPressed,
                onNavigateToLoginOtherDevice = {
                    navController.navigateToLoginOtherDeviceRoute()
                },
                onNavigateToLoadPreviousAccount = {
                    navController.navigateToLoadPreviousAccountRoute()
                },
                onNavigateToBlockedUsers = {
                    navController.navigateToBlockedUsersRoute()
                },
                onNavigateToNotice = {
                    navController.navigateToNoticeRoute()
                },
                onNavigateToInquiry = {
                    navController.navigateToInquiryRoute()
                },
                onNavigateToPrivacyPolicy = {
                    navController.navigateToPrivacyPolicyRoute()
                },
                onNavigateToAccountDeletion = {
                    navController.navigateToAccountDeletionRoute()
                }
            )
        }

        slideComposable(
            route = LOGIN_OTHER_DEVICE_ROUTE
        ) {
            LoginOtherDeviceRoute(
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }

        slideComposable(
            route = LOAD_PREVIOUS_ACCOUNT_ROUTE
        ) {
            LoadPreviousAccountRoute(
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }

        slideComposable(
            route = NOTICE_ROUTE
        ) {
            NoticeRoute(
                onBackPressed = {
                    navController.popBackStack()
                },
                onNoticeItemClick = { webViewArgs ->
                    navController.navigateToWebViewRoute(webViewArgs)
                }
            )
        }

        slideComposable(
            route = PRIVACY_POLICY_ROUTE
        ) {
            PrivacyPolicyRoute(
                onBackPressed = {
                    navController.popBackStack()
                },
                onNavigateToWebView = { webViewArgs ->
                    navController.navigateToWebViewRoute(webViewArgs)
                }
            )
        }

        slideComposable(
            route = WEBVIEW_ROUTE,
            arguments = listOf(
                navArgument(NavArgKey) {
                    type = createNavType<WebViewUrlArgs>()
                }
            )
        ) { nav ->
            val args = nav.arguments?.getNavArg<WebViewUrlArgs>()
            SooumWebViewRoute(
                args = args ?: WebViewUrlArgs(url = ""),
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }
    }
}