package com.phew.presentation.tag.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.navigation
import com.phew.core.ui.component.home.HomeTabType
import com.phew.core.ui.state.SooumAppState
import com.phew.core_design.slideComposable
import com.phew.presentation.tag.screen.TagRoute

val TAG_GRAPH = HomeTabType.TAG.graph

private val TAG_HOME_ROUTE = HomeTabType.TAG.route

fun NavGraphBuilder.tagGraph(
    appState: SooumAppState,
    navController: NavHostController,
    onBackPressed: () -> Unit
) {
    navigation(
        route = TAG_GRAPH,
        startDestination = TAG_HOME_ROUTE
    ) {
        slideComposable(route = TAG_HOME_ROUTE) { nav ->
            TagRoute(
                onBackPressed = onBackPressed
            )
        }
    }
}