package com.phew.presentation.detail.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import com.phew.core.ui.component.home.HomeTabType
import com.phew.core.ui.model.navigation.CardDetailArgs
import com.phew.core.ui.model.navigation.CardDetailCommentArgs
import com.phew.core.ui.model.navigation.TagViewArgs
import com.phew.core.ui.navigation.NavArgKey
import com.phew.core.ui.navigation.NavigationKeys
import com.phew.core.ui.navigation.asNavArg
import com.phew.core.ui.navigation.asNavParam
import com.phew.core.ui.navigation.createNavType
import com.phew.core.ui.navigation.getNavArg
import com.phew.core_common.CardDetailTrace
import com.phew.core_common.log.SooumLog
import com.phew.core_design.slideComposable
import com.phew.presentation.detail.screen.CardDetailRoute
import com.phew.presentation.detail.screen.CommentCardDetailScreen

val DETAIL_GRAPH = "detail_graph".asNavParam()

private val DETAIL_ROUTE = "detail_route".asNavParam()
private const val COMMENT_ROUTE_BASE = "comment_route"
private val COMMENT_ROUTE = COMMENT_ROUTE_BASE.asNavParam()
private const val PREVIOUS_DETAIL = "detail"

fun NavHostController.navigateToDetailGraph(
    cardDetailArgs: CardDetailArgs,
    navOptions: NavOptions? = null,
) {
    SooumLog.i(TAG, "navigateToDetailGraph() $cardDetailArgs")
    val resolvedOptions = navOptions ?: navOptions {
        popUpTo(DETAIL_GRAPH) {
            inclusive = true
        }
        launchSingleTop = true
    }
    this.navigate(DETAIL_GRAPH.asNavArg(cardDetailArgs), resolvedOptions)
}

private fun NavHostController.navigateToDetailRoute(
    cardDetailArgs: CardDetailArgs,
    navOptions: NavOptions? = null,
) {
    this.navigate(DETAIL_ROUTE.asNavArg(cardDetailArgs), navOptions)
}


fun NavHostController.navigateToDetailCommentDirect(
    cardDetailCommentArgs: CardDetailCommentArgs,
    navOptions: NavOptions? = null,
) {
    SooumLog.i(TAG, "navigateToDetailCommentDirect() $cardDetailCommentArgs")
    this.navigate(COMMENT_ROUTE.asNavArg(cardDetailCommentArgs), navOptions)
}

fun NavGraphBuilder.detailGraph(
    navController: NavController,
    onBackPressed: () -> Unit,
    onNavigateToWrite: (Long) -> Unit,
    onNavigateToReport: (Long) -> Unit,
    onNavigateToViewTags: (TagViewArgs) -> Unit,
    navToHome: () -> Unit,
    onTagPressed: () -> Unit = {},
    onProfileScreen: (Long) -> Unit,
) {
    navigation(
        route = DETAIL_GRAPH,
        startDestination = DETAIL_ROUTE,
        arguments = listOf(
            navArgument(NavArgKey) {
                type = createNavType<CardDetailArgs>()
            }
        )
    ) {
        slideComposable(
            route = DETAIL_ROUTE,
        ) { backStackEntry ->
            val args = backStackEntry.arguments?.getNavArg<CardDetailArgs>()
            if (args == null) {
                SooumLog.e(TAG, "CardDetailArgs is null")
                onBackPressed()
            } else {
                CardDetailRoute(
                    args = args,
                    onNavigateToComment = { commentArgs ->
                        navController.navigate(COMMENT_ROUTE.asNavArg(commentArgs))
                    },
                    onNavigateToHome = navToHome,
                    onNavigateToWrite = { cardId ->
                        onNavigateToWrite(cardId)
                    },
                    onNavigateToReport = onNavigateToReport,
                    onNavigateToViewTags = onNavigateToViewTags,
                    onBackPressed = onBackPressed,
                    profileClick = onProfileScreen,
                    onCardChanged = { navController.markFeedCardUpdated() },
                    cardDetailTrace = args.previousView
                )
            }
        }

        slideComposable(
            route = COMMENT_ROUTE,
            arguments = listOf(
                navArgument(NavArgKey) {
                    type = createNavType<CardDetailCommentArgs>()
                }
            )
        ) { backStackEntry ->
            val args = backStackEntry.arguments?.getNavArg<CardDetailCommentArgs>()
            if (args == null) {
                SooumLog.e(TAG, "CardDetailCommentArgs is null")
                navController.popBackStack()
            } else {
                CommentCardDetailScreen(
                    args = args,
                    onNavigateToComment = { commentArgs ->
                        navController.navigate(COMMENT_ROUTE.asNavArg(commentArgs))
                    },
                    onBackPressed = { parentId ->
                        val previousRoute = navController.previousBackStackEntry?.destination?.route
                        val hasDetailInStack =
                            previousRoute?.contains(PREVIOUS_DETAIL, ignoreCase = true) == true
                        val hasCommentInStack =
                            previousRoute?.startsWith(COMMENT_ROUTE_BASE) == true
                        val shouldNavigateToParent =
                            parentId > 0L && !hasDetailInStack && !hasCommentInStack

                        SooumLog.d(
                            TAG,
                            "onBackPressed() parentId=$parentId, prevRoute=$previousRoute, navigateParent=$shouldNavigateToParent"
                        )

                        if (shouldNavigateToParent) {
                            navController.popBackStack()
                            navController.navigate(
                                COMMENT_ROUTE.asNavArg(
                                    CardDetailCommentArgs(
                                        cardId = parentId,
                                        parentId = 0L,
                                    )
                                ),
                                navOptions {
                                    launchSingleTop = true
                                }
                            )
                        } else {
                            val popped = navController.popBackStack()
                            if (!popped) {
                                SooumLog.w(
                                    TAG,
                                    "Fail to popBackStack from comment route, navigating home"
                                )
                                navToHome()
                            }
                        }
                    },
                    onFeedPressed = navToHome,
                    onTagPressed = onTagPressed,
                    onNavigateToWrite = { cardId ->
                        onNavigateToWrite(cardId)
                    },
                    onNavigateToReport = onNavigateToReport,
                    onNavigateToViewTags = onNavigateToViewTags,
                    onProfileClick = onProfileScreen,
                    onCardChanged = { navController.markFeedCardUpdated() }
                )
            }
        }
    }
}

private const val TAG = "CardDetailNavigation"

private fun NavController.markFeedCardUpdated() {
    val feedEntry = runCatching { getBackStackEntry(HomeTabType.FEED.route) }
        .getOrNull()
        ?: previousBackStackEntry
    feedEntry?.savedStateHandle?.set(NavigationKeys.CARD_UPDATED, true)
}
