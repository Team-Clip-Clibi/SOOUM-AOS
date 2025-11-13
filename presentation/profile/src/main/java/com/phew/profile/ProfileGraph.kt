package com.phew.profile

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.phew.core.ui.component.home.HomeTabType
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

private val PROFILE_ROUTE_WITH_AGS = HomeTabType.MY.route.asNavParam()
private val PROFILE_ROUTE = HomeTabType.MY.route
private const val PROFILE_ARGS_KEY = NavArgKey
private val PROFILE_DESTINATION_ROUTE = "$PROFILE_ROUTE?$PROFILE_ARGS_KEY=$PROFILE_ROUTE_WITH_AGS"

//TODO 추후 다른 사용자 프로필 화면을 위해
fun NavHostController.navigateToProfileGraphWithArgs(
    profileArgs: ProfileArgs,
) {
    this.navigate(PROFILE_ROUTE_WITH_AGS.asNavArg(profileArgs))
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
        startDestination = PROFILE_DESTINATION_ROUTE,
    ) {

        slideComposable(
            route = PROFILE_DESTINATION_ROUTE, arguments = listOf(
                navArgument(PROFILE_ARGS_KEY) {
                    type = createNavType<ProfileArgs>(isNullableAllowed = true)
                    defaultValue = null
                    nullable = true
                }
            )) { navBackStackEntry ->
            val userId = navBackStackEntry.arguments?.getNavArg<ProfileArgs>()
            if (userId == null) {
                MyProfile(
                    onLogout = onLogOut,
                    onClickCard = { id ->
                        cardClick(id)
                    },
                    onClickSetting = {
                        navController.navigateToSettingGraph()
                    },
                    onClickFollowing = {
                        //TODO 팔로잉 화면으로 이동
                    },
                    onClickFollower = {
                        //TODO 팔로워 화면으로 이동
                    },
                    onEditProfileClick = {
                        //TODO 프로필 수정 화면으로 이동
                    },
                )
            }
        }

        settingGraph(
            navController = navController,
            onBackPressed = onBackPressed,
            onWithdrawalComplete = onWithdrawalComplete
        )
    }
}