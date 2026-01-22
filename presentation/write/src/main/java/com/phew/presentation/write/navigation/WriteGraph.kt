package com.phew.presentation.write.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import androidx.navigation.navArgument
import com.phew.core.ui.component.home.HomeTabType
import com.phew.core.ui.model.navigation.CardDetailArgs
import com.phew.core.ui.model.navigation.WriteArgs
import com.phew.core.ui.navigation.NavArgKey
import com.phew.core.ui.navigation.asNavArg
import com.phew.core.ui.navigation.asNavParam
import com.phew.core.ui.navigation.createNavType
import com.phew.core.ui.navigation.getNavArg
import com.phew.core.ui.state.SooumAppState
import com.phew.core_design.slideComposable
import com.phew.presentation.write.screen.WriteRoute


val WRITE_GRAPH = HomeTabType.WRITE.graph

private val WRITE_HOME_ROUTE = HomeTabType.WRITE.route
private val WRITE_ROUTE_WITH_ARGS = HomeTabType.WRITE.route.asNavParam()

fun NavHostController.navigateToWriteGraph(
    navOptions: NavOptions? = null
) {
    this.navigate(WRITE_GRAPH, navOptions)
}

fun NavHostController.navigateToWriteGraphWithArgs(
    writeArgs: WriteArgs,
    navOptions: NavOptions? = null
) {
    this.navigate(WRITE_ROUTE_WITH_ARGS.asNavArg(writeArgs), navOptions)
}

private fun NavHostController.navigateToWriteRoute(
    navOptions: NavOptions? = null
) {
    this.navigate(WRITE_HOME_ROUTE, navOptions)
}

fun NavGraphBuilder.writeGraph(
    appState: SooumAppState,
    navController: NavHostController,
    onBackPressed: () -> Unit,
    onWriteComplete: (CardDetailArgs) -> Unit,
    onDetailWriteComplete: () -> Unit = {},
    onHome : () -> Unit = {}
) {
    navigation(
        route = WRITE_GRAPH,
        startDestination = WRITE_HOME_ROUTE
    ) {
        // 탭에서 접근하는 경로 (파라미터 없음)
        slideComposable(route = WRITE_HOME_ROUTE) { nav ->
            WriteRoute(
                navController = navController,
                args = null,
                onBackPressed = onBackPressed,
                onWriteComplete = onWriteComplete,
                onHome = onBackPressed,
                isFromTab = true
            )
        }
        
        // Detail에서 접근하는 경로 (파라미터 있음)
        slideComposable(
            route = WRITE_ROUTE_WITH_ARGS,
            arguments = listOf(
                navArgument(NavArgKey) {
                    type = createNavType<WriteArgs>(isNullableAllowed = true)
                }
            )
        ) { nav ->
            val args = nav.arguments?.getNavArg<WriteArgs>()
            WriteRoute(
                navController = navController,
                args = args,
                onBackPressed = onBackPressed,
                onWriteComplete = {
                    // parentCardId가 있으면 Detail에서 온 것이므로 Detail 갱신 콜백 호출
                    if (args?.parentCardId != null) {
                        onDetailWriteComplete()
                    } else {
                        onWriteComplete(it)
                    }
                },
                onHome = onHome,
                isFromTab = false
            )
        }
    }
}
