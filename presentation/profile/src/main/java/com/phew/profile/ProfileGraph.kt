package com.phew.profile

import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.phew.core.ui.component.home.HomeTabType
import com.phew.core.ui.model.navigation.FollowArgs
import com.phew.core.ui.model.navigation.ProfileArgs
import com.phew.core.ui.navigation.asNavParam
import com.phew.core_design.slideComposable
import com.phew.profile.screen.MyProfile
import com.phew.core.ui.navigation.NavArgKey
import com.phew.core.ui.navigation.asNavArg
import com.phew.core.ui.navigation.createNavType
import com.phew.core.ui.navigation.getNavArg
import com.phew.presentation.settings.navigation.navigateToSettingGraph
import com.phew.presentation.settings.navigation.settingGraph
import com.phew.profile.screen.EditProfileScreen
import com.phew.profile.screen.FollowerScreen
import com.phew.profile.screen.OtherProfile

private val PROFILE_ROUTE = HomeTabType.MY.route
private const val PROFILE_ARGS_KEY = NavArgKey

private const val OTHER_PROFILE_ROUTE = "OTHER_PROFILE_ROUTE"
private val OTHER_PROFILE_ROUTE_WITH_AGS = OTHER_PROFILE_ROUTE.asNavParam()
private val OTHER_PROFILE_DESTINATION_ROUTE =
    "$OTHER_PROFILE_ROUTE?$PROFILE_ARGS_KEY=$OTHER_PROFILE_ROUTE_WITH_AGS"

private const val FOLLOW_ROUTE_BASE = "FOLLOW_ROUTE"
private val FOLLOW_PROFILE_ROUTE_WITH_AGS = FOLLOW_ROUTE_BASE.asNavParam()
private val FOLLOW_ROUTE_DESTINATION_ROUTE =
    "$FOLLOW_ROUTE_BASE?$PROFILE_ARGS_KEY=$FOLLOW_PROFILE_ROUTE_WITH_AGS"

private const val CHANGE_PROFILE_BASE = "CHANGE_PROFILE"

fun NavHostController.navigateToProfileGraphWithArgs(
    profileArgs: ProfileArgs,
) {
    this.navigate(OTHER_PROFILE_DESTINATION_ROUTE.asNavArg(profileArgs))
}
fun NavHostController.navigateToEditProfile(
) {
    this.navigate(CHANGE_PROFILE_BASE)
}

fun NavGraphBuilder.profileGraph(
    navController: NavHostController,
    onBackPressed: () -> Unit,
    onLogOut: () -> Unit,
    onWithdrawalComplete: () -> Unit,
    cardClick: (Long) -> Unit,
) {
    navigation(
        route = HomeTabType.MY.graph,
        startDestination = PROFILE_ROUTE,
    ) {

        slideComposable(
            route = PROFILE_ROUTE, arguments = listOf(
                navArgument(PROFILE_ARGS_KEY) {
                    type = createNavType<ProfileArgs>(isNullableAllowed = true)
                    defaultValue = null
                    nullable = true
                }
            )) { navBackStackEntry ->
            val parentEntry = remember(navBackStackEntry) {
                navController.getBackStackEntry(HomeTabType.MY.graph)
            }
            val viewModel: ProfileViewModel = hiltViewModel(parentEntry)
            MyProfile(
                viewModel = viewModel,
                onLogout = onLogOut,
                onClickCard = { id ->
                    cardClick(id)
                },
                onClickSetting = {
                    navController.navigateToSettingGraph()
                },
                onClickFollowing = {
                    navController.navigate(FOLLOW_ROUTE_DESTINATION_ROUTE.asNavArg(FollowArgs(
                        isMyProfile = true,
                        selectTab = TAB_FOLLOWING
                    )))
                },
                onClickFollower = {
                    navController.navigate(FOLLOW_ROUTE_DESTINATION_ROUTE.asNavArg(FollowArgs(
                        isMyProfile = true,
                        selectTab = TAB_FOLLOWER
                    )))
                },
                onEditProfileClick = {
                    navController.navigateToEditProfile()
                },
            )
        }

        slideComposable(
            route = OTHER_PROFILE_DESTINATION_ROUTE,
            arguments = listOf(
                navArgument(PROFILE_ARGS_KEY) {
                    type = createNavType<ProfileArgs>(isNullableAllowed = true)
                    defaultValue = null
                    nullable = true
                }
            )
        ) { navBackStackEntry ->
            val parentEntry = remember(navBackStackEntry) {
                navController.getBackStackEntry(HomeTabType.MY.graph)
            }
            val viewModel: ProfileViewModel = hiltViewModel(parentEntry)
            val userId = navBackStackEntry.arguments?.getNavArg<ProfileArgs>()
            OtherProfile(
                viewModel = viewModel,
                userId = userId?.userId ?: 0,
                onLogOut = onLogOut,
                onBackPress = onBackPressed,
                onClickFollower = {
                    navController.navigate(FOLLOW_ROUTE_DESTINATION_ROUTE.asNavArg(FollowArgs(
                        isMyProfile = false,
                        selectTab = TAB_FOLLOWER
                    )))
                },
                onClickFollowing = {
                    navController.navigate(FOLLOW_ROUTE_DESTINATION_ROUTE.asNavArg(FollowArgs(
                        isMyProfile = false,
                        selectTab = TAB_FOLLOWING
                    )))
                },
                onClickCard = { id ->
                    cardClick(id)
                }
            )
        }

        slideComposable(
            route = FOLLOW_ROUTE_DESTINATION_ROUTE,
            arguments = listOf(
                navArgument(PROFILE_ARGS_KEY) {
                    type = createNavType<FollowArgs>(isNullableAllowed = true)
                    defaultValue = null
                    nullable = true
                }
            )
        ) { navBackStackEntry ->
            val parentEntry = remember(navBackStackEntry) {
                navController.getBackStackEntry(HomeTabType.MY.graph)
            }
            val viewModel: ProfileViewModel = hiltViewModel(parentEntry)
            val followArgs = navBackStackEntry.arguments
                ?.getNavArg<FollowArgs>()
            val isMyProfile = followArgs?.isMyProfile ?: false
            val selectTab = followArgs?.selectTab ?: 0
            FollowerScreen(
                viewModel = viewModel,
                onBackPressed = onBackPressed,
                onLogout = onLogOut,
                isMyProfileView = isMyProfile,
                selectTab = selectTab
            )
        }
        slideComposable(
            route = CHANGE_PROFILE_BASE
        ){ navBackStackEntry ->
            val parentEntry = remember(navBackStackEntry) {
                navController.getBackStackEntry(HomeTabType.MY.graph)
            }
            val viewModel: ProfileViewModel = hiltViewModel(parentEntry)
            EditProfileScreen(
                viewModel = viewModel,
                onBackPress = onBackPressed
            )
        }
        settingGraph(
            navController = navController,
            onBackPressed = onBackPressed,
            onWithdrawalComplete = onWithdrawalComplete
        )
    }
}