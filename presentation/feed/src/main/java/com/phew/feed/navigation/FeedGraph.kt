package com.phew.feed.navigation

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import com.phew.core.ui.component.home.HomeTabType
import com.phew.core_design.slideComposable
import com.phew.feed.feed.FeedView
import com.phew.feed.notification.NotifyView
import com.phew.feed.viewModel.FeedViewModel
import com.phew.presentation.detail.navigation.navigateToDetailGraph
import com.phew.core.ui.state.SooumAppState

val FEED_GRAPH = HomeTabType.FEED.graph

private val FEED_HOME_ROUTE = HomeTabType.FEED.route

private const val NOTIFY_ROUTE = "notify_route"

fun NavHostController.navigateToFeedGraph(
    navOptions: NavOptions? = null,
) {
    this.navigate(FEED_GRAPH, navOptions)
}

private fun NavHostController.navigateToFeedHome(
    navOptions: NavOptions? = null,
) {
    this.navigate(FEED_HOME_ROUTE, navOptions)
}

private fun NavHostController.navigateToNotify(
    navOptions: NavOptions? = null,
) {
    this.navigate(NOTIFY_ROUTE, navOptions)
}


fun NavGraphBuilder.feedGraph(
    appState: SooumAppState,
    navController: NavHostController,
    webView: (String) -> Unit,
) {
    navigation(
        route = FEED_GRAPH,
        startDestination = FEED_HOME_ROUTE
    ) {
        slideComposable(FEED_HOME_ROUTE) { nav ->
            val feedViewModel: FeedViewModel = hiltViewModel()
            remember { SnackbarHostState() }
            val locationPermission = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions(),
                onResult = { permissionResult ->
                    val isGranted =
                        permissionResult[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
                    feedViewModel.onPermissionResult(isGranted = isGranted)
                }
            )
            LaunchedEffect(feedViewModel) {
                feedViewModel.requestPermissionEvent.collect { permissions ->
                    locationPermission.launch(permissions)
                }
            }
            FeedView(
                appState = appState,
                viewModel = feedViewModel,
                navController = navController,
                requestPermission = {
                    feedViewModel.onPermissionRequest(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                closeDialog = feedViewModel::rationalDialogDismissed,
                noticeClick = navController::navigateToNotify,
                navigateToDetail = { cardDetailArgs ->
                    navController.navigateToDetailGraph(cardDetailArgs)
                },
                webViewClick = webView,
            )
        }

        slideComposable(NOTIFY_ROUTE) { nav ->
            val navBackStackEntry =
                remember(nav) { navController.getBackStackEntry(FEED_GRAPH) }
            val feedViewModel: FeedViewModel = hiltViewModel(navBackStackEntry)
            NotifyView(
                viewModel = feedViewModel,
                backClick = { navController.popBackStack() },
            )
        }
    }
}

private const val TAG = "FeedGraph"
