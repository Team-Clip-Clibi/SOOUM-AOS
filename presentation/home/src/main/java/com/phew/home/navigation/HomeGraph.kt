package com.phew.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import com.phew.core.ui.state.SooumAppState
import com.phew.feed.navigation.FEED_GRAPH
import com.phew.feed.navigation.feedGraph
import com.phew.presentation.write.navigation.writeGraph

private const val HOME_GRAPH = "home_graph"

fun NavHostController.navigateToHomeGraph(
    navOptions: NavOptions? = null
) {
    this.navigate(HOME_GRAPH, navOptions)
}

/**
 *  버텀 네비게이션을 가지는 최상위 home graph
 */
fun NavGraphBuilder.homeGraph(
    appState: SooumAppState,
    navController: NavHostController,
    finish: () -> Unit,
    onBackPressed: () -> Unit
) {
    navigation(route = HOME_GRAPH, startDestination = FEED_GRAPH) {
        // Feed Card Graph
        feedGraph(
            appState = appState,
            navController = navController,
            finish = finish,
            onBackPressed = onBackPressed
        )

        writeGraph(
            appState = appState,
            navController = navController,
            onBackPressed = onBackPressed
        )

        // TODO 카드 그래프 추가
        // TODO Tag 그래프 추가
        // TODO My 그래프 추가

    }

}