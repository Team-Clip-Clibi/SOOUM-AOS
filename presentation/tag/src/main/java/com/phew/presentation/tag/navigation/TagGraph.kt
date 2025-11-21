package com.phew.presentation.tag.navigation

import android.annotation.SuppressLint
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.phew.core.ui.component.home.HomeTabType
import com.phew.core.ui.model.navigation.CardDetailArgs
import com.phew.core.ui.model.navigation.TagViewArgs
import com.phew.core.ui.navigation.NavArgKey
import com.phew.core.ui.navigation.asNavArg
import com.phew.core.ui.navigation.asNavParam
import com.phew.core.ui.navigation.createNavType
import com.phew.core.ui.navigation.getNavArg
import com.phew.core.ui.state.SooumAppState
import com.phew.core_design.slideComposable
import com.phew.presentation.detail.navigation.navigateToDetailGraph
import com.phew.presentation.tag.screen.SearchRoute
import com.phew.presentation.tag.screen.TagRoute
import com.phew.presentation.tag.screen.ViewTagsRoute
import com.phew.presentation.tag.viewmodel.TagViewModel

val TAG_GRAPH = HomeTabType.TAG.graph

private val TAG_HOME_ROUTE = HomeTabType.TAG.route

private const val SEARCH_ROUTE = "search_route"
private const val VIEW_TAGS_ROUTE = "view_tags_route"
private val VIEW_TAGS_ROUTE_WITH_ARGS = VIEW_TAGS_ROUTE.asNavParam()

private fun NavHostController.navigationSearchRoute(
    navOptions: NavOptions? = null
) {
    this.navigate(SEARCH_ROUTE, navOptions)
}

private fun NavHostController.navigateToViewTags(
    tagName: String,
    tagId: Long,
    navOptions: NavOptions? = null
) {
    val args = TagViewArgs(tagName = tagName, tagId = tagId)
    this.navigate(VIEW_TAGS_ROUTE_WITH_ARGS.asNavArg(args), navOptions)
}

@SuppressLint("UnrememberedGetBackStackEntry")
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
                navigateToSearchScreen = navController::navigationSearchRoute,
                navigateToViewTags = navController::navigateToViewTags
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

        slideComposable(
            route = VIEW_TAGS_ROUTE_WITH_ARGS,
            arguments = listOf(
                navArgument(NavArgKey) {
                    type = createNavType<TagViewArgs>(isNullableAllowed = false)
                }
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments?.getNavArg<TagViewArgs>()
                ?: TagViewArgs(tagName = "", tagId = 0L)

            // TAG_HOME_ROUTE의 ViewModel을 가져와서 refresh 호출
            val tagHomeEntry = navController.getBackStackEntry(TAG_HOME_ROUTE)
            val tagViewModel: TagViewModel = hiltViewModel(tagHomeEntry)
            
            ViewTagsRoute(
                tagName = args.tagName,
                tagId = args.tagId,
                onClickCard = { cardId ->
                    navController.navigateToDetailGraph(CardDetailArgs(cardId))
                },
                onBackPressed = {
                    tagViewModel.refresh()
                    navController.popBackStack()
                }
            )
        }
    }
}