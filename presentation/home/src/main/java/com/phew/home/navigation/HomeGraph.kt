package com.phew.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import com.phew.core.ui.component.back.SooumOnBackPressed
import com.phew.core.ui.model.navigation.CardDetailArgs
import com.phew.core.ui.state.SooumAppState
import com.phew.feed.navigation.FEED_GRAPH
import com.phew.feed.navigation.feedGraph
import com.phew.presentation.tag.navigation.tagGraph
import com.phew.presentation.write.navigation.writeGraph
import com.phew.reports.REPORT_GRAPH_ROUTE_PREFIX
import com.phew.profile.profileGraph

private const val HOME_GRAPH = "home_graph"

fun NavHostController.navigateToHomeGraph(
    navOptions: NavOptions? = null,
) {
    this.navigate(HOME_GRAPH, navOptions)
}

fun NavHostController.navigateToReport(cardId: String, navOptions: NavOptions? = null) {
    this.navigate("$REPORT_GRAPH_ROUTE_PREFIX/$cardId", navOptions)
}

/**
 *  버텀 네비게이션을 가지는 최상위 home graph
 */
fun NavGraphBuilder.homeGraph(
    appState: SooumAppState,
    navController: NavHostController,
    finish: () -> Unit,
    onBackPressed: () -> Unit,
    webView: (String) -> Unit,
    onWriteComplete: (CardDetailArgs) -> Unit = {},
    cardClick: (Long) -> Unit,
    onLogOut: () -> Unit,
    onWithdrawalComplete: () -> Unit,
) {
    navigation(route = HOME_GRAPH, startDestination = FEED_GRAPH) {
        // Feed Card Graph
        feedGraph(
            appState = appState,
            navController = navController,
            finish = finish,
            onBackPressed = onBackPressed,
            webView = webView
        )

        writeGraph(
            appState = appState,
            navController = navController,
            onBackPressed = onBackPressed,
            onWriteComplete = {
                onWriteComplete(it)
            },
            onDetailWriteComplete = {
                // Detail에서 Write 완료 시에는 Home에서 처리할 필요 없음 (Detail에서 직접 처리)
                navController.popBackStack()
            }
        )
        tagGraph(
            appState = appState,
            navController = navController,
            onBackPressed = {
                SooumOnBackPressed(appState = appState)
            }
        )

        profileGraph(
            navController = navController,
            onBackPressed = onBackPressed,
            onLogOut = onLogOut,
            onWithdrawalComplete = onWithdrawalComplete,
            cardClick = cardClick
        )

    }

}