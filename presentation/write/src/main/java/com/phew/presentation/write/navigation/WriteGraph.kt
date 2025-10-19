package com.phew.presentation.write.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import com.phew.core.ui.component.home.HomeTabType
import com.phew.core.ui.state.SooumAppState
import com.phew.core_design.slideComposable
import com.phew.presentation.write.screen.WriteRoute


val WRITE_GRAPH = HomeTabType.WRITE.graph

private val WRITE_ROUTE = HomeTabType.WRITE.route

fun NavHostController.navigateToWriteGraph(
    navOptions: NavOptions? = null
) {
    this.navigate(WRITE_GRAPH, navOptions)
}

private fun NavHostController.navigateToWriteRoute(
    navOptions: NavOptions? = null
) {
    this.navigate(WRITE_ROUTE, navOptions)
}

fun NavGraphBuilder.writeGraph(
    appState: SooumAppState,
    navController: NavHostController,
    onBackPressed: () -> Unit
) {
    navigation(
        route = WRITE_GRAPH,
        startDestination = WRITE_ROUTE
    ) {
        slideComposable(WRITE_ROUTE) { nav ->
            WriteRoute(
                onBackPressed = onBackPressed
            )
        }
    }
}
