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
import com.phew.domain.dto.Notice
import com.phew.feed.NotifyTab
import com.phew.feed.notification.WebView
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

val FEED_GRAPH = HomeTabType.FEED.graph

private val FEED_HOME_ROUTE = HomeTabType.FEED.route

private const val NOTIFY_ROUTE = "notify_route"
private const val NOTIFY_ARG_KEY = "notify_index"
private val NOTIFY_ARGS = "$NOTIFY_ROUTE/{$NOTIFY_ARG_KEY}"

private const val WEB_VIEW_ROUTE = "web_view_route"
private const val WEB_VIEW_ARG_KEY = "notice_url"
private val FEED_WEB_VIEW_ARGS = "$WEB_VIEW_ROUTE/{$WEB_VIEW_ARG_KEY}"

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
    data : String,
    navOptions: NavOptions? = null,
) {
    this.navigate("$NOTIFY_ROUTE/$data", navOptions)
}

private fun NavHostController.navigateToWebView(
    url: String,
    navOptions: NavOptions? = null,
) {
    val encodeUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
    this.navigate("$WEB_VIEW_ROUTE/$encodeUrl", navOptions)
}

fun NavGraphBuilder.feedGraph(
    appState: SooumAppState,
    navController: NavHostController,
    // 요기 수정 -> webView 삭제
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
                }
            )
        }

//        slideComposable(NOTIFY_ROUTE) { nav ->
//            val navBackStackEntry =
//                remember(nav) { navController.getBackStackEntry(FEED_GRAPH) }
//            val feedViewModel: FeedViewModel = hiltViewModel(navBackStackEntry)
//            NotifyView(
//                viewModel = feedViewModel,
//                backClick = { navController.popBackStack() },
//                navigateToDetail = { cardDetailArgs ->
//                    navController.navigateToDetailGraph(cardDetailArgs)
//                },
//                navigateToWebView = navController::navigateToWebView,
//                userSelectIndex = navController
//            )
//        }
        slideComposable(
            route = NOTIFY_ARGS,
            arguments = listOf(
                androidx.navigation.navArgument(NOTIFY_ARG_KEY) {
                    type = androidx.navigation.NavType.StringType
                }
            )
        ) { nav ->
            val navBackStackEntry =
                remember(nav) { navController.getBackStackEntry(FEED_GRAPH) }
            val feedViewModel: FeedViewModel = hiltViewModel(navBackStackEntry)
            val data = nav.arguments?.getString(NOTIFY_ARG_KEY) ?: ""
            NotifyView(
                viewModel = feedViewModel,
                backClick = { navController.popBackStack() },
                navigateToDetail = { cardDetailArgs ->
                    navController.navigateToDetailGraph(cardDetailArgs)
                },
                navigateToWebView = navController::navigateToWebView,
                userSelectIndex = NotifyTab.from(data)
            )
        }
        slideComposable(
            route = FEED_WEB_VIEW_ARGS,
            arguments = listOf(
                androidx.navigation.navArgument(WEB_VIEW_ARG_KEY) {
                    type = androidx.navigation.NavType.StringType
                }
            )
        ) { nav ->
            val navBackStackEntry =
                remember(nav) { navController.getBackStackEntry(FEED_GRAPH) }
            val feedViewModel: FeedViewModel = hiltViewModel(navBackStackEntry)
            val url = nav.arguments?.getString(WEB_VIEW_ARG_KEY) ?: ""
            val decodedUrl = java.net.URLDecoder.decode(url, StandardCharsets.UTF_8.toString())
            WebView(
                url = decodedUrl,
                viewModel = feedViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

private const val TAG = "FeedGraph"
