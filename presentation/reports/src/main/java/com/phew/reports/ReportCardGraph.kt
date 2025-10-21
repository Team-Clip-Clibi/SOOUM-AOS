package com.phew.reports

import androidx.navigation.NavGraphBuilder
import com.phew.core_design.slideComposable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.navigation.compose.navigation

const val REPORT_GRAPH_ROUTE_PREFIX = "report_graph"
const val CARD_ID_ARG = "cardId"
private val REPORT_ROUTE_WITH_ARGS = "$REPORT_GRAPH_ROUTE_PREFIX/{$CARD_ID_ARG}"
private const val REPORT_VIEW_ROUTE = "report_view_route"

fun NavGraphBuilder.reportGraph(
    onBackPressed: () -> Unit,
) {
    navigation(
        route = REPORT_ROUTE_WITH_ARGS,
        startDestination = REPORT_VIEW_ROUTE,
        arguments = listOf(
            navArgument(CARD_ID_ARG) {
                type = NavType.StringType
            }
        )
    ) {
        slideComposable(route = REPORT_VIEW_ROUTE) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getString(CARD_ID_ARG)
            ReportView(
                cardId = cardId ?: "",
                onBack = onBackPressed
            )
        }
    }
}