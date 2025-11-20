package com.phew.presentation.tag.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import com.phew.core.ui.component.home.HomeTabType
import com.phew.core.ui.model.navigation.CardDetailArgs
import com.phew.core.ui.state.SooumAppState
import com.phew.core_design.slideComposable
import com.phew.presentation.detail.navigation.navigateToDetailGraph
import com.phew.presentation.tag.screen.SearchRoute
import com.phew.presentation.tag.screen.TagRoute
import com.phew.presentation.tag.viewmodel.TagViewModel

val TAG_GRAPH = HomeTabType.TAG.graph

private val TAG_HOME_ROUTE = HomeTabType.TAG.route

private const val SEARCH_ROUTE = "search_route"

private fun NavHostController.navigationSearchRoute(
    navOptions: NavOptions? = null
) {
    this.navigate(SEARCH_ROUTE, navOptions)
}

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
            val tagViewModel: TagViewModel = hiltViewModel(nav)
            TagRoute(
                viewModel = tagViewModel,
                navigateToSearchScreen = navController::navigationSearchRoute
            )
        }

        slideComposable(route = SEARCH_ROUTE) { nav ->
            val tagHomeEntry = navController.getBackStackEntry(TAG_HOME_ROUTE)
            val tagViewModel: TagViewModel = hiltViewModel(tagHomeEntry)
            SearchRoute(
                onClickCard = { cardId ->
                    navController.navigateToDetailGraph(CardDetailArgs(cardId))
                },
                onBackPressed = {
                    tagViewModel.refreshTagScreenData()
                    navController.popBackStack()
                }
            )
        }
    }
}