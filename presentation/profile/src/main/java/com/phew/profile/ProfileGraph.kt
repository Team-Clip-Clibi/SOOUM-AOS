package com.phew.profile

import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.phew.core.ui.component.home.HomeTabType
import com.phew.core.ui.model.navigation.CardDetailArgs
import com.phew.core.ui.model.navigation.FollowArgs
import com.phew.core.ui.model.navigation.ProfileArgs
import com.phew.core.ui.navigation.asNavParam
import com.phew.core_design.slideComposable
import com.phew.profile.screen.MyProfile
import com.phew.core.ui.navigation.NavArgKey
import com.phew.core.ui.navigation.asNavArg
import com.phew.core.ui.navigation.createNavType
import com.phew.core.ui.navigation.getNavArg
import com.phew.core_common.USER_ID_EMPTY
import com.phew.presentation.settings.navigation.navigateToSettingGraph
import com.phew.presentation.settings.navigation.settingGraph
import com.phew.profile.screen.EditProfileScreen
import com.phew.profile.screen.FollowerScreen
import com.phew.profile.screen.OtherProfile

private val PROFILE_ROUTE = HomeTabType.MY.route
const val PROFILE_ARGS_KEY = NavArgKey

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

fun NavHostController.navigateToFollowScreen(
    isMyProfile: Boolean,
    selectTab: Int,
    userId : Long? = null,
    navOptions: NavOptions? = null
) {
    this.navigate(
        FOLLOW_ROUTE_DESTINATION_ROUTE.asNavArg(
            FollowArgs(
                isMyProfile = isMyProfile,
                selectTab = selectTab,
                userId = userId ?: 0L
            )
        ),
        navOptions
    )
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
    cardClick: (CardDetailArgs) -> Unit,
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
                onClickCard = { args ->
                    cardClick(args)
                },
                onClickSetting = {
                    navController.navigateToSettingGraph()
                },
                onClickFollowing = {
                    navController.navigate(FOLLOW_ROUTE_DESTINATION_ROUTE.asNavArg(FollowArgs(
                        isMyProfile = true,
                        selectTab = TAB_FOLLOWING,
                        userId = USER_ID_EMPTY
                    )))
                },
                onClickFollower = {
                    navController.navigate(FOLLOW_ROUTE_DESTINATION_ROUTE.asNavArg(FollowArgs(
                        isMyProfile = true,
                        selectTab = TAB_FOLLOWER,
                        userId = USER_ID_EMPTY
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
            val viewModel: ProfileViewModel = hiltViewModel()
            val userId = navBackStackEntry.arguments?.getNavArg<ProfileArgs>()
            OtherProfile(
                viewModel = viewModel,
                userId = userId?.userId ?: USER_ID_EMPTY,
                onLogOut = onLogOut,
                onBackPress = onBackPressed,
                onClickFollower = { id ->
                    navController.navigate(FOLLOW_ROUTE_DESTINATION_ROUTE.asNavArg(FollowArgs(
                        isMyProfile = false,
                        selectTab = TAB_FOLLOWER,
                        userId = id
                    )))
                },
                onClickFollowing = { id ->
                    navController.navigate(FOLLOW_ROUTE_DESTINATION_ROUTE.asNavArg(FollowArgs(
                        isMyProfile = false,
                        selectTab = TAB_FOLLOWING,
                        userId = id
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
            val viewModel: ProfileViewModel = hiltViewModel()
            val followArgs = navBackStackEntry.arguments
                ?.getNavArg<FollowArgs>()
            val selectTab = followArgs?.selectTab ?: 0
            val userId = followArgs?.userId ?: viewModel.currentUserId
            FollowerScreen(
                viewModel = viewModel,
                onBackPressed = onBackPressed,
                onLogout = onLogOut,
                selectTab = selectTab,
                userId = userId,
                myProfile = {
                    navController.navigate(HomeTabType.MY.graph) {
                        popUpTo(HomeTabType.MY.route)
                        launchSingleTop = true
                    }
                },
                otherProfile = { id ->
                    navController.navigateToProfileGraphWithArgs(ProfileArgs(id))
                }
            )
        }
        slideComposable(
            route = CHANGE_PROFILE_BASE
        ) { navBackStackEntry ->
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
